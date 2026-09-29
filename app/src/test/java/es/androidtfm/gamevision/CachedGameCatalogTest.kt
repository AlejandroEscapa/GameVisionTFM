package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.catalog.CachedGameCatalog
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.data.catalog.local.GameDao
import es.androidtfm.gamevision.data.catalog.local.GameEntity
import es.androidtfm.gamevision.data.catalog.local.HltbCacheEntity
import es.androidtfm.gamevision.data.catalog.local.SearchCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests JVM del decorador de caché (F0/T0.4-T0.5).
 *
 * El DAO es una interfaz: se falsea en memoria, sin Android ni Robolectric.
 */
class CachedGameCatalogTest {

    /** Catálogo remoto falso: se programa el resultado de cada llamada. */
    private class FakeCatalog(
        var searchResult: Result<List<CatalogGame>>,
        var detailsResult: Result<CatalogGame?>
    ) : GameCatalog {
        override suspend fun search(query: String) = searchResult
        override suspend fun getDetails(gameId: Int) = detailsResult
    }

    /** DAO en memoria que implementa la interfaz real. */
    private class FakeDao : GameDao {
        val games = LinkedHashMap<Int, GameEntity>()
        val searches = LinkedHashMap<String, SearchCacheEntity>()
        override suspend fun upsertGames(games: List<GameEntity>) { games.forEach { this.games[it.id] = it } }
        override suspend fun game(id: Int) = games[id]
        override suspend fun games(ids: List<Int>) = ids.mapNotNull { games[it] }
        override suspend fun upsertSearch(search: SearchCacheEntity) { searches[search.query] = search }
        override suspend fun search(query: String) = searches[query]
        val hltb = LinkedHashMap<String, HltbCacheEntity>()
        override suspend fun hltb(key: String) = hltb[key]
        override suspend fun upsertHltb(entity: HltbCacheEntity) { hltb[entity.cacheKey] = entity }
    }

    private fun game(id: Int, name: String) = CatalogGame(
        id = id, name = name, coverUrl = "http://img/$id.jpg",
        released = "2024-01-01", rating = 4.2, ratingsCount = 10,
        metacritic = 88, suggestionsCount = 3,
        genres = listOf("Action"), platforms = listOf("PC"), playtimeHours = 12
    )

    private fun catalog(remote: GameCatalog, dao: GameDao) =
        CachedGameCatalog(remote, dao, ioDispatcher = Dispatchers.Unconfined, clock = { 1_000L })

    @Test
    fun `search con exito cachea juegos y busqueda y devuelve el resultado`() = runTest {
        val dao = FakeDao()
        val sut = catalog(FakeCatalog(Result.success(listOf(game(1, "A"), game(2, "B"))), Result.success(null)), dao)

        val result = sut.search("zelda")

        assertTrue(result.isSuccess)
        assertEquals(listOf(1, 2), result.getOrNull()?.map { it.id })
        assertEquals(listOf(1, 2), dao.searches["zelda"]?.orderedGameIds)
        assertEquals(2, dao.games.size)
    }

    @Test
    fun `search sin red devuelve la cache conservando el orden`() = runTest {
        val dao = FakeDao()
        // Primero una búsqueda con éxito para poblar la caché...
        catalog(FakeCatalog(Result.success(listOf(game(1, "A"), game(2, "B"))), Result.success(null)), dao)
            .search("zelda")
        // ...y luego la misma búsqueda sin red.
        val offline = catalog(
            FakeCatalog(Result.failure(RuntimeException("sin conexion")), Result.success(null)),
            dao
        )

        val result = offline.search("zelda")

        assertTrue(result.isSuccess)
        assertEquals(listOf(1, 2), result.getOrNull()?.map { it.id })
    }

    @Test
    fun `search sin red y sin cache propaga el error`() = runTest {
        val sut = catalog(
            FakeCatalog(Result.failure(RuntimeException("sin conexion")), Result.success(null)),
            FakeDao()
        )

        val result = sut.search("nunca-buscado")

        assertTrue(result.isFailure)
        assertEquals("sin conexion", result.exceptionOrNull()?.message)
    }

    @Test
    fun `details con exito cachea la ficha`() = runTest {
        val dao = FakeDao()
        val sut = catalog(FakeCatalog(Result.success(emptyList()), Result.success(game(7, "G"))), dao)

        val result = sut.getDetails(7)

        assertTrue(result.isSuccess)
        assertEquals("G", dao.games[7]?.name)
    }

    @Test
    fun `details sin red devuelve la ficha cacheada`() = runTest {
        val dao = FakeDao()
        catalog(FakeCatalog(Result.success(emptyList()), Result.success(game(7, "G"))), dao).getDetails(7)
        val offline = catalog(
            FakeCatalog(Result.success(emptyList()), Result.failure(RuntimeException("sin conexion"))),
            dao
        )

        val result = offline.getDetails(7)

        assertTrue(result.isSuccess)
        assertEquals("G", result.getOrNull()?.name)
    }

    @Test
    fun `details sin red y sin cache propaga el error`() = runTest {
        val sut = catalog(
            FakeCatalog(Result.success(emptyList()), Result.failure(RuntimeException("sin conexion"))),
            FakeDao()
        )

        val result = sut.getDetails(99)

        assertTrue(result.isFailure)
    }

    // ---- Modo degradado visible (F0/T0.5) ----

    @Test
    fun `search desde cache marca el modo degradado y la red lo apaga`() = runTest {
        val dao = FakeDao()
        catalog(FakeCatalog(Result.success(listOf(game(1, "A"))), Result.success(null)), dao).search("zelda")
        val offline = catalog(
            FakeCatalog(Result.failure(RuntimeException("sin conexion")), Result.success(null)),
            dao
        )

        offline.search("zelda")

        assertTrue(offline.isServingFromCache())

        // Con red de nuevo, la respuesta es directa y el aviso desaparece
        val online = catalog(
            FakeCatalog(Result.success(listOf(game(1, "A"), game(3, "C"))), Result.success(null)),
            dao
        )
        online.search("zelda")

        assertTrue(!online.isServingFromCache())
    }

    @Test
    fun `details desde cache marca el modo degradado`() = runTest {
        val dao = FakeDao()
        catalog(FakeCatalog(Result.success(emptyList()), Result.success(game(7, "G"))), dao).getDetails(7)
        val offline = catalog(
            FakeCatalog(Result.success(emptyList()), Result.failure(RuntimeException("sin conexion"))),
            dao
        )

        offline.getDetails(7)

        assertTrue(offline.isServingFromCache())
    }

    @Test
    fun `sin cache el modo degradado no se activa`() = runTest {
        val sut = catalog(
            FakeCatalog(Result.failure(RuntimeException("sin conexion")), Result.success(null)),
            FakeDao()
        )

        sut.search("nunca-buscado")

        assertTrue(!sut.isServingFromCache())
    }
}
