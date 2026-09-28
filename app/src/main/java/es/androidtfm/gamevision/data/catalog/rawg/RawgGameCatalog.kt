package es.androidtfm.gamevision.data.catalog.rawg

import android.util.Log
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.retrofit.GameApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del catálogo sobre RAWG (F0/T0.2).
 *
 * Es la única clase que conoce los DTOs de RAWG. Traduce sus respuestas a
 * [CatalogGame] y envuelve los fallos en `Result`.
 */
@Singleton
class RawgGameCatalog @Inject constructor(
    private val gamesApi: GameApiService
) : GameCatalog {

    companion object {
        private const val TAG = "RawgGameCatalog"
    }

    override suspend fun search(query: String): Result<List<CatalogGame>> = runCatching {
        gamesApi.searchGames(query).results.map { it.toDomain() }
    }.onFailure { Log.e(TAG, "Error buscando «$query»: ${it.message}") }

    override suspend fun getDetails(gameId: Int): Result<CatalogGame?> = runCatching {
        gamesApi.getGameDetails(gameId).toDomain()
    }.onFailure { Log.e(TAG, "Error cargando el juego $gameId: ${it.message}") }
}

/**
 * Traduce el DTO de RAWG a nuestro modelo de dominio.
 * Si RAWG cambia sus campos, el cambio se queda encerrado en este fichero.
 */
private fun Game.toDomain(): CatalogGame = CatalogGame(
    id = id,
    name = name,
    coverUrl = backgroundImage,
    released = released,
    rating = rating,
    ratingsCount = ratingsCount,
    metacritic = metacritic,
    suggestionsCount = suggestionsCount,
    genres = genres.map { it.name },
    platforms = platforms.map { it.name },
    // Hueco de duración (D0.6): RAWG trae `playtime` medio en horas; se usa como
    // aproximación provisional hasta que el propietario decida la fuente definitiva.
    playtimeHours = playtime.takeIf { it > 0 }
)
