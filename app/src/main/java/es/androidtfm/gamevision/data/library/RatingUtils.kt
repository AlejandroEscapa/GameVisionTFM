package es.androidtfm.gamevision.data.library

/*
 * Utilidades de la nota personal (F1/T1.3).
 *
 * Escala acordada en ADR-0003: 0,5–5,0 en pasos de 0,5 (medias estrellas).
 * Estas funciones son lógica pura (sin Android) y por eso se testean en JVM.
 */

object RatingUtils {

    const val MIN = 0.5
    const val MAX = 5.0
    const val STEP = 0.5

    /** Todas las notas válidas de la escala, de menor a mayor. */
    val SCALE: List<Double> = generateSequence(MIN) { it + STEP }
        .takeWhile { it <= MAX + 1e-9 }
        .map { round1(it) }
        .toList()

    /**
     * Redondea una nota a la rejilla de la escala (pasos de 0,5) y la limita a
     * [MIN, MAX]. Devuelve null si el valor no es representable (p. ej. NaN).
     */
    fun snap(value: Double?): Double? {
        if (value == null || value.isNaN() || value.isInfinite()) return null
        val stepped = kotlin.math.round(value / STEP) * STEP
        return round1(stepped).coerceIn(MIN, MAX)
    }

    /** Nota media a partir de la suma y el número de notas (null si no hay ninguna). */
    fun average(sum: Double, count: Int): Double? =
        if (count <= 0) null else round1(sum / count)

    /** Redondeo a 1 decimal para evitar ruido de coma flotante (2.5000000001 → 2.5). */
    fun round1(value: Double): Double = kotlin.math.round(value * 10.0) / 10.0
}
