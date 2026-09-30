package es.androidtfm.gamevision

import es.androidtfm.gamevision.retrofit.Article
import es.androidtfm.gamevision.retrofit.New
import es.androidtfm.gamevision.retrofit.NewsApiService
import es.androidtfm.gamevision.retrofit.Source
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios de NewsViewModel usando un fake de NewsApiService (sin red ni mocking).
 */
class NewsViewModelTest {

    private class FakeNewsApiService(private val response: New) : NewsApiService {
        var lastQuery: String? = null
            private set
        var lastApiKey: String? = null
            private set

        override suspend fun getEverything(query: String, apiKey: String): New {
            lastQuery = query
            lastApiKey = apiKey
            return response
        }
    }

    /** Fake que simula un fallo de red (sin conexión). */
    private class ThrowingNewsApiService : NewsApiService {
        override suspend fun getEverything(query: String, apiKey: String): New =
            throw java.net.UnknownHostException("newsapi.org")
    }

    private fun article(
        title: String,
        publishedAt: String,
        urlToImage: String? = "https://example.com/img.jpg"
    ) = Article(
        source = Source(id = null, name = "Test"),
        author = null,
        title = title,
        description = "",
        url = "https://example.com",
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = ""
    )

    @Test
    fun `filtra articulos Removed y sin imagen`() = runTest {
        val api = FakeNewsApiService(
            New(
                status = "ok",
                totalResults = 3,
                articles = listOf(
                    article("Noticia válida", "2026-01-01T10:00:00Z"),
                    article("[Removed]", "2026-01-01T11:00:00Z"),
                    article("Sin imagen", "2026-01-01T12:00:00Z", urlToImage = null)
                )
            )
        )
        val viewModel = NewsViewModel(api)

        val result = viewModel.fetchFilteredNews("gaming")

        assertEquals(listOf("Noticia válida"), result.map { it.title })
        assertEquals("gaming", api.lastQuery)
    }

    @Test
    fun `ordena por fecha descendente`() = runTest {
        val api = FakeNewsApiService(
            New(
                status = "ok",
                totalResults = 3,
                articles = listOf(
                    article("Antigua", "2026-01-01T08:00:00Z"),
                    article("Reciente", "2026-01-01T12:00:00Z"),
                    article("Intermedia", "2026-01-01T10:00:00Z")
                )
            )
        )
        val viewModel = NewsViewModel(api)

        val result = viewModel.fetchFilteredNews("gaming")

        assertEquals(listOf("Reciente", "Intermedia", "Antigua"), result.map { it.title })
    }

    @Test
    fun `limita el resultado a 25 articulos`() = runTest {
        val articles = (1..30).map {
            article("Noticia $it", "2026-01-01T10:00:00Z")
        }
        val viewModel = NewsViewModel(FakeNewsApiService(New("ok", 30, articles)))

        val result = viewModel.fetchFilteredNews("gaming")

        assertEquals(25, result.size)
    }

    @Test
    fun `sin conexion devuelve lista vacia y marca loadFailed`() = runTest {
        val viewModel = NewsViewModel(ThrowingNewsApiService())

        val result = viewModel.fetchFilteredNews("gaming")

        assertTrue(result.isEmpty())
        assertTrue(viewModel.loadFailed.value)
    }

    @Test
    fun `formatPublishedAt formatea fecha ISO 8601`() {
        val viewModel = NewsViewModel(FakeNewsApiService(New("ok", 0, emptyList())))

        val formatted = viewModel.formatPublishedAt("2026-01-05T14:30:00Z")

        assertEquals("14:30 05-01", formatted) // formato del propietario: HH:mm dd-MM (sin año)
    }

    @Test
    fun `formatPublishedAt devuelve cadena vacia con formato invalido`() {
        val viewModel = NewsViewModel(FakeNewsApiService(New("ok", 0, emptyList())))

        assertTrue(viewModel.formatPublishedAt("no-es-una-fecha").isEmpty())
    }
}
