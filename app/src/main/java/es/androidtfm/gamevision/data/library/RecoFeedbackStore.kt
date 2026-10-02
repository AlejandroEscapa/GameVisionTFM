package es.androidtfm.gamevision.data.library

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Feedback ligero de la recomendación «¿Qué juego ahora?» (F3/T3.5, decisión D3.8).
 *
 * Guarda por usuario qué recomendaciones aceptó («Jugar») y cuáles descartó
 * («Descartar»). El motor (RecommendationEngine) excluye las descartadas y
 * empuja las aceptadas: así la respuesta mejora con el uso sin pedir nada más.
 *
 * Vive en DataStore local y no en Firestore a propósito: el motor corre en el
 * cliente con datos locales (D3.3), no toca firestore.rules y funciona en
 * invitado. Si el feedback debe cruzar de dispositivo, se migra tal cual.
 *
 * Los dos conjuntos se mantienen disjuntos: descartar saca de aceptados y
 * aceptar saca de descartados.
 */

val Context.recoFeedbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "recoFeedback")

data class RecoFeedback(
    val descartados: Set<String> = emptySet(),
    val aceptados: Set<String> = emptySet()
)

class RecoFeedbackStore(
    context: Context,
    // Inyectable para pruebas; por defecto usa el DataStore de la aplicación
    dataStoreOverride: DataStore<Preferences>? = null
) {
    private val store = dataStoreOverride ?: context.recoFeedbackDataStore

    private fun claveDescartados(uid: String) = stringSetPreferencesKey("descartados_$uid")
    private fun claveAceptados(uid: String) = stringSetPreferencesKey("aceptados_$uid")

    /** Feedback en vivo del usuario (SSOT del aprendizaje ligero). */
    fun feedback(uid: String): Flow<RecoFeedback> = store.data.map { prefs ->
        RecoFeedback(
            descartados = prefs[claveDescartados(uid)].orEmpty(),
            aceptados = prefs[claveAceptados(uid)].orEmpty()
        )
    }

    /** «Jugar»: el usuario elige esta recomendación (señal positiva). */
    suspend fun registrarAceptado(uid: String, gameId: String) {
        store.edit { prefs ->
            prefs[claveAceptados(uid)] = prefs[claveAceptados(uid)].orEmpty() + gameId
            prefs[claveDescartados(uid)] = prefs[claveDescartados(uid)].orEmpty() - gameId
        }
    }

    /** «Descartar»: el usuario no quiere ver esta recomendación (señal negativa). */
    suspend fun registrarDescartado(uid: String, gameId: String) {
        store.edit { prefs ->
            prefs[claveDescartados(uid)] = prefs[claveDescartados(uid)].orEmpty() + gameId
            prefs[claveAceptados(uid)] = prefs[claveAceptados(uid)].orEmpty() - gameId
        }
    }

    /** Vuelve a mostrar las descartadas (reversible: descartar no es para siempre). */
    suspend fun limpiarDescartados(uid: String) {
        store.edit { prefs ->
            prefs[claveDescartados(uid)] = emptySet()
        }
    }
}
