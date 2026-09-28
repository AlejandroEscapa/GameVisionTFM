package es.androidtfm.gamevision.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Historial local de "vistos recientemente" (F0-B, decisión del 28/09/2026:
 * el historial deja de vivir en Firestore y pasa a ser local del dispositivo).
 *
 * Se guarda un JSON pequeño en DataStore: los últimos 20 juegos abiertos, con
 * nombre y portada para poder pintarlos sin red y sin consultar el catálogo.
 */

val Context.recentGamesDataStore: DataStore<Preferences> by preferencesDataStore(name = "recentGames")

/** Juego del historial local (mínimo para pintar la tarjeta sin red). */
@Serializable
data class RecentGame(
    val gameId: Int,
    val name: String,
    val coverUrl: String? = null
)

@Singleton
class RecentGamesStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.recentGamesDataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(RecentGame.serializer())

    companion object {
        private const val MAX_RECENTS = 20
        private val KEY = stringPreferencesKey("recent_games_json")
    }

    /** Últimos juegos vistos, el más reciente primero. */
    val recentGames: Flow<List<RecentGame>> = dataStore.data.map { prefs ->
        decode(prefs[KEY])
    }

    /** Añade un juego al historial local (deduplicado y limitado a 20). */
    suspend fun add(game: RecentGame) {
        dataStore.edit { prefs ->
            val updated = (listOf(game) + decode(prefs[KEY]).filterNot { it.gameId == game.gameId })
                .take(MAX_RECENTS)
            prefs[KEY] = json.encodeToString(serializer, updated)
        }
    }

    private fun decode(raw: String?): List<RecentGame> =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrDefault(emptyList()) }
            ?: emptyList()
}
