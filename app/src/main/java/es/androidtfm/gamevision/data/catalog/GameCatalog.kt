package es.androidtfm.gamevision.data.catalog

/*
 * Interfaz de dominio del catálogo de juegos (F0/T0.1).
 *
 * Existe para que la app NO dependa de un proveedor concreto. Hoy la implementa
 * RAWG; cuando se cumpla alguna de las condiciones marcadas en
 * docs/roadmap/fase-0-cimientos-datos.md (RAWG degradado, querer metadatos en
 * español, necesitar franquicias/estudios/DLC), se añade una implementación para
 * IGDB y se cambia UNA línea en el módulo de inyección.
 *
 * Todos los métodos devuelven Result: la app nunca finge que una llamada fue bien.
 */
interface GameCatalog {

    /** Busca juegos por término. Devuelve la lista ya mapeada al dominio. */
    suspend fun search(query: String): Result<List<CatalogGame>>

    /** Detalle de un juego. `null` en el Result si el juego no existe. */
    suspend fun getDetails(gameId: Int): Result<CatalogGame?>

    /**
     * true si la última operación se respondió desde caché (modo degradado:
     * el proveedor remoto falló y sirvió datos guardados). La UI lo usa para
     * avisar con discreción; por defecto false (respuesta directa del remoto).
     */
    suspend fun isServingFromCache(): Boolean = false
}
