package es.androidtfm.gamevision

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Smoke test instrumentado (F0-B / Bloque 2 de deuda técnica).
 *
 * Objetivo mínimo pero real: la app arranca sin crash y llega a una pantalla
 * principal, tanto en frío (bienvenida) como con sesión (barra inferior).
 * Se ejecuta en el emulador con:  ./gradlew :app:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun arrancaYmuestraPantallaPrincipal() {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            // Bienvenida en frío (textos en español, DESIGN.md §8) o dock con sesión.
            val welcome = composeRule.onAllNodesWithText("Iniciar sesión")
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Explorar sin cuenta")
                    .fetchSemanticsNodes().isNotEmpty()
            val bottomBar = composeRule.onAllNodesWithContentDescription("Buscar")
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithContentDescription("Inicio")
                    .fetchSemanticsNodes().isNotEmpty()
            welcome || bottomBar
        }
    }
}
