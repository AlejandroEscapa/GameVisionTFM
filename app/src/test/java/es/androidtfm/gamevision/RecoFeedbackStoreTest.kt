package es.androidtfm.gamevision

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import es.androidtfm.gamevision.data.library.RecoFeedbackStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

/*
 * Tests del feedback ligero de la recomendación (F3/T3.5, decisión D3.8).
 *
 * Sobre un DataStore real en tmp, como ThemeViewModelTest: DataStore trabaja en
 * hilos reales de E/S, así que las lecturas usan first() en tiempo real.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecoFeedbackStoreTest {

    private fun createStore() = RecoFeedbackStore(
        // El contexto no se toca cuando hay override (igual que ThemeViewModelTest).
        context = android.app.Application(),
        dataStoreOverride = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
            produceFile = {
                File(
                    System.getProperty("java.io.tmpdir"),
                    "gamevision-reco-test-${UUID.randomUUID()}.preferences_pb"
                )
            }
        )
    )

    @Test
    fun recien_creado_no_hay_feedback() = runTest {
        val fb = createStore().feedback("uid1").first()
        assertTrue(fb.descartados.isEmpty())
        assertTrue(fb.aceptados.isEmpty())
    }

    @Test
    fun descartar_y_aceptar_se_registran() = runTest {
        val store = createStore()
        store.registrarDescartado("uid1", "juego-a")
        store.registrarAceptado("uid1", "juego-b")
        val fb = store.feedback("uid1").first()
        assertEquals(setOf("juego-a"), fb.descartados)
        assertEquals(setOf("juego-b"), fb.aceptados)
    }

    @Test
    fun descartar_saca_de_aceptados_y_viceversa() = runTest {
        val store = createStore()
        store.registrarAceptado("uid1", "juego-a")
        store.registrarDescartado("uid1", "juego-a")
        val fb = store.feedback("uid1").first()
        assertEquals(setOf("juego-a"), fb.descartados)
        assertTrue(fb.aceptados.isEmpty())
        store.registrarAceptado("uid1", "juego-a")
        val fb2 = store.feedback("uid1").first()
        assertEquals(setOf("juego-a"), fb2.aceptados)
        assertTrue(fb2.descartados.isEmpty())
    }

    @Test
    fun limpiar_descartados_vuelve_a_mostrar() = runTest {
        val store = createStore()
        store.registrarDescartado("uid1", "juego-a")
        store.limpiarDescartados("uid1")
        assertTrue(store.feedback("uid1").first().descartados.isEmpty())
    }

    @Test
    fun el_feedback_es_por_usuario() = runTest {
        val store = createStore()
        store.registrarDescartado("uid1", "juego-a")
        assertTrue(store.feedback("uid2").first().descartados.isEmpty())
    }
}
