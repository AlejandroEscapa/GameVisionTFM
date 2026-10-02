package es.androidtfm.gamevision.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import es.androidtfm.gamevision.MainActivity
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.data.library.RewindUtils

/*
 * Aviso de fin de año (F3/T3.11, decisión D3.7).
 *
 * Por qué una notificación y no un aviso dentro de la app: el valor del Rewind está
 * en alcanzar al usuario **cuando no está en la app** (es el motor viral de coste
 * cero de la investigación). Un cartel dentro de la app solo lo ve quien ya entró.
 *
 * Todo lo que se muestra aquí respeta el contrato: copy en español, sin emoji, sin
 * signos de exclamación y **sin culpabilizar** (D3.6). Tocar la notificación abre el
 * Rewind directamente con `gamevision://rewind`.
 */
object RewindNotifier {

    const val CANAL_ID = "rewind"

    private const val PREFS = "rewind_aviso"
    private const val CLAVE_ANIO = "ultimo_anio_avisado"
    private const val ID_NOTIFICACION = 3001

    /** Crea el canal una sola vez. Inofensivo si ya existe. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CANAL_ID) != null) return
        val canal = NotificationChannel(
            CANAL_ID,
            "Tu Rewind",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Aviso de que tu resumen anual de juegos ya está listo"
        }
        manager.createNotificationChannel(canal)
    }

    /** Último año del que ya se avisó, o null si nunca. */
    fun ultimoAnioAvisado(context: Context): Int? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(CLAVE_ANIO, -1)
            .takeIf { it > 0 }

    /** Deja constancia de que ya se avisó de [anio] (una vez por año, D3.7). */
    fun marcarAvisado(context: Context, anio: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(CLAVE_ANIO, anio)
            .apply()
    }

    /**
     * Publica el aviso. **No hace nada si el usuario no ha concedido el permiso**
     * (API 33+): una notificación sin permiso no se puede mostrar y no hay que
     * fingir que se ha hecho.
     */
    fun notificar(context: Context, anio: Int, minutosTotal: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        crearCanal(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("gamevision://rewind")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val horas = RewindUtils.formatHoursTotal(minutosTotal)
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            // TODO(pulido): icono monocromo propio para notificaciones; el logo a
            // color se renderiza como silueta blanca en la barra de estado.
            .setSmallIcon(R.drawable.gamevisionnoletters)
            .setContentTitle("Tu Rewind $anio ya está listo")
            .setContentText("$horas jugadas en $anio. Sin rankings ni comparaciones.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion)
    }
}
