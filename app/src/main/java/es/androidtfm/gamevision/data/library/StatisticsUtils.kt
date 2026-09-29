package es.androidtfm.gamevision.data.library

import java.util.Calendar

/*
 * Lógica pura de estadísticas (F1 — Bloque 3, T1.12/T1.13).
 *
 * Calcula, a partir de la biblioteca del usuario, los agregados que muestran
 * la pantalla de estadísticas y la semilla del «Tu año en un vistazo».
 * Sin Android ni Firebase: 100 % testeable en JVM.
 */

/** Resumen agregado de la biblioteca para la pantalla de estadísticas. */
data class LibraryInsights(
    val gamesTotal: Int = 0,
    val minutesTotal: Int = 0,
    val byStatus: Map<LibraryStatus, Int> = emptyMap(),
    val completedCount: Int = 0,
    val collectedCount: Int = 0,
    val favoriteCount: Int = 0,
    val ratedCount: Int = 0,
    val ratingAverage: Double? = null,
    /** Distribución de notas: (nota → nº de juegos), en orden ascendente. */
    val ratingDistribution: List<Pair<Double, Int>> = emptyList(),
    /** Géneros más frecuentes: (género → nº de juegos), descendente. */
    val topGenres: List<Pair<String, Int>> = emptyList(),
    /** Plataformas más jugadas (según `lastPlatform`), descendente. */
    val topPlatforms: List<Pair<String, Int>> = emptyList(),
    /** Juegos por año de lanzamiento (año → nº), descendente por año. */
    val gamesByYear: List<Pair<Int, Int>> = emptyList(),
    /** El juego con más minutos acumulados (null si no hay datos). */
    val mostPlayed: LibraryEntry? = null,
    /** Los mejor valorados por el usuario, de mayor a menor nota. */
    val topRated: List<LibraryEntry> = emptyList(),
    // --- Tu año en un vistazo (T1.13) ---
    val year: Int = 0,
    val completedThisYear: Int = 0,
    val addedThisYear: Int = 0,
    val bestOfYear: LibraryEntry? = null
)

object StatisticsUtils {

    /** Año (natural) de un instante. */
    fun yearOf(epochMillis: Long?): Int? {
        if (epochMillis == null || epochMillis <= 0) return null
        return Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.YEAR)
    }

    /** Año de lanzamiento a partir de la cadena «YYYY-MM-DD» (o null). */
    fun releaseYear(released: String): Int? =
        released.takeIf { it.length >= 4 }?.substring(0, 4)?.toIntOrNull()?.takeIf { it in 1950..2100 }

    /**
     * Calcula todos los agregados a partir de la biblioteca. Puro: mismo
     * `entries` + `now` → mismo resultado. `topN` limita las listas «top».
     */
    fun compute(
        entries: List<LibraryEntry>,
        now: Long,
        topN: Int = 5
    ): LibraryInsights {
        if (entries.isEmpty()) {
            return LibraryInsights(year = yearOf(now) ?: 0)
        }

        val byStatus = entries.groupingBy { it.status }.eachCount()
        val rated = entries.mapNotNull { e -> e.rating?.let { it to e } }
        val ratingAverage = if (rated.isEmpty()) null
        else RatingUtils.round1(rated.sumOf { it.first } / rated.size)

        // Distribución por nota (todas las medias estrellas, aunque estén a 0).
        val distribution = RatingUtils.SCALE.map { step ->
            step to rated.count { kotlin.math.abs(it.first - step) < 0.01 }
        }

        val topGenres = entries
            .flatMap { e -> e.genres.map { it.trim() to e } }
            .filter { it.first.isNotBlank() }
            .groupingBy { it.first }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN)
            .map { it.key to it.value }

        val topPlatforms = entries
            .mapNotNull { it.lastPlatform?.takeIf { p -> p.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN)
            .map { it.key to it.value }

        val gamesByYear = entries
            .mapNotNull { releaseYear(it.released) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.key }
            .map { it.key to it.value }

        val mostPlayed = entries.maxByOrNull { it.minutesTotal }?.takeIf { it.minutesTotal > 0 }
        val topRated = entries
            .filter { it.rating != null }
            .sortedWith(compareByDescending<LibraryEntry> { it.rating }.thenBy { it.name })
            .take(topN)

        val currentYear = yearOf(now) ?: 0
        val completedThisYear = entries.count { yearOf(it.finishedAt) == currentYear &&
            (it.status == LibraryStatus.COMPLETED || it.status == LibraryStatus.COLLECTED) }
        val addedThisYear = entries.count { yearOf(it.addedAt) == currentYear }
        val bestOfYear = entries
            .filter { yearOf(it.finishedAt) == currentYear && it.rating != null }
            .maxByOrNull { it.rating ?: 0.0 }

        return LibraryInsights(
            gamesTotal = entries.size,
            minutesTotal = entries.sumOf { it.minutesTotal },
            byStatus = byStatus,
            completedCount = entries.count { it.status == LibraryStatus.COMPLETED },
            collectedCount = entries.count { it.status == LibraryStatus.COLLECTED },
            favoriteCount = entries.count { it.favorite },
            ratedCount = rated.size,
            ratingAverage = ratingAverage,
            ratingDistribution = distribution,
            topGenres = topGenres,
            topPlatforms = topPlatforms,
            gamesByYear = gamesByYear,
            mostPlayed = mostPlayed,
            topRated = topRated,
            year = currentYear,
            completedThisYear = completedThisYear,
            addedThisYear = addedThisYear,
            bestOfYear = bestOfYear
        )
    }
}
