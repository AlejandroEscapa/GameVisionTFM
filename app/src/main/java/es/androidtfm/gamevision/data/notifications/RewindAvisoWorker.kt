package es.androidtfm.gamevision.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.library.RewindUtils
import es.androidtfm.gamevision.data.session.SessionRepository
import es.androidtfm.gamevision.data.session.SessionState
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/*
 * Trabajo diario que decide si toca avisar del Rewind (F3/T3.11, decisión D3.7).
 *
 * **Por qué diario y no un aviso programado para el 1 de enero.** Un
 * `OneTimeWorkRequest` con retardo de meses se pierde con más facilidad (el usuario
 * puede no abrir la app, el sistema puede matar el proceso) y no hay forma de
 * recuperarlo. Un trabajo **periódico diario e idempotente** es más robusto: si el
 * móvil estuvo apagado el 1 de enero, el aviso sale el día que pueda, y la regla de
 * «una vez por año» evita repetirlo. Es la misma razón por la que se descartó
 * `AlarmManager` (no sobrevive al reinicio sin un receptor `BOOT_COMPLETED`).
 *
 * Es idempotente: se apoya en `RewindAviso.debeAvisar` y en el año ya avisado.
 */
@HiltWorker
class RewindAvisoWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val libraryRepository: LibraryRepository,
    private val sessionRepository: SessionRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Sin sesión iniciada no hay Rewind que anunciar (invitado o anónimo).
        val uid = (sessionRepository.sessionState.value as? SessionState.LoggedIn)?.uid
            ?: return Result.success()

        val entries = libraryRepository.observeLibrary(uid).first().getOrNull()
            ?: return Result.retry()
        val sessions = libraryRepository.observeSessions(uid).first().getOrNull().orEmpty()

        val hoy = LocalDate.now()
        val anio = RewindAviso.anioQueCierra(hoy)
        val recap = RewindUtils.compute(entries, sessions, anio)

        val yaAvisado = RewindNotifier.ultimoAnioAvisado(applicationContext)
        if (!RewindAviso.debeAvisar(recap.minutesTotal, yaAvisado, hoy)) return Result.success()

        RewindNotifier.notificar(applicationContext, anio, recap.minutesTotal)
        RewindNotifier.marcarAvisado(applicationContext, anio)
        return Result.success()
    }
}
