package es.androidtfm.gamevision.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.repository.SocialRepository
import es.androidtfm.gamevision.data.social.FeedEntryPair
import es.androidtfm.gamevision.data.social.FeedQueryPlanner
import es.androidtfm.gamevision.data.social.MilestonePlanner
import es.androidtfm.gamevision.data.social.PostComposer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 30/09/2026 (Fase 2 — Social)
 *
 * Puente UI ↔ SocialRepository: feed en vivo, seguir, moderación, búsqueda
 * y listas. Estado observable con StateFlow; operaciones suspend lanzadas
 * en viewModelScope.
 */
@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    companion object {
        private const val TAG = "SocialViewModel"
        private const val USERS = "users"
    }

    // ------------------------------------------------------------- feed (D2.3)

    private val _feed = MutableStateFlow<Result<List<FeedEntryPair>>?>(null)
    val feed: StateFlow<Result<List<FeedEntryPair>>?> = _feed.asStateFlow()

    private var feedJob: Job? = null
    private var lastUids: List<String> = emptyList()

    /** D2.5: uids bloqueados por el usuario actual (para filtrar el feed). */
    private val _blockedUids = MutableStateFlow<Set<String>>(emptySet())
    val blockedUids: StateFlow<Set<String>> = _blockedUids.asStateFlow()

    /** Recarga la lista de bloqueados (al abrir Social y tras bloquear). */
    fun refreshBlocked(me: String) {
        if (me.isBlank()) return
        viewModelScope.launch {
            _blockedUids.value = socialRepository.blockedUids(me).getOrDefault(emptySet())
        }
    }

    /** Al desbloquear desde otra superficie, refresca la lista local. */
    fun clearBlockedCache() { _blockedUids.value = emptySet() }
    private var myUid: String? = null

    /**
     * Feed EN VIVO (snapshot listener) de los uids seguidos + el propio, con
     * autor resuelto y marca de "me gusta". CA2.2: se actualiza sin recargar.
     */
    fun observeFeed(authorUids: List<String>, currentUid: String?) {
        lastUids = authorUids
        myUid = currentUid
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            socialRepository.observeFeed(authorUids).collect { result ->
                _feed.value = result.mapCatching { pairs ->
                    val likes = socialRepository
                        .likedByMe(pairs.map { it.id }, currentUid.orEmpty())
                        .getOrDefault(emptyMap())
                    FeedQueryPlanner.sortFeedEntries(
                        FeedQueryPlanner.filterBlocked(
                            pairs.map { p ->
                                p.copy(entry = p.entry.copy(likedByMe = likes[p.id] ?: false))
                            },
                            _blockedUids.value
                        )
                    )
                }
            }
        }
    }

    /** Re-observa con los últimos uids (tras seguir/dejar de seguir o gustar). */
    fun refreshFeed() = observeFeed(lastUids, myUid)

    /** Publica un post del usuario (D2.3: 1–280; valida en cliente y reglas). */
    fun publishPost(uid: String, text: String, onDone: (Result<String>) -> Unit) {
        when (val validity = PostComposer.validate(text)) {
            is PostComposer.Validity.Invalid ->
                onDone(Result.failure(IllegalArgumentException(validity.reason)))
            is PostComposer.Validity.Ok -> viewModelScope.launch {
                onDone(
                    socialRepository.publishEntry(
                        FeedEntry(type = FeedEntry.TYPE_POST, authorUid = uid, text = text)
                    )
                )
            }
        }
    }

    /** Me gusta / quita me gusta; el refresh lo re-pinta desde el snapshot. */
    fun toggleLike(entry: FeedEntry, uid: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onDone(socialRepository.setLike(entry.id, uid, liked = !entry.likedByMe))
        }
    }

    // ------------------------------------------------------------- seguir (D2.1)

    private val _following = MutableStateFlow<Set<String>>(emptySet())
    val following: StateFlow<Set<String>> = _following.asStateFlow()

    private val _followers = MutableStateFlow<Set<String>>(emptySet())
    val followers: StateFlow<Set<String>> = _followers.asStateFlow()

    fun refreshFollowEdges(uid: String) {
        viewModelScope.launch {
            socialRepository.followingIds(uid).onSuccess { _following.value = it }
            socialRepository.followerIds(uid).onSuccess { _followers.value = it }
        }
    }

    fun follow(me: String, target: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onDone(socialRepository.follow(me, target).onSuccess {
                _following.value = _following.value + target
            })
        }
    }

    fun unfollow(me: String, target: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onDone(socialRepository.unfollow(me, target).onSuccess {
                _following.value = _following.value - target
            })
        }
    }

    private val _publicLibrary = MutableStateFlow<Result<List<LibraryEntry>>?>(null)
    val publicLibrary: StateFlow<Result<List<LibraryEntry>>?> = _publicLibrary.asStateFlow()

    /** Biblioteca pública del perfil visitado (reglas: solo si !isPrivate). */
    fun loadPublicLibrary(uid: String) {
        viewModelScope.launch { _publicLibrary.value = socialRepository.publicLibrary(uid) }
    }

    fun clearPublicLibrary() {
        _publicLibrary.value = null
    }

    // ------------------------------------------------------------- moderación (D2.5)

    fun block(me: String, target: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onDone(socialRepository.block(me, target)) }
    }

    fun unblock(me: String, target: String, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onDone(socialRepository.unblock(me, target)) }
    }

    fun blocked(me: String, onDone: (Set<String>) -> Unit) {
        viewModelScope.launch { onDone(socialRepository.blockedUids(me).getOrDefault(emptySet())) }
    }

    fun report(me: String, targetUid: String, reason: String, detail: String = "") {
        viewModelScope.launch { socialRepository.report(me, targetUid, reason, detail) }
    }

    // ------------------------------------------------------------- perfil público (D2.2/T2.4)

    private val _publicProfile = MutableStateFlow<UserProfile?>(null)
    val publicProfile: StateFlow<UserProfile?> = _publicProfile.asStateFlow()

    /** true cuando la lectura del perfil público se deniega (privado o bloqueo). */
    private val _publicProfileError = MutableStateFlow(false)
    val publicProfileError: StateFlow<Boolean> = _publicProfileError.asStateFlow()

    private val _profileCounts = MutableStateFlow(0L to 0L)
    val profileCounts: StateFlow<Pair<Long, Long>> = _profileCounts.asStateFlow()

    /** Carga el perfil público de `uid` + sus contadores (seguidores, seguidos). */
    fun loadPublicProfile(uid: String) {
        _publicProfileError.value = false
        viewModelScope.launch {
            runCatching {
                db.collection(USERS).document(uid).get().await()
                    .data?.let { UserProfile.fromMap(it) }
            }.onSuccess { _publicProfile.value = it }
                .onFailure {
                    // Denegado por privacidad o bloqueo (D2.2/D2.5): estado explícito, sin bucle.
                    Log.e(TAG, "loadPublicProfile($uid): ${it.message}")
                    _publicProfile.value = null
                    _publicProfileError.value = true
                }
        }
        viewModelScope.launch {
            _profileCounts.value = socialRepository.followCounts(uid).getOrDefault(0L to 0L)
        }
    }

    fun clearPublicProfile() {
        _publicProfile.value = null
        _publicProfileError.value = false
        _profileCounts.value = 0L to 0L
    }

    /** true si `me` sigue a `target` (lectura puntual, para pintar el botón del perfil). */
    suspend fun isFollowing(me: String, target: String): Boolean =
        socialRepository.followingIds(me).getOrDefault(emptySet()).contains(target)

    // ------------------------------------------------------------- búsqueda (D2.4)

    private val _searchResults = MutableStateFlow<Result<List<Pair<String, UserProfile>>>?>(null)
    val searchResults: StateFlow<Result<List<Pair<String, UserProfile>>>?> = _searchResults.asStateFlow()

    fun search(prefix: String) {
        viewModelScope.launch {
            _searchResults.value = socialRepository.searchByUsername(prefix)
        }
    }

    // ------------------------------------------------------------- listas (T2.6)

    private val _lists = MutableStateFlow<Result<List<GameList>>?>(null)
    val lists: StateFlow<Result<List<GameList>>?> = _lists.asStateFlow()

    fun loadLists(uid: String, onlyPublic: Boolean = false) {
        viewModelScope.launch { _lists.value = socialRepository.listsOf(uid, onlyPublic) }
    }

    fun saveList(uid: String, list: GameList, onDone: (Result<Unit>) -> Unit) {
        viewModelScope.launch { onDone(socialRepository.saveList(uid, list)) }
    }

    // ------------------------------------------------------------- hitos (D2.3)

    /**
     * Publica el hito de una acción (completado / reseña / lista pública).
     * Determinista: re-completar el mismo juego no duplica entrada.
     */
    fun publishMilestone(
        uid: String,
        milestoneType: String,
        gameId: String,
        gameName: String,
        gameCover: String,
        rating: Double? = null
    ) {
        viewModelScope.launch {
            socialRepository.publishEntry(
                FeedEntry(
                    type = FeedEntry.TYPE_MILESTONE,
                    authorUid = uid,
                    milestoneType = milestoneType,
                    gameId = gameId,
                    gameName = gameName,
                    gameCover = gameCover,
                    rating = rating?.toFloat()
                )
            ).onFailure { Log.e(TAG, "publishMilestone: ${it.message}") }
        }
    }

    /** Atajo: hito de "game_completed" (se llama desde el cambio de estado). */
    fun publishCompletedMilestone(
        uid: String,
        gameId: String,
        gameName: String,
        gameCover: String
    ) {
        val milestoneType = MilestonePlanner.forAction("game_completed", gameId) ?: return
        publishMilestone(
            uid = uid,
            milestoneType = milestoneType,
            gameId = gameId,
            gameName = gameName,
            gameCover = gameCover
        )
    }

    /** Atajo: hito de reseña (texto nuevo o primera reseña del juego). */
    fun publishReviewMilestone(
        uid: String,
        gameId: String,
        gameName: String,
        gameCover: String,
        rating: Double?
    ) {
        val milestoneType = MilestonePlanner.forAction("review_published", gameId) ?: return
        publishMilestone(
            uid = uid,
            milestoneType = milestoneType,
            gameId = gameId,
            gameName = gameName,
            gameCover = gameCover,
            rating = rating
        )
    }
}
