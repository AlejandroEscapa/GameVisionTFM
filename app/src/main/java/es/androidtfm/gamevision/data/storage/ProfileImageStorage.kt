package es.androidtfm.gamevision.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/*
 * F0/T0.12 — Foto de perfil en Firebase Storage.
 *
 * Antes la imagen se copiaba a filesDir y en Firestore se guardaba esa ruta
 * local: al cambiar de dispositivo (o borrar datos) la foto no existía (CA0.3).
 * Ahora se sube a Storage y en el perfil se persiste la URL de descarga, que
 * sí viaja entre dispositivos.
 */

@Singleton
class ProfileImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {

    companion object {
        private const val TAG = "ProfileImageStorage"
        private const val MAX_BYTES = 5L * 1024 * 1024
        private const val PROFILE_IMAGES = "profile_images"
    }

    /**
     * Sube la imagen elegida al Storage del usuario y devuelve su URL de descarga.
     * La ruta `profile_images/{email}/profile.jpg` es fija por usuario: cada subida
     * sustituye a la anterior y la URL de descarga sigue siendo estable.
     */
    suspend fun uploadProfileImage(email: String, localUri: Uri): Result<String> = runCatching {
        require(email.isNotBlank()) { "Usuario no autenticado" }

        val bytes = context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen seleccionada")
        require(bytes.isNotEmpty()) { "La imagen está vacía" }
        require(bytes.size <= MAX_BYTES) { "La imagen supera los 5 MB" }

        // Storage no tiene la restricción de caracteres de Firestore: el email (sin
        // '/') sirve como carpeta, y así las reglas casan identidad exacta con request.auth.
        val fileName = email.trim().lowercase()
        val reference = storage.reference.child("$PROFILE_IMAGES/$fileName/profile.jpg")

        reference.putBytes(bytes).await()
        reference.downloadUrl.await().toString()
    }.onFailure { Log.e(TAG, "Error subiendo la foto de perfil: ${it.message}") }
}
