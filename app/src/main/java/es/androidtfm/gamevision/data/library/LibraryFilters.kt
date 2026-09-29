package es.androidtfm.gamevision.data.library

/*
 * Filtros y orden de la biblioteca (F1 — Bloque 4, T1.14/T1.15).
 *
 * Lógica pura (sin Android) para poder testearla en JVM. La UI le pasa los
 * valores ya extraídos de cada ficha (nombre, géneros, plataforma) y opciones
 * de filtro; nada de Compose aquí dentro.
 */

object LibraryFilters {

    /** Criterios de filtro activos. `null` = sin filtrar por ese campo. */
    data class Criteria(
        val query: String = "",
        val genre: String? = null,
        val platform: String? = null
    ) {
        val isActive: Boolean
            get() = query.isNotBlank() || genre != null || platform != null
    }

    /** Opciones de orden disponibles en la lista. */
    enum class Sort(val label: String) {
        ALPHA("Alfabético"),
        YEAR_ASC("Año (Asc.)"),
        YEAR_DESC("Año (Desc.)"),
        MOST_PLAYED("Más jugados"),
        BEST_RATED("Mejor nota");

        companion object {
            fun fromLabel(label: String): Sort = entries.firstOrNull { it.label == label } ?: ALPHA
            val labels: List<String> get() = entries.map { it.label }
        }
    }

    /**
     * ¿Cumple la ficha todos los criterios activos?
     * La búsqueda es por nombre (sin distinguir mayúsculas); el género y la
     * plataforma comparan sin distinguir mayúsculas y sin espacios sobrantes.
     */
    fun matches(
        name: String,
        genres: List<String>,
        platform: String?,
        criteria: Criteria
    ): Boolean {
        val q = criteria.query.trim()
        if (q.isNotBlank() && !name.contains(q, ignoreCase = true)) return false
        criteria.genre?.let { g ->
            if (genres.none { it.trim().equals(g, ignoreCase = true) }) return false
        }
        criteria.platform?.let { p ->
            if (!platform.equals(p, ignoreCase = true)) return false
        }
        return true
    }

    /**
     * Ordena una lista de fichas por el criterio elegido. `released` es «YYYY-MM-DD».
     * Con «Mejor nota», las fichas sin nota van al final.
     */
    fun sort(entries: List<LibraryEntry>, sort: Sort): List<LibraryEntry> = when (sort) {
        Sort.ALPHA -> entries.sortedBy { it.name.lowercase() }
        Sort.YEAR_ASC -> entries.sortedBy { it.released }
        Sort.YEAR_DESC -> entries.sortedByDescending { it.released }
        Sort.MOST_PLAYED -> entries.sortedByDescending { it.minutesTotal }
        Sort.BEST_RATED -> entries.sortedWith(
            compareByDescending<LibraryEntry> { it.rating ?: -1.0 }.thenBy { it.name.lowercase() }
        )
    }
}
