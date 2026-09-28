package es.androidtfm.gamevision

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme
import es.androidtfm.gamevision.ui.designsystem.components.EmptyState
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.ui.designsystem.components.RatingBadge
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI del design system (requieren dispositivo o emulador):
 * ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class DesignSystemUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ratingBadge_muestra_la_puntuacion() {
        composeRule.setContent {
            GameVisionTheme(darkTheme = true) { RatingBadge(rating = "4.4") }
        }

        composeRule.onNodeWithText("★ 4.4").assertIsDisplayed()
    }

    @Test
    fun emptyState_muestra_titulo_y_pista() {
        composeRule.setContent {
            GameVisionTheme(darkTheme = true) {
                EmptyState(title = "Sin resultados", hint = "Prueba otra búsqueda")
            }
        }

        composeRule.onNodeWithText("Sin resultados").assertIsDisplayed()
        composeRule.onNodeWithText("Prueba otra búsqueda").assertIsDisplayed()
    }

    @Test
    fun boton_primario_muestra_su_texto() {
        composeRule.setContent {
            GameVisionTheme(darkTheme = true) { GVButton(text = "Añadir", onClick = {}) }
        }

        composeRule.onNodeWithText("Añadir").assertIsDisplayed()
    }
}
