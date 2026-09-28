package es.androidtfm.gamevision.data.catalog

/*
 * Modelo de dominio del catálogo de juegos.
 *
 * Es NUESTRO modelo: ni la UI ni los ViewModels conocen ya los DTOs del proveedor
 * (hoy RAWG). Cambiar de proveedor no toca ni una pantalla — ese es el objetivo
 * de esta capa (ver docs/roadmap/fase-0-cimientos-datos.md, tarea T0.1).
 */

data class CatalogGame(
    val id: Int,
    val name: String,
    val coverUrl: String?,
    val released: String = "",
    val rating: Double = 0.0,
    val ratingsCount: Int = 0,
    val metacritic: Int? = null,
    val suggestionsCount: Int = 0,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    /**
     * Duración estimada en horas. Hueco reservado (decisión D0.6): el campo existe
     * pero la FUENTE de los datos está pendiente de decidir. Se deja nulo hasta entonces.
     */
    val playtimeHours: Int? = null
)
