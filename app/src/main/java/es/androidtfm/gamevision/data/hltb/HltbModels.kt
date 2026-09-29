package es.androidtfm.gamevision.data.hltb

import java.util.Locale
import kotlin.math.roundToInt

/*
 * Duración de juegos vía HowLongToBeat (F1/T1.11 — decisión D1.4).
 *
 * Datos verificados el 29/09/2026 (docs/investigacion-2026/hltb-verificacion-2026.md):
 * HLTB devuelve los tiempos en SEGUNDOS. Aquí se normalizan a MINUTOS.
 *
 * Flujo no oficial → se implementa con mitigaciones: caché con TTL amplio,
 * valor manual del usuario que siempre gana, y degradación sin romper la app.
 */

/** Tiempos de un juego en HLTB, en MINUTOS (null = sin dato). */
data class HltbPlaytimes(
    val hltbId: Int,
    val mainMinutes: Int?,
    val plusMinutes: Int?,
    val completeMinutes: Int?,
    /** Momento (epoch ms) en que se obtuvo el dato; null si no hay dato. */
    val fetchedAt: Long?
) {
    /** true si hay al menos un tiempo útil. */
    val hasData: Boolean
        get() = (mainMinutes ?: 0) > 0 || (plusMinutes ?: 0) > 0 || (completeMinutes ?: 0) > 0
}

object HltbUtils {

    /** TTL del dato cacheado: los tiempos de HLTB derivan muy lentamente. */
    const val CACHE_TTL_MS = 90L * 24 * 60 * 60 * 1000

    /** Segundos → minutos, redondeando; null si el valor no es positivo. */
    fun secondsToMinutes(seconds: Long?): Int? {
        if (seconds == null || seconds <= 0) return null
        return (seconds / 60.0).roundToInt().takeIf { it > 0 }
    }

    /**
     * Clave de caché normalizada del nombre del juego: minúsculas, sin acentos
     * ni signos, espacios colapsados. Sirve para no pedir dos veces lo mismo.
     */
    fun cacheKey(name: String): String {
        val lower = name.lowercase(Locale.ROOT)
        val noAccents = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return noAccents
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /** true si el dato cacheado sigue vigente. */
    fun isFresh(fetchedAt: Long?, now: Long): Boolean =
        fetchedAt != null && (now - fetchedAt) in 0 until CACHE_TTL_MS

    /** Formatea minutos como «12 h» o «1 h 30 min» (para la ficha). */
    fun format(minutes: Int?): String {
        if (minutes == null || minutes <= 0) return "—"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m min"
            m == 0 -> "$h h"
            else -> "$h h $m min"
        }
    }
}
