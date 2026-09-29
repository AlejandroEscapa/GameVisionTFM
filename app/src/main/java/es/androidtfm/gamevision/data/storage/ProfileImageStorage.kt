package es.androidtfm.gamevision.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/*
 * F0/T0.12 — Foto de perfil sin Firebase Storage (ADR-0007).
 *
 * Antes se subía a Cloud Storage, pero Storage exige el plan de pago Blaze aunque
 * solo guardemos un avatar. Como el único uso es la foto de perfil, se guarda la
 * imagen **comprimida** (base64) en Firestore, en una colección propia
 * `profile_images/{uid}` (documento aparte para no engordar el perfil), y en el
 * perfil se persiste un puntero estable. Así viaja entre dispositivos (CA0.3)
 * sin activar ningún servicio de pago.
 *
 * Escalabilidad (cuota gratuita de Firestore): ~130 KB por avatar (imagen JPEG de
 * ~100 KB en base64) → 1 GiB ≈ ~8.000 avatares; 10 GiB/mes de salida ≈ ~80.000
 * vistas/mes. El límite por documento de Firestore es 1 MiB; aquí el tope es
 * MAX_STORED_BYTES, muy por debajo.
 */

@Singleton
class ProfileImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "ProfileImageStorage"
        private const val COLLECTION = "profile_images"
        private const val FIELD_IMAGE = "image"
        private const val FIELD_UPDATED_AT = "updatedAt"

        /** Máximo del archivo elegido por el usuario, antes de comprimir. */
        const val MAX_SELECTED_BYTES = 5L * 1024 * 1024

        /**
         * Máximo del resultado comprimido que aceptamos guardar. Es la cifra que
         * sostiene la escalabilidad en el plan gratuito (ver cabecera).
         */
        const val MAX_STORED_BYTES = 256 * 1024

        private const val MAX_DIMENSION = 512
        private const val JPEG_QUALITY = 80

        /** Marca que se guarda en el perfil (Firestore) para saber que hay foto. */
        const val POINTER = "firestore://profile_images"
    }

    /** true si el valor guardado en el perfil apunta a la imagen gestionada por la app. */
    fun isManaged(imageUri: String?): Boolean = imageUri == POINTER

    /**
     * Comprime la imagen elegida y la guarda en `profile_images/{uid}`.
     * Devuelve el puntero estable que se persiste en el perfil.
     */
    suspend fun uploadProfileImage(uid: String, localUri: Uri): Result<String> = runCatching {
        require(uid.isNotBlank()) { "Usuario no autenticado" }

        val bytes = context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen seleccionada")
        require(bytes.isNotEmpty()) { "La imagen está vacía" }
        require(bytes.size <= MAX_SELECTED_BYTES) {
            "La imagen supera el máximo de ${MAX_SELECTED_BYTES / (1024 * 1024)} MB. Elige una más ligera."
        }

        val jpeg = compress(bytes)
            ?: error("No se pudo procesar la imagen. Prueba con otra.")
        require(jpeg.size <= MAX_STORED_BYTES) {
            "La imagen es demasiado grande incluso comprimida (máx. ${MAX_STORED_BYTES / 1024} KB). Prueba con otra."
        }

        val base64 = Base64.encodeToString(jpeg, Base64.NO_WRAP)
        db.collection(COLLECTION).document(uid).set(
            mapOf(
                FIELD_IMAGE to base64,
                FIELD_UPDATED_AT to System.currentTimeMillis()
            )
        ).await()
        POINTER
    }.onFailure { Log.e(TAG, "Error subiendo la foto de perfil: ${it.message}") }

    /**
     * Devuelve la imagen del usuario como data URI (`data:image/jpeg;base64,…`)
     * o null si no tiene foto. Coil sabe cargar data URIs, así que la UI no cambia.
     */
    suspend fun loadProfileImageDataUri(uid: String): Result<String?> = runCatching {
        if (uid.isBlank()) return@runCatching null
        val base64 = db.collection(COLLECTION).document(uid).get().await().getString(FIELD_IMAGE)
        base64?.takeIf { it.isNotBlank() }?.let { "data:image/jpeg;base64,$it" }
    }.onFailure { Log.e(TAG, "Error leyendo la foto de perfil: ${it.message}") }

    /**
     * Reduce la imagen a un JPEG de como mucho MAX_DIMENSION px por lado.
     * Primero decodifica con inSampleSize (evita cargar en memoria la foto entera)
     * y después escala a la medida final.
     */
    private fun compress(bytes: ByteArray): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null

        val scaled = scaleDown(decoded)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        if (scaled !== decoded) decoded.recycle()
        scaled.recycle()
        return out.toByteArray()
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var largest = max(width, height)
        while (largest / 2 >= MAX_DIMENSION) {
            largest /= 2
            sample *= 2
        }
        return sample
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= MAX_DIMENSION) return bitmap
        val ratio = MAX_DIMENSION.toFloat() / largest
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
