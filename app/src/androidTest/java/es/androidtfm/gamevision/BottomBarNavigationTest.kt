package es.androidtfm.gamevision

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.androidtfm.gamevision.ui.navigation.BottomNavItem
import es.androidtfm.gamevision.ui.navigation.ModernStyledNavigationBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI Compose para la barra de navegación inferior.
 * Requieren un dispositivo o emulador conectado (connectedDebugAndroidTest).
 */
@RunWith(AndroidJUnit4::class)
class BottomBarNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBar_muestra_todos_los_items() {
        val items = listOf(
            BottomNavItem("news", Icons.Default.Home, "Home"),
            BottomNavItem("gamesearch", Icons.Filled.Search, "Search")
        )

        composeRule.setContent {
            ModernStyledNavigationBar(rememberNavController(), items)
        }

        composeRule.onNodeWithContentDescription("Home").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }
}
