package es.androidtfm.gamevision.data.social

import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.data.model.MilestoneTypes

/*
 * Lógica pura de F2 (sin Firestore ni Android): todo testeable con JUnit.
 * La capa de datos orquesta el I/O; aquí vive la decisión, no el acceso a red.
 * Espeja las restricciones que también gobiernan las reglas de Firestore.
 */

/**
 * D2.2 — ¿Quién puede ver el contenido público (biblioteca/stats) de un usuario?
 * Espejo en cliente de la regla `canSeePublicContent`: sirve para pintar el estado
 * "cuenta privada" antes de intentar la lectura.
 */
object VisibilityLogic {

    const val REASON_PUBLIC = "public"
    const val REASON_PRIVATE = "private"
    const val REASON_BLOCKED = "blocked"

    /**
     * @param viewerUid quien mira ("" = sin sesión; nunca ve nada)
     * @param ownerUid dueño del contenido
     * @param isPrivate cuenta cerrada (default false = pública)
     * @param blocked ¿hay bloqueo en cualquiera de las dos direcciones?
     */
    fun canSee(
        viewerUid: String,
        ownerUid: String,
        isPrivate: Boolean,
        blocked: Boolean
    ): Boolean {
        if (viewerUid.isBlank()) return false
        if (blocked) return false
        return viewerUid == ownerUid || !isPrivate
    }

    /** Motivo por el que no se ve (para el copy del estado vacío). */
    fun reasonNotVisible(
        viewerUid: String,
        ownerUid: String,
        isPrivate: Boolean,
        blocked: Boolean
    ): String = when {
        canSee(viewerUid, ownerUid, isPrivate, blocked) -> REASON_PUBLIC
        blocked -> REASON_BLOCKED
        else -> REASON_PRIVATE
    }
}

/**
 * D2.1 — id de la arista de seguimiento y decisión de si ya se sigue.
 * Las aristas son idempotentes: seguir dos veces es una sola.
 */
object FollowLogic {

    fun edgeId(followerUid: String, followedUid: String): String =
        "${followerUid}_${followedUid}"

    fun isFollowing(myEdges: Set<String>, otherUid: String): Boolean =
        otherUid in myEdges

    /** Tras alternar seguir/no seguir, ¿queda la arista presente? */
    fun afterToggle(currentlyFollowing: Boolean): Boolean = !currentlyFollowing
}

/**
 * Sin Cloud Functions (D2.4: sin Blaze), el feed se consulta por chunks de
 * `whereIn` (límite duro de Firestore: 30 por disyunción) y se mezclan en
 * cliente ordenados por fecha.
 */
object FeedQueryPlanner {

    /** Límite duro de Firestore para disyunciones `in`. */
    const val MAX_CHUNK = 30
    const val PAGE_SIZE = 20

    /** Parte los uids seguidos en chunks válidos para whereIn. */
    fun chunksFor(followedUids: List<String>, maxChunk: Int = MAX_CHUNK): List<List<String>> {
        require(maxChunk > 0) { "maxChunk debe ser positivo" }
        if (followedUids.isEmpty()) return emptyList()
        return followedUids.chunked(maxChunk)
    }

    /**
     * Mezcla las páginas de los chunks: dedupe por id y orden descendente por
     * createdAt (empate estable por id para no bailar la lista entre refrescos).
     */
    fun merge(vararg pages: List<FeedEntryPair>): List<FeedEntryPair> {
        val byId = LinkedHashMap<String, FeedEntryPair>()
        for (page in pages) {
            for (entry in page) {
                val key = entry.id.ifBlank { entry.entry.hashCode().toString() }
                if (!byId.containsKey(key)) byId[key] = entry
            }
        }
        return byId.values.sortedWith(
            compareByDescending<FeedEntryPair> { it.entry.createdAt }
                .thenByDescending { it.id }
        )
    }

    /**
     * Ordena entradas de feed en cliente (mismo criterio que merge):
     * createdAt descendente con empate estable por id.
     * Usado por el fallback del repo cuando falta el índice compuesto
     * feed(authorUid, createdAt) — D2.4 ya ordena en cliente al mezclar chunks.
     */
    fun sortFeedEntries(entries: List<FeedEntryPair>): List<FeedEntryPair> =
        entries.sortedWith(
            compareByDescending<FeedEntryPair> { it.entry.createdAt }.thenByDescending { it.id }
        )

    /**
     * D2.5: quita del feed las entradas de autores bloqueados (en cualquier
     * dirección). Las reglas dejan el listado del feed abierto con sesión (es la
     * página pública del producto); el corte de visibilidad entre bloqueados lo
     * aplica el cliente tras resolver el autor. Al fallar la lectura del perfil
     * el autor viaja vacío, así que el filtro mira el uid, no el nombre.
     */
    fun filterBlocked(entries: List<FeedEntryPair>, blockedUids: Set<String>): List<FeedEntryPair> =
        if (blockedUids.isEmpty()) entries
        else entries.filter { it.entry.authorUid !in blockedUids }

    /** ¿Merece la pena pedir la página siguiente ya? */
    fun hasMorePages(loaded: Int, pageSize: Int = PAGE_SIZE): Boolean = loaded >= pageSize
}

/** Par feed + su autor resuelto (evita mapas paralelos en la UI). */
data class FeedEntryPair(
    val id: String,
    val entry: FeedEntry,
    val authorName: String = "",
    val authorUsername: String = ""
)

/**
 * D2.3 — ¿Qué hito genera cada acción? Las sesiones diarias NUNCA van al feed.
 */
object MilestonePlanner {

    fun forAction(action: String, gameId: String): String? {
        if (gameId.isBlank()) return null
        return when (action) {
            "game_completed" -> MilestoneTypes.COMPLETED
            "review_published" -> MilestoneTypes.REVIEW
            "list_published" -> MilestoneTypes.LIST_PUBLIC
            else -> null
        }
    }

    /** Id determinista del hito: re-completar el mismo juego NO duplica entrada. */
    fun milestoneId(authorUid: String, milestoneType: String, gameId: String): String =
        "${authorUid}_${milestoneType}_${gameId}"
}

/**
 * T2.6 — progreso de una lista contra la biblioteca del espectador/propietario.
 */
object ListProgress {

    data class Progress(val total: Int, val inLibrary: Int) {
        val fraction: Float get() = if (total == 0) 0f else inLibrary.toFloat() / total
    }

    fun of(list: GameList, libraryGameIds: Set<String>): Progress =
        Progress(
            total = list.gameIds.size,
            inLibrary = list.gameIds.count { it in libraryGameIds }
        )

    /** CA2.4: una lista privada solo la ve su dueño. */
    fun canSee(list: GameList, viewerUid: String, blocked: Boolean): Boolean =
        VisibilityLogic.canSee(viewerUid, list.ownerUid, isPrivate = !list.isPublic, blocked = blocked)
}

/**
 * D2.3 (posts) — validación del composer, espejo de las reglas: 1–280 chars.
 */
object PostComposer {

    const val MAX_CHARS = 280

    sealed class Validity {
        data object Ok : Validity()
        data class Invalid(val reason: String) : Validity()
    }

    fun validate(text: String): Validity = when {
        text.isBlank() -> Validity.Invalid("El post está vacío")
        text.length > MAX_CHARS -> Validity.Invalid("Máximo $MAX_CHARS caracteres")
        else -> Validity.Ok
    }

    fun canPublish(text: String): Boolean = validate(text) is Validity.Ok
}
