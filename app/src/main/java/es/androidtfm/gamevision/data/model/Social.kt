package es.androidtfm.gamevision.data.model

/*
 * Modelos sociales de F2 (seguimiento asimétrico, feed, listas curadas).
 * Sustituyen a los Map<String, Any> a mano. D2.1–D2.7 en fase-2-social.md.
 */

/** Arista de seguimiento (D2.1): id determinista `{followerUid}_{followedUid}`. */
data class FollowEdge(
    val followerUid: String = "",
    val followedUid: String = ""
) {
    companion object {
        /** Id del documento en `following/`: determinista = idempotente. */
        fun edgeId(followerUid: String, followedUid: String): String = "${followerUid}_${followedUid}"

        fun fromMap(id: String, data: Map<String, Any?>?): FollowEdge {
            if (data == null) return FollowEdge()
            return FollowEdge(
                followerUid = data["followerUid"]?.toString().orEmpty(),
                followedUid = data["followedUid"]?.toString().orEmpty()
            )
        }
    }
}

/** Tipos de hito del feed (D2.3): nunca sesiones diarias. */
object MilestoneTypes {
    const val COMPLETED = "completed"
    const val REVIEW = "review"
    const val LIST_PUBLIC = "list_public"
    val ALL = listOf(COMPLETED, REVIEW, LIST_PUBLIC)
}

/**
 * Entrada del feed (D2.3): un hito automático o un post manual del usuario
 * (ampliación del propietario, 30/09). Texto de posts: 1–280 en reglas.
 */
data class FeedEntry(
    val id: String = "",
    val type: String = "",
    val authorUid: String = "",
    val text: String = "",
    val gameId: String = "",
    val gameName: String = "",
    val gameCover: String = "",
    val milestoneType: String = "",
    val rating: Float? = null,
    val likesCount: Long = 0,
    val createdAt: Long = 0,
    /** Relleno en cliente al construir la tarjeta, no vive en Firestore. */
    val likedByMe: Boolean = false
) {
    val isMilestone: Boolean get() = type == TYPE_MILESTONE

    companion object {
        const val TYPE_MILESTONE = "milestone"
        const val TYPE_POST = "post"
        const val MAX_POST_CHARS = 280

        fun fromMap(id: String, data: Map<String, Any?>?): FeedEntry {
            if (data == null) return FeedEntry(id = id)
            return FeedEntry(
                id = id,
                type = data["type"]?.toString().orEmpty(),
                authorUid = data["authorUid"]?.toString().orEmpty(),
                text = data["text"]?.toString().orEmpty(),
                gameId = data["gameId"]?.toString().orEmpty(),
                gameName = data["gameName"]?.toString().orEmpty(),
                gameCover = data["gameName"].let { data["gameCover"]?.toString().orEmpty() },
                milestoneType = data["milestoneType"]?.toString().orEmpty(),
                rating = (data["rating"] as? Number)?.toFloat(),
                likesCount = (data["likesCount"] as? Number)?.toLong() ?: 0L,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/** Lista curada (T2.6): pública o privada, con progreso sobre la biblioteca. */
data class GameList(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val gameIds: List<String> = emptyList(),
    val createdAt: Long = 0
) {
    companion object {
        fun fromMap(id: String, ownerUid: String, data: Map<String, Any?>?): GameList {
            if (data == null) return GameList(id = id, ownerUid = ownerUid)
            return GameList(
                id = id,
                ownerUid = ownerUid,
                name = data["name"]?.toString().orEmpty(),
                description = data["description"]?.toString().orEmpty(),
                isPublic = data["isPublic"] as? Boolean ?: true,
                gameIds = (data["gameIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
