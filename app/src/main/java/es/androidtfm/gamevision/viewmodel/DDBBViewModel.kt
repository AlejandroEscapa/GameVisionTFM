package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.model.ChatMessage
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.data.repository.UserRepository
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (Frente 2/3 — Result en toda la capa de datos)
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
    private val repository: UserRepository
) : ViewModel() {

    /** Constructor sin argumentos para previews y usos manuales. */
    constructor() : this(UserRepository(FirebaseFirestore.getInstance()))

    // ---- Juegos ----

    suspend fun addGame(email: String, gameId: String, targetCollection: String): Result<Unit> =
        repository.addGame(email, gameId, targetCollection)

    suspend fun addGameToHistory(email: String, gameId: String): Result<Unit> =
        repository.addGameToHistory(email, gameId)

    suspend fun removePlayedGame(email: String, gameId: String): Result<Unit> =
        repository.removePlayedGame(email, gameId)

    suspend fun getUserGameIds(email: String, collectionName: String): Result<List<String>> =
        repository.getUserGameIds(email, collectionName)

    // ---- Amigos ----

    suspend fun addFriend(email: String, friendEmail: String): Result<Unit> =
        repository.addFriend(email, friendEmail)

    suspend fun removeFriend(email: String, friendEmail: String): Result<Unit> =
        repository.removeFriend(email, friendEmail)

    suspend fun getFriends(email: String): Result<List<Friend>> = repository.getFriends(email)

    /** true si existe un perfil con ese email (para añadir amigos). */
    suspend fun profileExists(email: String): Result<Boolean> = repository.profileExists(email)

    // ---- Mensajes ----

    suspend fun publishMessage(email: String, message: String, time: String): Result<Unit> =
        repository.publishMessage(email, message, time)

    suspend fun deleteMessage(email: String, messageId: String): Result<Unit> =
        repository.deleteMessage(email, messageId)

    suspend fun getMessages(email: String): Result<List<ChatMessage>> =
        repository.getMessages(email)
}
