package es.androidtfm.gamevision.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import es.androidtfm.gamevision.data.model.ChatMessage
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (reescritura: Frente 2 — SSOT y propagación de errores)
 * Descripción: acceso a datos en Firestore.
 *
 * Reglas de esta capa:
 *  - TODOS los métodos devuelven Result: nunca se traga un error en silencio.
 *  - El perfil se expone como Flow en vivo (addSnapshotListener) para que la UI
 *    tenga una única fuente de verdad y se actualice sola.
 *  - El password NO existe aquí: vive en Firebase Authentication.
 *  - Los juegos ya NO viven aquí: la biblioteca está en LibraryRepository (F0-B).
 */

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "UserRepository"
        private const val USERS_COLLECTION = "users"
        private const val FRIENDS_COLLECTION = "friends"
        private const val MESSAGES_COLLECTION = "messages"
    }

    private fun userDoc(email: String) = db.collection(USERS_COLLECTION).document(email)

    // ------------------------------------------------------------------------
    // Perfil
    // ------------------------------------------------------------------------

    /**
     * Flujo en vivo del perfil: única fuente de verdad de los datos de usuario.
     * Se actualiza automáticamente ante cualquier cambio en Firestore.
     */
    fun observeProfile(email: String): Flow<Result<UserProfile>> = callbackFlow {
        if (email.isBlank()) {
            trySend(Result.success(UserProfile()))
            close()
            return@callbackFlow
        }
        val registration = userDoc(email).addSnapshotListener { snapshot, error ->
            when {
                error != null -> trySend(Result.failure(error))
                snapshot != null && snapshot.exists() ->
                    trySend(Result.success(UserProfile.fromMap(email, snapshot.data)))
                else -> trySend(Result.success(UserProfile(email = email)))
            }
        }
        awaitClose { registration.remove() }
    }

    /** Lectura puntual del perfil. */
    suspend fun getProfile(email: String): Result<UserProfile> = runCatching {
        val document = userDoc(email).get().await()
        UserProfile.fromMap(email, document.data)
    }.onFailure { Log.e(TAG, "Error obteniendo perfil de $email: ${it.message}") }

    /** Crea el documento de perfil (sin password). */
    suspend fun createProfile(profile: UserProfile): Result<Unit> = runCatching {
        userDoc(profile.email).set(
            mapOf(
                "nameSurname" to profile.nameSurname,
                "username" to profile.username,
                "description" to profile.description,
                "country" to profile.country
            )
        ).await()
        Unit
    }.onFailure { Log.e(TAG, "Error creando perfil ${profile.email}: ${it.message}") }

    /** Actualiza campos del perfil. */
    suspend fun updateProfile(email: String, fields: Map<String, Any?>): Result<Unit> = runCatching {
        val cleaned = fields.filterValues { it != null }
        if (cleaned.isEmpty()) return@runCatching Unit
        userDoc(email).update(cleaned).await()
        Unit
    }.onFailure { Log.e(TAG, "Error actualizando perfil $email: ${it.message}") }

    /** Actualiza la imagen de perfil. */
    suspend fun updateProfileImage(email: String, imageUri: Uri): Result<Unit> = runCatching {
        userDoc(email).update("imageUri", imageUri.toString()).await()
        Unit
    }.onFailure { Log.e(TAG, "Error actualizando imagen de $email: ${it.message}") }

    /** Comprueba si existe un perfil (usado al añadir amigos). */
    suspend fun profileExists(email: String): Result<Boolean> = runCatching {
        userDoc(email).get().await().exists()
    }.onFailure { Log.e(TAG, "Error comprobando existencia de $email: ${it.message}") }

    // ------------------------------------------------------------------------
    // Amigos
    // ------------------------------------------------------------------------

    /** Añade un amigo (idempotente: si ya existe devuelve éxito sin duplicar). */
    suspend fun addFriend(email: String, friendEmail: String): Result<Unit> = runCatching {
        require(email.isNotEmpty()) { "Usuario no autenticado" }
        val ref = userDoc(email).collection(FRIENDS_COLLECTION).document(friendEmail)
        if (!ref.get().await().exists()) {
            ref.set(emptyMap<String, Any>()).await()
        }
        Unit
    }.onFailure { Log.e(TAG, "Error añadiendo amigo: ${it.message}") }

    /** Elimina un amigo. */
    suspend fun removeFriend(email: String, friendEmail: String): Result<Unit> = runCatching {
        require(email.isNotEmpty()) { "Usuario no autenticado" }
        userDoc(email).collection(FRIENDS_COLLECTION).document(friendEmail).delete().await()
        Unit
    }.onFailure { Log.e(TAG, "Error eliminando amigo: ${it.message}") }

    /** Lista de amigos con su nombre de usuario. */
    suspend fun getFriends(email: String): Result<List<Friend>> = runCatching {
        val snapshot = userDoc(email).collection(FRIENDS_COLLECTION).get().await()
        snapshot.documents.map { friendDoc ->
            val profile = getProfile(friendDoc.id).getOrNull()
            Friend(
                email = friendDoc.id,
                username = profile?.username?.takeIf { it.isNotBlank() } ?: "Sin nombre"
            )
        }
    }.onFailure { Log.e(TAG, "Error obteniendo amigos: ${it.message}") }

    // ------------------------------------------------------------------------
    // Mensajes
    // ------------------------------------------------------------------------

    /** Publica un mensaje en el muro del usuario. */
    suspend fun publishMessage(email: String, message: String, time: String): Result<Unit> =
        runCatching {
            require(email.isNotEmpty()) { "Usuario no autenticado" }
            userDoc(email).collection(MESSAGES_COLLECTION)
                .add(mapOf("texto" to message, "hora" to time)).await()
            Unit
        }.onFailure { Log.e(TAG, "Error publicando mensaje: ${it.message}") }

    /** Elimina un mensaje del usuario. */
    suspend fun deleteMessage(email: String, messageId: String): Result<Unit> = runCatching {
        require(email.isNotEmpty()) { "Usuario no autenticado" }
        userDoc(email).collection(MESSAGES_COLLECTION).document(messageId).delete().await()
        Unit
    }.onFailure { Log.e(TAG, "Error eliminando mensaje: ${it.message}") }

    /** Mensajes del muro de un usuario. */
    suspend fun getMessages(email: String): Result<List<ChatMessage>> = runCatching {
        if (email.isBlank()) return@runCatching emptyList()
        val snapshot = userDoc(email).collection(MESSAGES_COLLECTION).get().await()
        snapshot.documents.map { document ->
            ChatMessage(
                id = document.id,
                text = document.getString("texto").orEmpty(),
                time = document.getString("hora").orEmpty(),
                ownerEmail = email
            )
        }
    }.onFailure { Log.e(TAG, "Error obteniendo mensajes: ${it.message}") }
}
