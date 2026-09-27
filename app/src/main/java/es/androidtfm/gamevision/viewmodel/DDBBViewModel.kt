package es.androidtfm.gamevision.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 16/02/2025
 * Descripción:
 */

/**
 * ViewModel para la base de datos
 *
 * Expone el estado (datos de usuario, carga) y delega el acceso a datos en
 * [UserRepository]. El valor por defecto del repositorio mantiene la
 * construcción manual (previews/tests) sin Hilt.
 */

@HiltViewModel
class DDBBViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    /**
     * Constructor sin argumentos para previews y usos manuales (Hilt usa el primario).
     */
    constructor() : this(UserRepository(FirebaseFirestore.getInstance()))

    companion object {
        private const val TAG = "DDBBViewModel"
    }

    // Flujos de estado para los datos del usuario y el indicador de carga
    private val _userData = MutableStateFlow<HashMap<String, String>?>(null)
    val userData: StateFlow<HashMap<String, String>?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Obtiene los datos del usuario desde Firestore.
     */
    suspend fun fetchUserData(email: String) {
        _isLoading.value = true
        _userData.value = repository.getUser(email)
        _isLoading.value = false
    }

    /**
     * Registra un nuevo usuario en Firestore.
     */
    suspend fun registerUser(formFields: Map<String, String>): Boolean =
        repository.registerUser(formFields)

    /**
     * Actualiza los datos del usuario en Firestore.
     */
    suspend fun updateUser(email: String, updatedFields: HashMap<String, String?>) =
        repository.updateUser(email, updatedFields)

    /**
     * Recupera la URI de la imagen de perfil.
     */
    suspend fun recoverProfilePicture(email: String): Uri? =
        repository.recoverProfilePicture(email)

    /**
     * Actualiza la imagen de perfil del usuario en Firestore.
     */
    suspend fun updateUserProfilePicture(email: String, imageUri: Uri) =
        repository.updateUserProfilePicture(email, imageUri)

    /**
     * Verifica las credenciales de inicio de sesión.
     */
    suspend fun loginCheck(email: String, password: String): Boolean =
        repository.loginCheck(email, password)

    /**
     * Añade un juego a una colección específica del usuario.
     */
    suspend fun addGameToCollection(email: String, gameId: String, targetCollection: String) =
        repository.addGameToCollection(email, gameId, targetCollection)

    /**
     * Añade un juego al historial del usuario.
     */
    suspend fun addGameToHistory(email: String, gameId: String) =
        repository.addGameToHistory(email, gameId)

    /**
     * Elimina un juego de la colección "playedlist" del usuario.
     */
    suspend fun removeGameFromUser(email: String, gameId: String) =
        repository.removeGameFromUser(email, gameId)

    /**
     * Obtiene una lista de juegos de una colección específica del usuario.
     */
    suspend fun getUserGames(email: String, collectionName: String): List<Map<String, Any>> =
        repository.getUserGames(email, collectionName)

    /**
     * Añade un amigo a la lista de amigos del usuario.
     */
    suspend fun addFriend(email: String, friendEmail: String) =
        repository.addFriend(email, friendEmail)

    /**
     * Elimina un amigo de la lista de amigos del usuario.
     */
    fun removeFriend(email: String, friendEmail: String) =
        repository.removeFriend(email, friendEmail)

    /**
     * Obtiene la lista de amigos con correo y nombre de usuario.
     */
    suspend fun getFriendsList(email: String): List<Map<String, Any>> =
        repository.getFriendsList(email)

    /**
     * Publica un mensaje para el usuario.
     */
    suspend fun publishMessage(email: String, message: String, hora: String) =
        repository.publishMessage(email, message, hora)

    /**
     * Elimina un mensaje del usuario.
     */
    suspend fun deleteMessage(email: String, messageId: String) =
        repository.deleteMessage(email, messageId)

    /**
     * Obtiene los mensajes de la colección "messages" del usuario.
     */
    suspend fun getFriendMessages(email: String): List<Map<String, Any>> =
        repository.getFriendMessages(email)

    /**
     * Verifica si el correo electrónico ya existe en Firestore.
     */
    suspend fun checkEmailExists(email: String): Boolean =
        repository.checkEmailExists(email)

    /**
     * Cierra la sesión del usuario.
     */
    suspend fun logout() {
        _userData.value = null
        _isLoading.value = false
        FirebaseAuth.getInstance().signOut()
        Log.d(TAG, "Sesión cerrada y datos del usuario eliminados")
    }
}
