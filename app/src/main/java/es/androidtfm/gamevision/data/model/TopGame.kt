package es.androidtfm.gamevision.data.model

/*
 * Modelo de dominio del Top 4 (T2.5, revisión 02/10/2026).
 *
 * ANTES el Top 4 era SOLO una lista de ids (`topGameIds`) y la portada y el
 * nombre se resolvían contra la biblioteca del espectador. Con el buscador de
 * catálogo (puedes elegir un juego que NO está en tu biblioteca) eso rompía: el
 * perfil público pintaba "#1 Sin definir" porque no encontraba el id.
 *
 * Ahora cada slot guarda su propia miniatura (id + nombre + portada). El perfil
 * se pinta sin depender de la biblioteca ni de la red. `UserProfile.topGameIds`
 * se mantiene como índice plano por compatibilidad y para lecturas baratas.
 */

data class TopGame(
    /** RAWG id como String (mismo formato que `topGameIds` y `LibraryEntry.gameId`). */
    val gameId: String,
    val name: String = "",
    val coverUrl: String = ""
) {
    companion object {
        /** Máximo de slots del Top (decisión: 4 por ahora, extensible). */
        const val MAX = 4

        fun fromMap(data: Map<*, *>?): TopGame? {
            if (data == null) return null
            val id = data["gameId"]?.toString().orEmpty()
            if (id.isBlank()) return null
            return TopGame(
                gameId = id,
                name = data["name"]?.toString().orEmpty(),
                coverUrl = data["coverUrl"]?.toString().orEmpty()
            )
        }

        fun fromList(value: Any?): List<TopGame> =
            (value as? List<*>).orEmpty().mapNotNull { fromMap(it as? Map<*, *>) }

        fun toMapList(games: List<TopGame>): List<Map<String, Any>> =
            games.map { mapOf("gameId" to it.gameId, "name" to it.name, "coverUrl" to it.coverUrl) }
    }
}
