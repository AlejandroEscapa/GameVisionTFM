package es.androidtfm.gamevision.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import es.androidtfm.gamevision.data.model.ChatMessage
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.data.model.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (Frente 2 — SSOT y propagación de errores)
 *        actualizado 29/09/2026 (ADR-0008: clave única = uid)
 *
 * Acceso a datos en Firestore.
 *
 * CLAVE ÚNICA (ADR-0008): todo cuelga del **uid** de Firebase Auth. El email deja
 * de ser clave y pasa a ser un CAMPO del perfil. Se mantienen dos índices para
 * poder resolver «email → uid» (alta de amigos) y «username → uid» (búsqueda, F2)
 * sin exponer ni recorrer toda la colección de usuarios.
 *
 * Reglas de esta capa:
 *  - TODOS los métodos devuelven Result: nunca se traga un error en silencio.
 *  - El perfil se expone como Flow en vivo (addSnapshotListener).
 *  - El password NO existe aquí: vive en Firebase Authentication.
 *  - Los juegos viven en LibraryRepository.
 */

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "UserRepository"
        private const val USERS = "users"
        private const val FRIENDS = "friends"
        private const val MESSAGES = "messages"
        private const val EMAIL_INDEX = "email_index"
        private const val USERNAMES = "usernames"
    }

    private fun userDoc(uid: String) = db.collection(USERS).document(uid)

    // ------------------------------------------------------------------------
    // Perfil (clave: uid; el email es un campo)
    // ------------------------------------------------------------------------

    /** Flujo en vivo del perfil: única fuente de verdad de los datos de usuario. */
    fun observeProfile(uid: String): Flow<Result<UserProfile>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(Result.success(UserProfile()))
            close()
            return@callbackFlow
        }
        val registration = userDoc(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> trySend(Result.failure(error))
                snapshot != null && snapshot.exists() ->
                    trySend(Result.success(UserProfile.fromMap(snapshot.data)))
                else -> trySend(Result.success(UserProfile()))
            }
        }
        awaitClose { registration.remove() }
    }

    /** Lectura puntual del perfil. */
    suspend fun getProfile(uid: String): Result<UserProfile> = runCatching {
        val document = userDoc(uid).get().await()
        UserProfile.fromMap(document.data)
    }.onFailure { Log.e(TAG, "Error obteniendo perfil de $uid: ${it.message}") }

    /**
     * Crea el documento de perfil (sin password) en `users/{uid}` y sus índices
     * (`email_index`, `usernames`). El email se guarda como CAMPO.
     *
     * Todo en UN lote: si un índice no se puede escribir (p. ej. el username ya está
     * tomado y las reglas lo deniegan), no se escribe nada — así no quedan perfiles
     * huérfanos ni escrituras a medias.
     */
    suspend fun createProfile(uid: String, profile: UserProfile): Result<Unit> = runCatching {
        require(uid.isNotBlank()) { "Usuario no autenticado" }
        val email = profile.email.trim().lowercase()
        val username = profile.username
        val batch = db.batch()
        batch.set(
            userDoc(uid),
            mapOf(
                "email" to email,
                "nameSurname" to profile.nameSurname,
                "username" to username,
                "description" to profile.description,
                "country" to profile.country
            )
        )
        applyIndexWrites(batch, createIndexWrites(uid, email, username))
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error creando perfil $uid: ${it.message}") }

    /**
     * Actualiza campos del perfil. Si cambia el `username`, retira el índice viejo y
     * registra el nuevo **en el mismo lote**: o cambia todo, o no cambia nada.
     */
    suspend fun updateProfile(uid: String, fields: Map<String, Any?>): Result<Unit> = runCatching {
        require(uid.isNotBlank()) { "Usuario no autenticado" }
        val cleaned = fields.filterValues { it != null }
        if (cleaned.isEmpty()) return@runCatching Unit

        val newUsername = cleaned["username"] as? String
        val oldUsername = if (newUsername.isNullOrBlank()) null
            else userDoc(uid).get().await().getString("username").orEmpty()

        val batch = db.batch()
        batch.update(userDoc(uid), cleaned)
        applyIndexWrites(batch, updateIndexWrites(uid, newUsername, oldUsername))
        batch.commit().await()
        Unit
    }.onFailure { Log.e(TAG, "Error actualizando perfil $uid: ${it.message}") }

    /**
     * Persiste la URL de la foto de perfil ya subida (F0/T0.12).
     * Puntero estable: la imagen vive en `profile_images/{uid}`.
     */
    suspend fun updateProfileImage(uid: String, imageUrl: String): Result<Unit> = runCatching {
        require(uid.isNotBlank()) { "Usuario no autenticado" }
        userDoc(uid).update("imageUri", imageUrl).await()
        Unit
    }.onFailure { Log.e(TAG, "Error actualizando imagen de $uid: ${it.message}") }

    /** Comprueba si existe un perfil. */
    suspend fun profileExists(uid: String): Result<Boolean> = runCatching {
        userDoc(uid).get().await().exists()
    }.onFailure { Log.e(TAG, "Error comprobando existencia de $uid: ${it.message}") }

    /**
     * Resuelve un email a uid usando el índice `email_index` (no expone ni recorre
     * la colección de usuarios; coherente con D2.4). null si no existe.
     */
    suspend fun findUidByEmail(email: String): Result<String?> = runCatching {
        val key = email.trim().lowercase()
        if (key.isBlank()) return@runCatching null
        db.collection(EMAIL_INDEX).document(key).get().await().getString("uid")
    }.onFailure { Log.e(TAG, "Error resolviendo email→uid: ${it.message}") }

    /** Perfil público por uid (nombre de usuario visible sin exponer datos privados). */
    suspend fun getUsername(uid: String): Result<String> = runCatching {
        userDoc(uid).get().await().getString("username").orEmpty()
    }.onFailure { Log.e(TAG, "Error obteniendo username de $uid: ${it.message}") }

    /** Traduce el plan de índices a escrituras de lote. La decisión ya está tomada arriba. */
    private fun applyIndexWrites(batch: WriteBatch, writes: List<IndexWrite>) {
        writes.forEach { write ->
            when (write) {
                is IndexWrite.LinkEmail ->
                    batch.set(db.collection(EMAIL_INDEX).document(write.email), mapOf("uid" to write.uid))
                is IndexWrite.LinkUsername ->
                    batch.set(db.collection(USERNAMES).document(write.username), mapOf("uid" to write.uid))
                is IndexWrite.UnlinkUsername ->
                    batch.delete(db.collection(USERNAMES).document(write.username))
            }
        }
    }

    // ------------------------------------------------------------------------
    // Amigos (clave: uid; el id del documento es el uid del amigo)
    // ------------------------------------------------------------------------

    /** Añade un amigo (idempotente). */
    suspend fun addFriend(uid: String, friendUid: String): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        require(friendUid.isNotEmpty()) { "Amigo no válido" }
        require(uid != friendUid) { "No puedes añadirte a ti mismo" }
        val ref = userDoc(uid).collection(FRIENDS).document(friendUid)
        if (!ref.get().await().exists()) {
            ref.set(emptyMap<String, Any>()).await()
        }
        Unit
    }.onFailure { Log.e(TAG, "Error añadiendo amigo: ${it.message}") }

    /** Elimina un amigo. */
    suspend fun removeFriend(uid: String, friendUid: String): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        userDoc(uid).collection(FRIENDS).document(friendUid).delete().await()
        Unit
    }.onFailure { Log.e(TAG, "Error eliminando amigo: ${it.message}") }

    /**
     * Lista de amigos con su nombre visible. El email NO se expone (D2.4).
     *
     * Una sola lectura de la lista y resolución de perfiles en paralelo: el nombre y el
     * username salen del mismo documento de perfil, así que no hay viajes extra.
     */
    suspend fun getFriends(uid: String): Result<List<Friend>> = runCatching {
        val snapshot = userDoc(uid).collection(FRIENDS).get().await()
        coroutineScope {
            snapshot.documents.map { friendDoc ->
                async {
                    val friendUid = friendDoc.id
                    val profile = getProfile(friendUid).getOrNull()
                    Friend(
                        uid = friendUid,
                        nameSurname = profile?.nameSurname.orEmpty(),
                        username = profile?.username?.takeIf { it.isNotBlank() } ?: "Sin nombre"
                    )
                }
            }.awaitAll()
        }
    }.onFailure { Log.e(TAG, "Error obteniendo amigos: ${it.message}") }

    // ------------------------------------------------------------------------
    // Mensajes (clave: uid del dueño del muro)
    // ------------------------------------------------------------------------

    /** Publica un mensaje en el muro del usuario. */
    suspend fun publishMessage(uid: String, message: String, time: String): Result<Unit> =
        runCatching {
            require(uid.isNotEmpty()) { "Usuario no autenticado" }
            userDoc(uid).collection(MESSAGES)
                .add(mapOf("texto" to message, "hora" to time)).await()
            Unit
        }.onFailure { Log.e(TAG, "Error publicando mensaje: ${it.message}") }

    /** Elimina un mensaje del usuario. */
    suspend fun deleteMessage(uid: String, messageId: String): Result<Unit> = runCatching {
        require(uid.isNotEmpty()) { "Usuario no autenticado" }
        userDoc(uid).collection(MESSAGES).document(messageId).delete().await()
        Unit
    }.onFailure { Log.e(TAG, "Error eliminando mensaje: ${it.message}") }

    /** Mensajes del muro de un usuario. */
    suspend fun getMessages(uid: String): Result<List<ChatMessage>> = runCatching {
        if (uid.isBlank()) return@runCatching emptyList()
        val snapshot = userDoc(uid).collection(MESSAGES).get().await()
        snapshot.documents.map { document ->
            ChatMessage(
                id = document.id,
                text = document.getString("texto").orEmpty(),
                time = document.getString("hora").orEmpty(),
                ownerUid = uid
            )
        }
    }.onFailure { Log.e(TAG, "Error obteniendo mensajes: ${it.message}") }
}
