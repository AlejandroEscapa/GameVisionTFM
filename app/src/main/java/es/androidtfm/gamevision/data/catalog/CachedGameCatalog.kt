package es.androidtfm.gamevision.data.catalog

import es.androidtfm.gamevision.data.catalog.local.GameDao
import es.androidtfm.gamevision.data.catalog.local.GameEntity
import es.androidtfm.gamevision.data.catalog.local.SearchCacheEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Decorador de catálogo con caché local y modo degradado (F0/T0.4 y T0.5).
 *
 * Envuelve a otro GameCatalog (hoy RawgGameCatalog) y añade dos cosas:
 *  1. Cachea cada ficha visitada y cada búsqueda reciente en Room.
 *  2. Si la red/RAWG falla, sirve lo cacheado en lugar de dar error — así la app
 *     sigue mostrando el catálogo ya visto aunque el proveedor esté caído (el
 *     riesgo transversal del proyecto: RAWG cayó 1 d 15 h en agosto de 2026).
 *
 * Sigue devolviendo Result: solo se cae si NO hay red Y NO hay caché.
 */
class CachedGameCatalog(
    private val remote: GameCatalog,
    private val dao: GameDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis
) : GameCatalog {

    /** Última operación respondida desde caché (F0/T0.5: modo degradado visible). */
    @Volatile
    private var cacheServed = false

    override suspend fun isServingFromCache(): Boolean = cacheServed

    override suspend fun search(query: String): Result<List<CatalogGame>> =
        withContext(ioDispatcher) {
            cacheServed = false
            remote.search(query).fold(
                onSuccess = { games ->
                    // Cachea juegos + la búsqueda (con orden) para poder servirla sin red.
                    dao.upsertGames(games.map { it.toEntity(clock()) })
                    dao.upsertSearch(
                        SearchCacheEntity(
                            query = query,
                            orderedGameIds = games.map { it.id },
                            cachedAt = clock()
                        )
                    )
                    Result.success(games)
                },
                onFailure = { error ->
                    cachedSearch(query)?.let {
                        cacheServed = true
                        Result.success(it)
                    } ?: Result.failure(error)
                }
            )
        }

    override suspend fun getDetails(gameId: Int): Result<CatalogGame?> =
        withContext(ioDispatcher) {
            cacheServed = false
            remote.getDetails(gameId).fold(
                onSuccess = { game ->
                    game?.let { dao.upsertGames(listOf(it.toEntity(clock()))) }
                    Result.success(game)
                },
                onFailure = { error ->
                    dao.game(gameId)?.let {
                        cacheServed = true
                        Result.success(it.toDomain())
                    } ?: Result.failure(error)
                }
            )
        }

    /** Reconstruye la lista cacheada de una búsqueda, respetando el orden guardado. */
    private suspend fun cachedSearch(query: String): List<CatalogGame>? {
        val cached = dao.search(query) ?: return null
        if (cached.orderedGameIds.isEmpty()) return null
        val byId = dao.games(cached.orderedGameIds).associateBy { it.id }
        val restored = cached.orderedGameIds.mapNotNull { byId[it]?.toDomain() }
        return restored.takeIf { it.isNotEmpty() }
    }
}

/* --- Mapeo dominio <-> entidad (el resto de la app no conoce GameEntity) --- */

private fun CatalogGame.toEntity(now: Long): GameEntity = GameEntity(
    id = id,
    name = name,
    coverUrl = coverUrl,
    released = released,
    rating = rating,
    ratingsCount = ratingsCount,
    metacritic = metacritic,
    suggestionsCount = suggestionsCount,
    genres = genres,
    platforms = platforms,
    playtimeHours = playtimeHours,
    cachedAt = now
)

private fun GameEntity.toDomain(): CatalogGame = CatalogGame(
    id = id,
    name = name,
    coverUrl = coverUrl,
    released = released,
    rating = rating,
    ratingsCount = ratingsCount,
    metacritic = metacritic,
    suggestionsCount = suggestionsCount,
    genres = genres,
    platforms = platforms,
    playtimeHours = playtimeHours
)
