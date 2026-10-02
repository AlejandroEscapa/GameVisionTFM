package es.androidtfm.gamevision.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Escritura de la imagen compartible del Rewind (F3/T3.8-T3.9).
 *
 * Por qué en la caché y no en el almacenamiento del usuario: la imagen es un
 * subproducto de compartir, no un documento que el usuario haya pedido guardar.
 * La caché la limpia el sistema y no requiere permisos ni aparece en la galería
 * (si algún día se ofrece «guardar en el dispositivo», esa es otra decisión y
 * otro flujo: MediaStore).
 *
 * El fichero se expone con FileProvider (ver `res/xml/file_paths.xml`), nunca con
 * una ruta `file://`.
 */
object ShareImageStorage {

    /** Carpeta de caché donde viven las imágenes compartibles. */
    private const val CARPETA = "rewind"

    /**
     * Comprime [bitmap] como PNG en la caché y devuelve el URI con el que otra app
     * puede leerlo. Escribe en IO: nunca en el hilo de UI.
     */
    suspend fun writeToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, CARPETA).apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
