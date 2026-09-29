package es.androidtfm.gamevision.data.analytics

/*
 * Analítica de producto (F1 — Bloque 4, instrumentación).
 *
 * Se abstrae detrás de una interfaz para que el resto del código NO dependa de
 * Firebase directamente (testeable en JVM y cambiable de proveedor). El objetivo
 * es medir el embudo real: registro → primer juego → hábito (sesiones/diario).
 *
 * Regla de privacidad: solo eventos y parámetros NO personales (nada de emails).
 */

/** Eventos de embudo que medimos. Los nombres son estables (no traducir). */
object AnalyticsEvents {
    const val SIGN_UP = "sign_up"
    const val LOGIN = "login"
    const val ADD_GAME = "add_game"          // primer juego / añadir a biblioteca
    const val STATUS_CHANGE = "status_change"
    const val RATE_GAME = "rate_game"
    const val REVIEW_GAME = "review_game"
    const val LOG_SESSION = "log_session"    // apunte en el diario
    const val VIEW_DIARY = "view_diary"
    const val VIEW_STATS = "view_stats"
    const val SET_MANUAL_PLAYTIME = "set_manual_playtime"
}

/** Punto único de registro de eventos de producto. */
interface AnalyticsLogger {
    fun log(event: String, params: Map<String, Any?> = emptyMap())
    /** Identifica al usuario por su uid (nunca por email). */
    fun setUserId(uid: String?)
    /** Marca la pantalla actual (para embudos de navegación). */
    fun logScreen(screenName: String)
}
