package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.model.ChatMessage
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.data.repository.UserRepository
import es.androidtfm.gamevision.datastore.RecentGame
import es.androidtfm.gamevision.datastore.RecentGamesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (actualizado 28/09/2026 — F0-B/B2: biblioteca nueva y
 * historial local; los juegos ya no viven en las listas antiguas por email).
 * Descripción:
 *
 * Fachada de los datos de juego, amigos y mensajes del usuario. El perfil y la
 * identidad ya NO están aquí: viven en UserViewModel (SSOT).
 *
 * Todos los métodos devuelven Result, así que la UI puede informar de los
 * fallos en vez de fallar en silencio como antes.
 */

@HiltViewModel
class DDBBViewModel @Inject constructor(
    private val repository: UserRepository,
    private val libraryRepository: LibraryRepository,
    private val recentGamesStore: RecentGamesStore
) : ViewModel() {

    // ------------------------------------------------------------------------
    // Biblioteca (F0-B): users/{uid}/library|logs|sessions|stats
    // ------------------------------------------------------------------------

    /** Flujo en vivo de la biblioteca del usuario (SSOT de sus fichas). */
    fun observeLibrary(uid: String): Flow<Result<List<LibraryEntry>>> =
        libraryRepository.observeLibrary(uid)

    /** Añade una ficha nueva a la biblioteca. */
    suspend fun addToLibrary(uid: String, entry: LibraryEntry): Result<Unit> =
        libraryRepository.addGame(uid, entry)

    /** Cambia el estado de una ficha (ajusta contadores en el mismo lote). */
    suspend fun updateStatus(
        uid: String,
        gameId: String,
        from: LibraryStatus,
        to: LibraryStatus
    ): Result<Unit> = libraryRepository.updateStatus(uid, gameId, from, to)

    /** Marca o desmarca el favorito. */
    suspend fun setFavorite(uid: String, gameId: String, favorite: Boolean): Result<Unit> =
        libraryRepository.updateFavorite(uid, gameId, favorite)

    /** Elimina una ficha (se pasa la ficha en vivo para ajustar contadores). */
    suspend fun removeFromLibrary(uid: String, entry: LibraryEntry): Result<Unit> =
        libraryRepository.removeGame(uid, entry)

    // ------------------------------------------------------------------------
    // Historial local (recientes) — vive en el dispositivo
    // ------------------------------------------------------------------------

    /** Últimos juegos vistos (local; no viaja entre dispositivos). */
    val recentGames: Flow<List<RecentGame>> = recentGamesStore.recentGames

    /** Registra un juego en el historial local. */
    fun addRecentGame(game: RecentGame) {
        viewModelScope.launch { recentGamesStore.add(game) }
    }

    // ------------------------------------------------------------------------
    // Amigos (rutas heredadas por email hasta F2)
    // ------------------------------------------------------------------------

    suspend fun addFriend(email: String, friendEmail: String): Result<Unit> =
        repository.addFriend(email, friendEmail)

    suspend fun removeFriend(email: String, friendEmail: String): Result<Unit> =
        repository.removeFriend(email, friendEmail)

    suspend fun getFriends(email: String): Result<List<Friend>> = repository.getFriends(email)

    /** true si existe un perfil con ese email (para añadir amigos). */
    suspend fun profileExists(email: String): Result<Boolean> = repository.profileExists(email)

    // ------------------------------------------------------------------------
    // Mensajes (rutas heredadas por email hasta F2)
    // ------------------------------------------------------------------------

    suspend fun publishMessage(email: String, message: String, time: String): Result<Unit> =
        repository.publishMessage(email, message, time)

    suspend fun deleteMessage(email: String, messageId: String): Result<Unit> =
        repository.deleteMessage(email, messageId)

    suspend fun getMessages(email: String): Result<List<ChatMessage>> =
        repository.getMessages(email)
}
