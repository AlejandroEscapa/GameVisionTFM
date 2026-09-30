package es.androidtfm.gamevision.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import es.androidtfm.gamevision.ui.designsystem.GVMotion
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Dock flotante de navegación ("Galería", barra 2026 — ver
 * docs/plan/barra-y-home-2026.md).
 *
 * La píldora se despega del borde (márgenes laterales e inferiores), respira
 * sobre el fondo con superficie translúcida y hairline — sin sombra: la
 * separación la da el tono, como manda el design system. El activo se anuncia
 * con relleno suave del acento + micro-escala del icono (muelle). Ventana
 * ancha: rail M3 automático (igual que antes).
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
    val isGuest by userViewModel.isGuest.collectAsStateWithLifecycle()

    val items = if (isGuest) {
        listOf(
            BottomNavItem("news", Icons.Default.Newspaper, "Noticias"),
            BottomNavItem("gameSearch", Icons.Filled.Search, "Buscar")
        )
    } else {
        listOf(
            BottomNavItem("gameSearch", Icons.Filled.Search, "Buscar"),
            BottomNavItem("gamelist", Icons.AutoMirrored.Filled.List, "Biblioteca"),
            BottomNavItem("diary", Icons.AutoMirrored.Filled.MenuBook, "Diario"),
            BottomNavItem("news", Icons.Default.Newspaper, "Noticias"),
            BottomNavItem("profile", Icons.Default.Person, "Perfil"),
            BottomNavItem("social", Icons.Default.Face, "Social")
        )
    }

    // API adaptativa V2: ancho compacto = por debajo del breakpoint medio.
    val isCompactWidth = !currentWindowAdaptiveInfoV2()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (isCompactWidth) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { GVDock(navController, items) }
        ) { innerPadding ->
            content(innerPadding)
        }
    } else {
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
private fun GVDock(navController: NavController, items: List<BottomNavItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50)
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = navController.currentDestination?.route == item.route
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    animationSpec = GVMotion.springStandard(),
                    label = "dock-tint"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = GVMotion.springBouncy(),
                    label = "dock-scale"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(item.route) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
