package es.androidtfm.gamevision

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import es.androidtfm.gamevision.datastore.ThemeDataStore
import es.androidtfm.gamevision.viewmodel.ThemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * Tests unitarios de ThemeViewModel sobre un DataStore real.
 *
 * El scope del ViewModel se inyecta y se cancela en `@After` (antes de resetMain)
 * para que ninguna corrutina sobreviva al test. DataStore trabaja en hilos reales
 * de E/S: las esperas se hacen en tiempo real, no en el tiempo virtual de runTest.
 * Los ficheros de DataStore se crean en tmp y no se borran en caliente (un DataStore
 * activo no admite apagado limpio).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModelScope: CoroutineScope

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): ThemeViewModel {
        viewModelScope = CoroutineScope(SupervisorJob() + mainDispatcher)
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                File(
                    System.getProperty("java.io.tmpdir"),
                    "gamevision-theme-test-${UUID.randomUUID()}.preferences_pb"
                )
            }
        )
        val viewModel = ThemeViewModel(Application(), ThemeDataStore(Application(), dataStore), viewModelScope)
        // Emula al colector de la UI (collectAsState): Lazily solo arranca el upstream con un colector
        viewModelScope.launch { viewModel.isDarkTheme.collect { } }
        return viewModel
    }

    /**
     * Dentro de runTest el tiempo es virtual: para esperar E/S real de DataStore
     * se ejecuta la espera en un dispatcher de tiempo real.
     */
    private suspend fun <T> awaitReal(block: suspend () -> T): T =
        withContext(Dispatchers.Default.limitedParallelism(1)) { withTimeout(10_000) { block() } }

    @Test
    fun `por defecto el tema es claro`() = runTest {
        val viewModel = createViewModel()

        val initial = awaitReal { viewModel.themeDataStore.isDarkTheme.first() }

        assertFalse(initial)
    }

    @Test
    fun `toggleTheme cambia de tema claro a oscuro y viceversa`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleTheme()
        assertTrue(awaitReal { viewModel.isDarkTheme.first { it } })

        viewModel.toggleTheme()
        assertFalse(awaitReal { viewModel.isDarkTheme.first { !it } })
    }
}
