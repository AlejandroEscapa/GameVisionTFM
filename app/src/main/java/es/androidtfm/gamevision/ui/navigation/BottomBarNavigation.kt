package es.androidtfm.gamevision.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: navegación adaptativa (ver DESIGN.md).
 *
 * Teléfono (ancho compacto): Scaffold + NavigationBar.
 * Tablet/ventana ancha: NavigationSuiteScaffold con rail/drawer automático.
 */

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppScaffold(
    navController: NavController,
    userViewModel: UserViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val isGuest by userViewModel.isGuest.collectAsState()

    val items = if (isGuest) {
        listOf(
            BottomNavItem("news", Icons.Default.Home, "Home"),
            BottomNavItem("gameSearch", Icons.Filled.Search, "Search")
        )
    } else {
        listOf(
            BottomNavItem("gameSearch", Icons.Filled.Search, "Search"),
            BottomNavItem("gamelist", Icons.AutoMirrored.Filled.List, "Game List"),
            BottomNavItem("diary", Icons.AutoMirrored.Filled.MenuBook, "Diario"),
            BottomNavItem("news", Icons.Default.Home, "Home"),
            BottomNavItem("profile", Icons.Default.Person, "Profile"),
            BottomNavItem("social", Icons.Default.Face, "Social")
        )
    }

    // API adaptativa V2 (material3-adaptive 1.3 + window-core 1.5): ancho compacto =
    // por debajo del breakpoint medio (soporta además L y XL como no compacto).
    val isCompactWidth = !currentWindowAdaptiveInfoV2()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (isCompactWidth) {
        // Teléfono: barra inferior (comportamiento verificado)
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BottomNavigationBar(navController, items) }
        ) { innerPadding ->
            content(innerPadding)
        }
    } else {
        // Ventana ancha: rail lateral automático vía NavigationSuiteScaffold
        NavigationSuiteScaffold(
            containerColor = MaterialTheme.colorScheme.background,
            navigationSuiteItems = {
                items.forEach { item ->
                    item(
                        selected = navController.currentDestination?.route == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            },
            content = { content(PaddingValues(0.dp)) }
        )
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController, items: List<BottomNavItem>) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
    ) {
        items.forEach { item ->
            val isSelected = navController.currentDestination?.route == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { navController.navigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}
