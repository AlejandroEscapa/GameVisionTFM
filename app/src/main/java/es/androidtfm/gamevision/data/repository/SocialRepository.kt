package es.androidtfm.gamevision.data.repository

import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.model.FollowEdge
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.social.FeedEntryPair
import es.androidtfm.gamevision.data.social.FeedQueryPlanner
import es.androidtfm.gamevision.data.social.MilestonePlanner
import es.androidtfm.gamevision.data.repository.usernameAliasId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 30/09/2026 (Fase 2 — Social; D2.1–D2.7)
 *
 * Seguimiento asimétrico, feed de hitos + posts, me gusta, listas curadas,
 * búsqueda por username (sin Blaze: índice + reglas) y moderación mínima.
 *
 * Reglas de esta capa:
 *  - TODOS los métodos devuelven Result: nunca se traga un error en silencio.
 *  - Sin escrituras cruzadas: los contadores se leen con aggregate count().
 *  - El feed se consulta por chunks de whereIn (sin Cloud Functions, D2.4).
 */
@Singleton
class SocialRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "SocialRepository"
        private const val USERS = "users"
        private const val FOLLOWING = "following"
        private const val FEED = "feed"
        private const val LIKES = "likes"
        private const val BLOCKS = "blocks"
        private const val PEOPLE = "people"
        private const val REPORTS = "reports"
        private const val USERNAMES = "usernames"
        private const val LISTS = "gamelist"
        private const val LISTS_PRIVATE = "gamelist_private"

        /** Máximo de uids por whereIn: límite duro de Firestore. */
        const val MAX_FEED_CHUNK = FeedQueryPlanner.MAX_CHUNK

        /** Mínimo de caracteres para lanzar la búsqueda por prefijo. */
        const val MIN_PREFIX = 3
    }

    // ------------------------------------------------------------------------
    // Seguimiento (D2.1)
    // ------------------------------------------------------------------------

    /** Uids de a quien sigo. */
    suspend fun followingIds(uid: String): Result<Set<String>> = runCatching {
        db.collection(FOLLOWING)
            .whereEqualTo("followerUid", uid)
            .get().await()
            .documents.mapNotNull { it.getString("followedUid") }.toSet()
    }.onFailure { Log.e(TAG, "followingIds($uid): ${it.message}") }

    /** Uids de mis seguidores. */
    suspend fun followerIds(uid: String): Result<Set<String>> = runCatching {
        db.collection(FOLLOWING)
            .whereEqualTo("followedUid", uid)
            .get().await()
            .documents.mapNotNull { it.getString("followerUid") }.toSet()
    }.onFailure { Log.e(TAG, "followerIds($uid): ${it.message}") }

    /** (seguidores, seguidos) por aggregate count: sin escrituras cruzadas. */
    suspend fun followCounts(uid: String): Result<Pair<Long, Long>> = runCatching {
        val followers = db.collection(FOLLOWING)
            .whereEqualTo("followedUid", uid)
            .count()
            .get(AggregateSource.SERVER).await()
            .count
        val following = db.collection(FOLLOWING)
            .whereEqualTo("followerUid", uid)
            .count()
            .get(AggregateSource.SERVER).await()
            .count
        followers to following
    }.onFailure { Log.e(TAG, "followCounts($uid): ${it.message}") }

    suspend fun follow(me: String, target: String): Result<Unit> = runCatching {
        require(me.isNotBlank() && target.isNotBlank()) { "uids vacíos" }
        require(me != target) { "No te puedes seguir a ti mismo" }
        db.collection(FOLLOWING)
            .document(FollowEdge.edgeId(me, target))
            .set(
                mapOf(
                    "followerUid" to me,
                    "followedUid" to target,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        Unit
    }.onFailure { Log.e(TAG, "follow($me->$target): ${it.message}") }

    suspend fun unfollow(me: String, target: String): Result<Unit> = runCatching {
        db.collection(FOLLOWING)
            .document(FollowEdge.edgeId(me, target))
            .delete().await()
        Unit
    }.onFailure { Log.e(TAG, "unfollow($me->$target): ${it.message}") }

    // ------------------------------------------------------------------------
    // Feed (D2.3): hitos + posts; chunks de whereIn + merge en cliente
    // ------------------------------------------------------------------------

    /**
     * Página del feed de los uids dados (máx. 30 por la disyunción). Orden
     * descendente por createdAt; el encadenado de páginas lo decide la UI.
     */
    suspend fun feedPage(
        authorUids: List<String>,
        limit: Long = FeedQueryPlanner.PAGE_SIZE.toLong()
    ): Result<List<FeedEntry>> = runCatching {
        if (authorUids.isEmpty()) return@runCatching emptyList()
        db.collection(FEED)
            .whereIn("authorUid", authorUids.take(MAX_FEED_CHUNK))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
            .documents.mapNotNull { doc -> doc.data?.let { FeedEntry.fromMap(doc.id, it) } }
    }.onFailure { Log.e(TAG, "feedPage(${authorUids.size} uids): ${it.message}") }

    /**
     * Feed en vivo (snapshot) de un chunk de uids, con autor resuelto. Con más
     * de MAX_FEED_CHUNK uids seguidos, escucha el primer chunk; el resto de
     * autores entra por feedPage (pull-to-refresh). Limitación documentada
     * del plan sin Functions (D2.4).
     */
    fun observeFeed(
        authorUids: List<String>,
        limit: Long = FeedQueryPlanner.PAGE_SIZE.toLong()
    ): Flow<Result<List<FeedEntryPair>>> = callbackFlow {
        if (authorUids.isEmpty()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        val chunk = authorUids.take(MAX_FEED_CHUNK)
        val registration = db.collection(FEED)
            .whereIn("authorUid", chunk)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { FeedEntry.fromMap(doc.id, it) }
                }.orEmpty()
                // La resolución de autores es suspend: se lanza en el scope del flow.
                launch {
                    val resolved = entries.map { entry -> async { entry.withAuthor() } }
                        .awaitAll()
                    trySend(Result.success(resolved))
                }
            }
        awaitClose { registration.remove() }
    }

    private suspend fun FeedEntry.withAuthor(): FeedEntryPair {
        val author = runCatching { resolveAuthor(authorUid) }.getOrNull()
        return FeedEntryPair(
            id = id,
            entry = this,
            authorName = author?.first.orEmpty(),
            authorUsername = author?.second.orEmpty()
        )
    }

    /** (nombre, username) de un perfil; fallo = vacíos. */
    private suspend fun resolveAuthor(uid: String): Pair<String, String> {
        val snapshot = db.collection(USERS).document(uid).get().await()
        return snapshot.getString("nameSurname").orEmpty() to snapshot.getString("username").orEmpty()
    }

    /** Publica un hito o un post (las reglas validan tipo, autoría y longitud). */
    suspend fun publishEntry(entry: FeedEntry): Result<String> = runCatching {
        val data = mutableMapOf<String, Any?>(
            "type" to entry.type,
            "authorUid" to entry.authorUid,
            "text" to entry.text,
            "likesCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (entry.gameId.isNotBlank()) data["gameId"] = entry.gameId
        if (entry.gameName.isNotBlank()) data["gameName"] = entry.gameName
        if (entry.gameCover.isNotBlank()) data["gameCover"] = entry.gameCover
        if (entry.milestoneType.isNotBlank()) data["milestoneType"] = entry.milestoneType
        if (entry.rating != null) data["rating"] = entry.rating

        val id = if (entry.isMilestone) {
            MilestonePlanner.milestoneId(entry.authorUid, entry.milestoneType, entry.gameId)
        } else {
            db.collection(FEED).document().id
        }
        db.collection(FEED).document(id).set(data).await()
        id
    }.onFailure { Log.e(TAG, "publishEntry: ${it.message}") }

    /** Me gusta idempotente (el like ES el uid); contador por increment. */
    suspend fun setLike(entryId: String, uid: String, liked: Boolean): Result<Unit> = runCatching {
        val likeRef = db.collection(FEED).document(entryId).collection(LIKES).document(uid)
        val entryRef = db.collection(FEED).document(entryId)
        if (liked) {
            likeRef.set(mapOf("at" to FieldValue.serverTimestamp())).await()
            entryRef.update("likesCount", FieldValue.increment(1)).await()
        } else {
            likeRef.delete().await()
            entryRef.update("likesCount", FieldValue.increment(-1)).await()
        }
        Unit
    }.onFailure { Log.e(TAG, "setLike($entryId, $uid, $liked): ${it.message}") }

    /** ¿Le di me gusta? (1 lectura) */
    suspend fun hasLiked(entryId: String, uid: String): Result<Boolean> = runCatching {
        db.collection(FEED).document(entryId).collection(LIKES).document(uid)
            .get().await().exists()
    }.onFailure { Log.e(TAG, "hasLiked($entryId): ${it.message}") }

    /** Mapa entryId → ¿me gusta? para una página del feed (lecturas en paralelo). */
    suspend fun likedByMe(entryIds: List<String>, uid: String): Result<Map<String, Boolean>> =
        runCatching {
            if (entryIds.isEmpty()) return@runCatching emptyMap()
            coroutineScope {
                entryIds.map { id -> async { id to hasLiked(id, uid).getOrDefault(false) } }
                    .awaitAll().toMap()
            }
        }.onFailure { Log.e(TAG, "likedByMe: ${it.message}") }

    // ------------------------------------------------------------------------
    // Moderación (D2.5)
    // ------------------------------------------------------------------------

    /** Uids bloqueados por `uid`. */
    suspend fun blockedUids(uid: String): Result<Set<String>> = runCatching {
        db.collection(BLOCKS).document(uid).collection(PEOPLE).get().await()
            .documents.map { it.id }.toSet()
    }.onFailure { Log.e(TAG, "blockedUids($uid): ${it.message}") }

    suspend fun block(me: String, target: String): Result<Unit> = runCatching {
        require(me != target) { "No te puedes bloquear a ti mismo" }
        db.collection(BLOCKS).document(me).collection(PEOPLE).document(target)
            .set(mapOf("at" to FieldValue.serverTimestamp())).await()
        Unit
    }.onFailure { Log.e(TAG, "block($me->$target): ${it.message}") }

    suspend fun unblock(me: String, target: String): Result<Unit> = runCatching {
        db.collection(BLOCKS).document(me).collection(PEOPLE).document(target)
            .delete().await()
        Unit
    }.onFailure { Log.e(TAG, "unblock($me->$target): ${it.message}") }

    suspend fun report(
        reporterUid: String,
        targetUid: String,
        reason: String,
        detail: String = ""
    ): Result<Unit> = runCatching {
        db.collection(REPORTS).add(
            mapOf(
                "reporterUid" to reporterUid,
                "targetUid" to targetUid,
                "reason" to reason,
                "detail" to detail,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }.onFailure { Log.e(TAG, "report: ${it.message}") }

    // ------------------------------------------------------------------------
    // Búsqueda por username (D2.4: sin Blaze — índice + prefijo)
    // ------------------------------------------------------------------------

    /**
     * Búsqueda por prefijo INSENSIBLE a mayúsculas: rango sobre los documentos
     * alias en minúsculas (`~username`) del índice, escritos por UserRepository
     * junto al original. Mínimo MIN_PREFIX caracteres.
     */
    suspend fun searchByUsername(prefix: String): Result<List<UserProfile>> = runCatching {
        val clean = prefix.trim().lowercase()
        require(clean.length >= MIN_PREFIX) { "Escribe al menos $MIN_PREFIX caracteres" }
        val end = clean.substring(0, clean.length - 1) + (clean.last() + 1)
        db.collection(USERNAMES)
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), usernameAliasId(clean))
            .whereLessThan(FieldPath.documentId(), usernameAliasId(end))
            .limit(20)
            .get().await()
            .documents.mapNotNull { doc ->
                val targetUid = doc.getString("uid") ?: return@mapNotNull null
                db.collection(USERS).document(targetUid).get().await().data
                    ?.let { UserProfile.fromMap(it) }
            }
    }.onFailure { Log.e(TAG, "searchByUsername($prefix): ${it.message}") }

    // ------------------------------------------------------------------------
    // Listas curadas (T2.6): dos colecciones (regla CA2.4 por construcción)
    // ------------------------------------------------------------------------

    private fun listsCollection(uid: String, isPublic: Boolean) =
        db.collection(USERS).document(uid)
            .collection(if (isPublic) LISTS else LISTS_PRIVATE)

    /** Listas de un usuario. Para terceros: onlyPublic=true (no toca la privada). */
    suspend fun listsOf(uid: String, onlyPublic: Boolean = false): Result<List<GameList>> =
        runCatching {
            val publicLists = listsCollection(uid, isPublic = true).get().await()
                .documents.mapNotNull { doc ->
                    doc.data?.let { GameList.fromMap(doc.id, uid, it) }
                }
            if (onlyPublic) return@runCatching publicLists
            val privateLists = listsCollection(uid, isPublic = false).get().await()
                .documents.mapNotNull { doc ->
                    doc.data?.let { GameList.fromMap(doc.id, uid, it) }
                }
            (publicLists + privateLists).sortedByDescending { it.createdAt }
        }.onFailure { Log.e(TAG, "listsOf($uid): ${it.message}") }

    suspend fun list(uid: String, listId: String): Result<GameList> = runCatching {
        val publicDoc = listsCollection(uid, isPublic = true).document(listId).get().await()
        if (publicDoc.exists()) {
            return@runCatching GameList.fromMap(publicDoc.id, uid, publicDoc.data)
        }
        val privateDoc = listsCollection(uid, isPublic = false).document(listId).get().await()
        require(privateDoc.exists()) { "La lista no existe" }
        GameList.fromMap(privateDoc.id, uid, privateDoc.data)
    }.onFailure { Log.e(TAG, "list($uid/$listId): ${it.message}") }

    suspend fun saveList(uid: String, list: GameList): Result<Unit> = runCatching {
        // CA2.4 por construcción: lo privado NO se copia nunca a la colección pública.
        val data = mapOf(
            "name" to list.name,
            "description" to list.description,
            "isPublic" to list.isPublic,
            "gameIds" to list.gameIds,
            "createdAt" to (if (list.createdAt == 0L) FieldValue.serverTimestamp() else list.createdAt)
        )
        val collection = listsCollection(uid, isPublic = list.isPublic)
        val id = list.id.ifBlank { collection.document().id }
        collection.document(id).set(data).await()
        Unit
    }.onFailure { Log.e(TAG, "saveList($uid): ${it.message}") }

    suspend fun deleteList(uid: String, listId: String, isPublic: Boolean = true): Result<Unit> =
        runCatching {
            listsCollection(uid, isPublic).document(listId).delete().await()
            Unit
        }.onFailure { Log.e(TAG, "deleteList($uid/$listId): ${it.message}") }
}
