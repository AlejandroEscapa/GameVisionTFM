package es.androidtfm.gamevision.data.library

import es.androidtfm.gamevision.data.model.TopGame

/*
 * T2.5 (revisión 02/10) — Lógica PURA del Top 4.
 *
 * Vive fuera del Composable a propósito: es la regla de negocio del Top (cuántos
 * caben, qué pasa al añadir uno repetido, qué orden queda) y así se prueba con
 * JUnit sin Android ni Firestore, como RecommendationEngine o LibraryFilters.
 *
 * Decisiones que codifica:
 *  · El máximo es [TopGame.MAX] (4). No se recorta en silencio: `add` devuelve
 *    null si no cabe, y la UI avisa. Recortar callado hacía desaparecer el
 *    primero sin que el usuario supiera por qué.
 *  · Añadir un juego que ya está NO lo duplica ni lo mueve (idempotente).
 *  · El orden de la lista ES la posición en el Top (1..4).
 */
object TopGamesLogic {

    /** true si aún hay hueco para otro juego. */
    fun hasRoom(selected: List<TopGame>): Boolean = selected.size < TopGame.MAX

    /** Posiciones libres que quedan (0 = Top completo). */
    fun freeSlots(selected: List<TopGame>): Int =
        (TopGame.MAX - selected.size).coerceAtLeast(0)

    /** true si el juego ya ocupa un slot. */
    fun contains(selected: List<TopGame>, gameId: String): Boolean =
        selected.any { it.gameId == gameId }

    /**
     * Añade al final del Top.
     *
     * @return la lista nueva, o null si ya estaba o no cabe (el llamante avisa).
     */
    fun add(selected: List<TopGame>, game: TopGame): List<TopGame>? {
        if (game.gameId.isBlank()) return null
        if (contains(selected, game.gameId)) return null
        if (!hasRoom(selected)) return null
        return selected + game
    }

    /** Quita el juego del Top. Devuelve la misma lista si no estaba. */
    fun remove(selected: List<TopGame>, gameId: String): List<TopGame> =
        selected.filterNot { it.gameId == gameId }

    /** Alta o baja según su estado actual (un solo gesto en la UI). */
    fun toggle(selected: List<TopGame>, game: TopGame): List<TopGame> =
        if (contains(selected, game.gameId)) remove(selected, game.gameId)
        else add(selected, game) ?: selected

    /** Solo los ids, en orden: es lo que se persiste en `topGameIds`. */
    fun ids(selected: List<TopGame>): List<String> = selected.map { it.gameId }

    /** Normaliza una entrada (búsqueda o biblioteca) al modelo del Top. */
    fun from(id: String, name: String, coverUrl: String?): TopGame =
        TopGame(gameId = id, name = name, coverUrl = coverUrl.orEmpty())
}
