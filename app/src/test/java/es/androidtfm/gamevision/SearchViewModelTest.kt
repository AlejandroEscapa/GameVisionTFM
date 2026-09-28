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

        assertTrue(viewModel.error.value?.contains("Error al cargar juegos") == true)
        assertTrue(viewModel.games.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
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

        assertTrue(viewModel.errorDetails.value?.contains("Error al cargar detalles") == true)
        assertNull(viewModel.gameDetails.value)
    }
}
