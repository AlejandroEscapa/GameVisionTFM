package es.androidtfm.gamevision

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import es.androidtfm.gamevision.data.notifications.RewindAvisoWorker
import es.androidtfm.gamevision.data.notifications.RewindNotifier
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application de la app: habilita Hilt como contenedor de dependencias.
 *
 * Implementa [Configuration.Provider] para que WorkManager construya sus trabajos con
 * Hilt (F3/T3.11, decisión D3.7): así el aviso del Rewind usa los repositorios
 * inyectados en vez de construir Firestore a mano, que rompería la regla de DI del
 * proyecto. Por eso el manifest quita el inicializador automático de WorkManager.
 */
@HiltAndroidApp
class GameVisionApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        RewindNotifier.crearCanal(this)
        programarAvisoDelRewind()
    }

    /**
     * Programa la comprobación diaria del aviso. `KEEP` respeta un trabajo ya
     * programado, así que abrir la app cien veces no duplica nada.
     */
    private fun programarAvisoDelRewind() {
        val peticion = PeriodicWorkRequestBuilder<RewindAvisoWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NOMBRE_TRABAJO_AVISO,
            ExistingPeriodicWorkPolicy.KEEP,
            peticion
        )
    }

    private companion object {
        const val NOMBRE_TRABAJO_AVISO = "rewind-aviso-anual"
    }
}
