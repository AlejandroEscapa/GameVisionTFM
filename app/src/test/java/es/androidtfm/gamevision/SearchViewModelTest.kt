package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios de SearchViewModel sobre el catálogo de dominio.
 *
 * Se usa un fake de `GameCatalog`, así que estos tests NO conocen a RAWG: si algún
 * día se migra a IGDB, siguen valiendo tal cual (ese es el valor del adapter, F0).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    private class FakeGameCatalog(
        private val searchResult: Result<List<CatalogGame>> = Result.success(emptyList()),
        private val detailsResult: Result<CatalogGame?> = Result.success(null)
    ) : GameCatalog {
        var lastQuery: String? = null
            private set

        override suspend fun search(query: String): Result<List<CatalogGame>> {
            lastQuery = query
            return searchResult
        }

        override suspend fun getDetails(gameId: Int): Result<CatalogGame?> = detailsResult
        override suspend fun popular(limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
        override suspend fun byGenres(genres: List<String>, limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
        override suspend fun discover(seed: Long, limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
    }

    private fun game(id: Int, name: String) = CatalogGame(
        id = id,
        name = name,
        coverUrl = "https://example.com/$id.jpg",
        released = "2025-01-01",
        rating = 4.5,
        genres = listOf("Action", "RPG")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchGames con exito actualiza la lista y finaliza la carga`() = runTest {
        val catalog = FakeGameCatalog(
            searchResult = Result.success(listOf(game(1, "Zelda"), game(2, "Mario")))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("  zelda \"\" ")

        assertEquals(listOf("Zelda", "Mario"), viewModel.games.value.map { it.name })
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `fetchGames limpia la consulta enviada al catalogo`() = runTest {
        val catalog = FakeGameCatalog()
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("  metroid \"prime\"  ")

        assertEquals("metroid prime", catalog.lastQuery)
    }

    @Test
    fun `fetchGames con fallo publica el error y deja la lista vacia`() = runTest {
        val catalog = FakeGameCatalog(
            searchResult = Result.failure(RuntimeException("sin conexión"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("zelda")

        assertTrue(viewModel.error.value?.contains("No se pudo cargar la información") == true)
        assertTrue(viewModel.games.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchGames sin conexion da un mensaje claro sin detalle tecnico`() = runTest {
        val catalog = FakeGameCatalog(
            searchResult = Result.failure(java.net.UnknownHostException("api.rawg.io"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("halo")

        val msg = viewModel.error.value.orEmpty()
        assertTrue(msg.contains("Sin conexión"))
        assertFalse(msg.contains("api.rawg.io"))
    }

    @Test
    fun `fetchGameDetails con exito actualiza los detalles`() = runTest {
        val catalog = FakeGameCatalog(
            detailsResult = Result.success(game(7, "Hollow Knight"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGameDetails(7)

        assertEquals("Hollow Knight", viewModel.gameDetails.value?.name)
        assertFalse(viewModel.isLoadingDetails.value)
        assertNull(viewModel.errorDetails.value)
    }

    @Test
    fun `fetchGameDetails con fallo publica el error de detalles`() = runTest {
        val catalog = FakeGameCatalog(
            detailsResult = Result.failure(RuntimeException("404"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGameDetails(99)

        assertTrue(viewModel.errorDetails.value?.contains("No se pudo cargar la información") == true)
        assertNull(viewModel.gameDetails.value)
    }

    @Test
    fun `fetchGameDetails sin conexion no expone el host del proveedor`() = runTest {
        val catalog = FakeGameCatalog(
            detailsResult = Result.failure(java.net.UnknownHostException("api.rawg.io"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGameDetails(1)

        val msg = viewModel.errorDetails.value.orEmpty()
        assertTrue(msg.contains("Sin conexión"))
        assertFalse(msg.contains("api.rawg.io"))
    }

    // ---- Modo degradado visible (F0/T0.5) ----

    /** Fake que replica el flag del decorador de caché. */
    private class FakeCachedCatalog(
        private val searchResult: Result<List<CatalogGame>>,
        private val servingFromCache: Boolean = false
    ) : GameCatalog {
        override suspend fun search(query: String): Result<List<CatalogGame>> = searchResult
        override suspend fun getDetails(gameId: Int): Result<CatalogGame?> = Result.success(null)
        override suspend fun popular(limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
        override suspend fun byGenres(genres: List<String>, limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
        override suspend fun discover(seed: Long, limit: Int): Result<List<CatalogGame>> =
            Result.success(emptyList())
        override suspend fun isServingFromCache(): Boolean = servingFromCache
    }

    @Test
    fun `fetchGames refleja el modo degradado del catalogo`() = runTest {
        val catalog = FakeCachedCatalog(
            searchResult = Result.success(listOf(game(1, "Zelda"))),
            servingFromCache = true
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("zelda")

        assertTrue(viewModel.fromCache.value)
    }

    @Test
    fun `fetchGames con fallo apaga el modo degradado`() = runTest {
        val catalog = FakeGameCatalog(
            searchResult = Result.failure(RuntimeException("sin conexión"))
        )
        val viewModel = SearchViewModel(catalog)

        viewModel.fetchGames("zelda")

        assertFalse(viewModel.fromCache.value)
    }
}
