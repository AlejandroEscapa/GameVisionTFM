package es.androidtfm.gamevision

import es.androidtfm.gamevision.retrofit.ApiResponse
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.retrofit.GameApiService
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
 * Tests unitarios de SearchViewModel usando un fake de GameApiService (sin red ni mocking).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    private class FakeGameApiService(
        private val searchResponse: ApiResponse = ApiResponse(0, null, null, emptyList()),
        private val game: Game? = null,
        private val failSearch: Boolean = false
    ) : GameApiService {
        var lastSearchQuery: String? = null
            private set

        override suspend fun searchGames(search: String, key: String): ApiResponse {
            lastSearchQuery = search
            if (failSearch) throw RuntimeException("network down")
            return searchResponse
        }

        override suspend fun getGameDetails(gameId: Int, key: String): Game =
            game ?: throw RuntimeException("not found")
    }

    private fun testGame(id: Int, name: String) = Game(
        slug = "slug-$id",
        name = name,
        playtime = 10,
        platforms = emptyList(),
        stores = emptyList(),
        released = "2025-01-01",
        tba = false,
        backgroundImage = "https://example.com/$id.jpg",
        rating = 4.5,
        ratingTop = 5,
        ratings = emptyList(),
        ratingsCount = 100,
        reviewsTextCount = 10,
        added = 200,
        addedByStatus = null,
        metacritic = 80,
        suggestionsCount = 5,
        updated = "2025-06-01",
        id = id,
        score = null,
        clip = null,
        tags = emptyList(),
        esrbRating = null,
        userGame = null,
        reviewsCount = 50,
        saturatedColor = "#0f0f0f",
        dominantColor = "#0f0f0f",
        shortScreenshots = emptyList(),
        parentPlatforms = emptyList(),
        genres = emptyList()
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
        val games = listOf(testGame(1, "Zelda"), testGame(2, "Mario"))
        val viewModel = SearchViewModel(FakeGameApiService(searchResponse = ApiResponse(2, null, null, games)))

        viewModel.fetchGames("  zelda \"\" ")

        assertEquals(listOf("Zelda", "Mario"), viewModel.games.value.map { it.name })
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `fetchGames limpia la consulta enviada a la API`() = runTest {
        val api = FakeGameApiService()
        val viewModel = SearchViewModel(api)

        viewModel.fetchGames("  metroid \"prime\"  ")

        assertEquals("metroid prime", api.lastSearchQuery)
    }

    @Test
    fun `fetchGames con fallo de red publica el error y vacia resultados`() = runTest {
        val viewModel = SearchViewModel(FakeGameApiService(failSearch = true))

        viewModel.fetchGames("zelda")

        assertTrue(viewModel.error.value?.contains("Error al cargar juegos") == true)
        assertTrue(viewModel.games.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchGameDetails con exito actualiza los detalles`() = runTest {
        val viewModel = SearchViewModel(FakeGameApiService(game = testGame(7, "Hollow Knight")))

        viewModel.fetchGameDetails(7)

        assertEquals("Hollow Knight", viewModel.gameDetails.value?.name)
        assertFalse(viewModel.isLoadingDetails.value)
        assertNull(viewModel.errorDetails.value)
    }
}
