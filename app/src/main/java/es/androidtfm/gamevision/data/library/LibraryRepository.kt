package es.androidtfm.gamevision.data.library

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Repositorio de la biblioteca (F0-B/T0.9), según la propuesta aprobada el 28/09/2026:
 *
 *  - Estructura: users/{uid}/{library|logs|sessions} + stats/summary.
 *  - TODOS los métodos devuelven Result (nunca se traga un error en silencio).
 *  - Las escrituras que tocan contadores van en LOTE (batch): funciona offline;
 *    las transacciones no, y por eso NO se usan en el camino caliente.
 *  - Los contadores se mantienen con FieldValue.increment (atómico, offline-safe).
 */

@Singleton
class LibraryRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "LibraryRepository"
        private const val USERS = "users"
        private const val LIBRARY = "library"
        private const val LOGS = "logs"
        private const val SESSIONS = "sessions"
        private const val STATS = "stats"
        private const val STATS_DOC = "summary"
    }

    private fun userDoc(uid: String) = db.collection(USERS).document(uid)
    private fun libraryCol(uid: String) = userDoc(uid).collection(LIBRARY)
    private fun logsCol(uid: String) = userDoc(uid).collection(LOGS)
    private fun sessionsCol(uid: String) = userDoc(uid).collection(SESSIONS)
    private fun statsDoc(uid: String) = userDoc(uid).collection(STATS).document(STATS_DOC)

    // ------------------------------------------------------------------------
    // Lectura en vivo
    // ------------------------------------------------------------------------

    /** Flujo en vivo de la biblioteca (SSOT del usuario para sus fichas). */
    fun observeLibrary(uid: String): Flow<Result<List<LibraryEntry>>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        val registration = libraryCol(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> trySend(Result.failure(error))
                snapshot != null -> trySend(
                    Result.success(snapshot.documents.mapNotNull { it.toLibraryEntryOrNull() })
                )
            }
        }
        awaitClose { registration.remove() }
    }

    /** Flujo en vivo de los agregados (stats/summary). */
    fun observeStats(uid: String): Flow<Result<LibraryStats>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(Result.success(LibraryStats()))
            close()
            return@callbackFlow
        }
        val registration = statsDoc(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> trySend(Result.failure(error))
                snapshot != null && snapshot.exists() ->
                    trySend(Result.success(snapshot.toLibraryStats()))
                else -> trySend(Result.success(LibraryStats()))
            }
        }
        awaitClose { registration.remove() }
    }

    /** Últimas sesiones del diario (más recientes primero). */
    fun observeSessions(uid: String, limit: Long = 200): Flow<Result<List<PlaySession>>> =
        callbackFlow {
            if (uid.isBlank()) {
                trySend(Result.success(emptyList()))
                close()
                return@callbackFlow
            }
            val query = sessionsCol(uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
            val registration = query.addSnapshotListener { snapshot, error ->
                when {
                    error != null -> trySend(Result.failure(error))
                    snapshot != null -> trySend(
                        Result.success(snapshot.documents.mapNotNull { it.toSessionOrNull() })
                    )
                }
            }
            awaitClose { registration.remove() }
        }

    // ------------------------------------------------------------------------
    // Fichas (library)
    // ------------------------------------------------------------------------

    /**
     * Añade un juego a la biblioteca y ajusta los contadores, todo en un lote.
     * Si el juego ya existía, se sobrescribe la ficha (uso previsto: añadir solo si no está).
     */
    suspend fun addGame(uid: String, entry: LibraryEntry): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        val batch = db.batch()
        batch.set(libraryCol(uid).document(entry.gameId), entry.toMap())
        batch.set(
            statsDoc(uid),
            mapOf(
                "gamesTotal" to FieldValue.increment(1L),
                "gamesByStatus.${entry.status.value}" to FieldValue.increment(1L),
                "lastActiveAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error añadiendo juego ${entry.gameId}: ${it.message}") }

    /**
     * Cambia el estado de una ficha (y sus contadores) en un lote.
     * `from` es el estado actual conocido por la UI (evita lecturas extra).
     */
    suspend fun updateStatus(
        uid: String,
        gameId: String,
        from: LibraryStatus,
        to: LibraryStatus
    ): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        if (from == to) return@runCatching Unit

        val update = mutableMapOf<String, Any>(
            "status" to to.value,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        when (to) {
            LibraryStatus.PLAYING -> update["startedAt"] = FieldValue.serverTimestamp()
            LibraryStatus.COMPLETED, LibraryStatus.COLLECTED ->
                update["finishedAt"] = FieldValue.serverTimestamp()
            else -> Unit
        }

        val batch = db.batch()
        batch.update(libraryCol(uid).document(gameId), update)
        batch.set(
            statsDoc(uid),
            mapOf(
                "gamesByStatus.${from.value}" to FieldValue.increment(-1L),
                "gamesByStatus.${to.value}" to FieldValue.increment(1L),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error cambiando estado de $gameId: ${it.message}") }

    /**
     * Actualiza la nota (o la borra con `rating = null`) y ajusta la media agregada.
     * `previous` es la nota actual conocida por la UI.
     */
    suspend fun updateRating(
        uid: String,
        gameId: String,
        previous: Double?,
        rating: Double?
    ): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        val delta = (rating ?: 0.0) - (previous ?: 0.0)
        val countDelta = (if (rating != null) 1L else 0L) - (if (previous != null) 1L else 0L)

        val batch = db.batch()
        batch.update(
            libraryCol(uid).document(gameId),
            mapOf(
                "rating" to (rating ?: FieldValue.delete()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            statsDoc(uid),
            mapOf(
                "ratingSum" to FieldValue.increment(delta),
                "ratingCount" to FieldValue.increment(countDelta),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error actualizando nota de $gameId: ${it.message}") }

    /** Actualiza la reseña (o la borra con `review = null`). */
    suspend fun updateReview(uid: String, gameId: String, review: String?): Result<Unit> =
        runCatching {
            require(uid.isNotEmpty()) { "Usuario no autenticado" }
            libraryCol(uid).document(gameId).update(
                mapOf(
                    "review" to (review ?: FieldValue.delete()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Unit
        }.onFailure { Log.e(TAG, "Error actualizando reseña de $gameId: ${it.message}") }

    /** Marca o desmarca el favorito. */
    suspend fun updateFavorite(uid: String, gameId: String, favorite: Boolean): Result<Unit> =
        runCatching {
            require(uid.isNotEmpty()) { "Usuario no autenticado" }
            libraryCol(uid).document(gameId).update(
                mapOf(
                    "favorite" to favorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Unit
        }.onFailure { Log.e(TAG, "Error actualizando favorito de $gameId: ${it.message}") }

    /**
     * Elimina una ficha y ajusta contadores. Se pasa la ficha completa (la UI la tiene
     * en vivo) para poder deshacer sus contribuciones sin lecturas extra.
     * Nota: las partidas y sesiones del juego no se borran aquí (borrado en cascada
     * se decide en B3/F1).
     */
    suspend fun removeGame(uid: String, entry: LibraryEntry): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        val batch = db.batch()
        batch.delete(libraryCol(uid).document(entry.gameId))
        val stats = mutableMapOf<String, Any>(
            "gamesTotal" to FieldValue.increment(-1L),
            "gamesByStatus.${entry.status.value}" to FieldValue.increment(-1L),
            "minutesTotal" to FieldValue.increment(-entry.minutesTotal.toLong()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (entry.rating != null) {
            stats["ratingSum"] = FieldValue.increment(-entry.rating)
            stats["ratingCount"] = FieldValue.increment(-1L)
        }
        batch.set(statsDoc(uid), stats, SetOptions.merge())
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error eliminando juego ${entry.gameId}: ${it.message}") }

    // ------------------------------------------------------------------------
    // Partidas (logs) y sesiones (sessions)
    // ------------------------------------------------------------------------

    /** Crea una partida. Si lleva minutos manuales, los suma a los contadores (en lote). */
    suspend fun createLog(uid: String, log: GameLog): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        val batch = db.batch()
        batch.set(logsCol(uid).document(), log.toMap())
        val minutes = log.minutes
        if (minutes != null && minutes > 0) {
            batch.set(
                statsDoc(uid),
                mapOf(
                    "minutesTotal" to FieldValue.increment(minutes.toLong()),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            batch.set(
                libraryCol(uid).document(log.gameId),
                mapOf("minutesTotal" to FieldValue.increment(minutes.toLong())),
                SetOptions.merge()
            )
        }
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error creando partida de ${log.gameId}: ${it.message}") }

    /**
     * Apunta una sesión del diario en un lote: sesión + contadores (total y por juego).
     * Es la escritura más frecuente de la app; offline-safe por diseño.
     */
    suspend fun addSession(uid: String, session: PlaySession): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        require(session.minutes > 0) { "La sesión debe tener minutos positivos" }
        val batch = db.batch()
        batch.set(sessionsCol(uid).document(), session.toMap())
        batch.set(
            statsDoc(uid),
            mapOf(
                "sessionsTotal" to FieldValue.increment(1L),
                "minutesTotal" to FieldValue.increment(session.minutes.toLong()),
                "lastActiveAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        batch.set(
            libraryCol(uid).document(session.gameId),
            mapOf("minutesTotal" to FieldValue.increment(session.minutes.toLong())),
            SetOptions.merge()
        )
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error apuntando sesión de ${session.gameId}: ${it.message}") }
}

/* --- Mapeo dominio <-> Firestore (el resto de la app no conoce los mapas) --- */

private fun LibraryEntry.toMap(): Map<String, Any?> = mapOf(
    "gameId" to gameId,
    "status" to status.value,
    "rating" to rating,
    "review" to review,
    "favorite" to favorite,
    "lastPlatform" to lastPlatform,
    "addedAt" to FieldValue.serverTimestamp(),
    "startedAt" to startedAt?.let { Timestamp(Date(it)) },
    "finishedAt" to finishedAt?.let { Timestamp(Date(it)) },
    "name" to name,
    "coverUrl" to coverUrl,
    "released" to released,
    "genres" to genres,
    "minutesTotal" to minutesTotal,
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun DocumentSnapshot.toLibraryEntryOrNull(): LibraryEntry? {
    val gameId = getString("gameId") ?: id
    val status = LibraryStatus.fromValue(getString("status")) ?: return null
    return LibraryEntry(
        gameId = gameId,
        status = status,
        rating = getDouble("rating"),
        review = getString("review"),
        favorite = getBoolean("favorite") ?: false,
        lastPlatform = getString("lastPlatform"),
        addedAt = getTimestamp("addedAt")?.toDate()?.time,
        startedAt = getTimestamp("startedAt")?.toDate()?.time,
        finishedAt = getTimestamp("finishedAt")?.toDate()?.time,
        name = getString("name").orEmpty(),
        coverUrl = getString("coverUrl"),
        released = getString("released").orEmpty(),
        genres = (get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        minutesTotal = (getLong("minutesTotal") ?: 0L).toInt()
    )
}

private fun GameLog.toMap(): Map<String, Any?> = mapOf(
    "gameId" to gameId,
    "runIndex" to runIndex,
    "startedOn" to startedOn?.let { Timestamp(Date(it)) },
    "finishedOn" to finishedOn?.let { Timestamp(Date(it)) },
    "platform" to platform,
    "minutes" to minutes,
    "note" to note,
    "createdAt" to FieldValue.serverTimestamp()
)

private fun DocumentSnapshot.toSessionOrNull(): PlaySession? {
    val gameId = getString("gameId") ?: return null
    val date = getTimestamp("date")?.toDate()?.time ?: return null
    return PlaySession(
        id = id,
        gameId = gameId,
        logId = getString("logId"),
        date = date,
        minutes = (getLong("minutes") ?: 0L).toInt(),
        note = getString("note")
    )
}

private fun PlaySession.toMap(): Map<String, Any?> = mapOf(
    "gameId" to gameId,
    "logId" to logId,
    "date" to Timestamp(Date(date)),
    "minutes" to minutes,
    "note" to note,
    "createdAt" to FieldValue.serverTimestamp()
)

private fun DocumentSnapshot.toLibraryStats(): LibraryStats {
    val byStatus = (get("gamesByStatus") as? Map<*, *>)
        ?.mapNotNull { (key, value) ->
            val k = key as? String ?: return@mapNotNull null
            val v = (value as? Long)?.toInt() ?: return@mapNotNull null
            k to v
        }?.toMap() ?: emptyMap()
    return LibraryStats(
        gamesTotal = (getLong("gamesTotal") ?: 0L).toInt(),
        gamesByStatus = byStatus,
        minutesTotal = (getLong("minutesTotal") ?: 0L).toInt(),
        sessionsTotal = (getLong("sessionsTotal") ?: 0L).toInt(),
        ratingSum = getDouble("ratingSum") ?: 0.0,
        ratingCount = (getLong("ratingCount") ?: 0L).toInt(),
        lastActiveAt = getTimestamp("lastActiveAt")?.toDate()?.time
    )
}
