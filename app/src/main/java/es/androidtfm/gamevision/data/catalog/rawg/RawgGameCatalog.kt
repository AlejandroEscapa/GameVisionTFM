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

    override suspend fun popular(limit: Int): Result<List<CatalogGame>> = runCatching {
        gamesApi.discoverGames(ordering = "-added", pageSize = limit).results.map { it.toDomain() }
    }.onFailure { Log.e(TAG, "Error cargando populares: ${it.message}") }

    override suspend fun byGenres(genres: List<String>, limit: Int): Result<List<CatalogGame>> =
        runCatching {
            val slugs = genres.joinToString(",") { it.lowercase().replace(" ", "-") }
            gamesApi.discoverGames(
                // Popularidad dentro del género: con -rating salen indies oscuros
                // de nota alta; para «Para ti» queremos juegos reconocibles.
                ordering = "-added",
                genresSlugs = slugs.ifBlank { null },
                pageSize = limit
            ).results.map { it.toDomain() }
        }.onFailure { Log.e(TAG, "Error descubriendo por géneros: ${it.message}") }

    override suspend fun discover(seed: Long, limit: Int): Result<List<CatalogGame>> = runCatching {
        // Estable dentro del día: la semilla deriva la página dentro de un rango
        // razonable (RAWG no expone aleatorio) y el orden es por valoración.
        val page = (seed % 20).toInt() + 1
        // Filtro de nota: sin él, paginar el catálogo saca contenido de relleno
        // (portadas y fichas de baja calidad). 75+ mantiene el listón.
        gamesApi.discoverGames(
            ordering = "-rating",
            metacritic = "75,100",
            page = page,
            pageSize = limit
        ).results.map { it.toDomain() }
    }.onFailure { Log.e(TAG, "Error descubriendo: ${it.message}") }
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
