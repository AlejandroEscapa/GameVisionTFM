package es.androidtfm.gamevision.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import es.androidtfm.gamevision.ui.designsystem.GVMotion
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import es.androidtfm.gamevision.datastore.ThemeDataStore
import es.androidtfm.gamevision.ui.views.composables.EditProfileScreen
import es.androidtfm.gamevision.ui.views.composables.DiaryScreen
import es.androidtfm.gamevision.ui.views.composables.SettingsScreen
import es.androidtfm.gamevision.ui.views.composables.FriendsList
import es.androidtfm.gamevision.ui.views.composables.GameDetails
import es.androidtfm.gamevision.ui.views.composables.GameListScreen
import es.androidtfm.gamevision.ui.designsystem.GVSharedElementProvider
import es.androidtfm.gamevision.ui.views.composables.HomeScreen
import es.androidtfm.gamevision.ui.views.composables.LoginScreen
import es.androidtfm.gamevision.ui.views.composables.NewsScreen
import es.androidtfm.gamevision.ui.views.composables.PassScreen
import es.androidtfm.gamevision.ui.views.composables.ProfileScreen
import es.androidtfm.gamevision.ui.views.composables.PublicProfileScreen
import es.androidtfm.gamevision.ui.views.composables.RegisterScreen
import es.androidtfm.gamevision.ui.views.composables.SearchScreen
import es.androidtfm.gamevision.ui.views.composables.SocialScreen
import es.androidtfm.gamevision.ui.views.composables.StatsScreen
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 15/01/2025
 * Descripción: 
 */

@Composable
fun NavHost(
    navController: NavHostController,
    themeDataStore: ThemeDataStore,
    onThemeChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    newsViewModel: NewsViewModel,
    onGoogleSignInClick: () -> Unit,
    libraryViewModel: LibraryViewModel,
    socialViewModel: SocialViewModel,
    isGuest: Boolean,
    searchViewModel: SearchViewModel
) {
    val isDarkTheme by themeDataStore.isDarkTheme.collectAsStateWithLifecycle(initialValue = false)
    // Scope para el alta de invitado (suspend) desde un callback no composable
    val guestScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "main",
        // Transiciones de navegación unificadas (criterio del propietario: la
        // default se siente barata). Patrón estándar Android: la nueva pantalla
        // entra desde la derecha, la anterior sale un tercio hacia la izquierda
        // con fade; al volver (gesto o botón atrás) se invierte. Física del
        // design system (GVMotion), 300 ms.
        enterTransition = {
            slideInHorizontally(tween(300, easing = GVMotion.EnterEasing)) { it } +
                fadeIn(tween(300, easing = GVMotion.EnterEasing))
        },
        exitTransition = {
            slideOutHorizontally(tween(300, easing = GVMotion.ExitEasing)) { -it / 3 } +
                fadeOut(tween(300, easing = GVMotion.ExitEasing))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300, easing = GVMotion.EnterEasing)) { -it / 3 } +
                fadeIn(tween(300, easing = GVMotion.EnterEasing))
        },
        popExitTransition = {
            slideOutHorizontally(tween(300, easing = GVMotion.ExitEasing)) { it } +
                fadeOut(tween(300, easing = GVMotion.ExitEasing))
        }
    ) {
        // Pantalla principal de la aplicación
        composable("main") {
            HomeScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                navController = navController,
                isGuest = isGuest,
                onGuestStatusChange = { guestStatus ->
                    if (guestStatus) guestScope.launch { userViewModel.enterAsGuest() }
                }
            )
        }

        // Pantalla de Login
        composable("login") {
            LoginScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                navconThemeChange = onThemeChange,
                userViewModel = userViewModel,
                googleViewModel = googleViewModel,
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        // Pantalla de noticias: Es la pantalla principal donde se carga la información del usuario (fetch centralizado)
        composable("news") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel // Se utiliza el ViewModel compartido
            ) { paddingValues ->
                NewsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    newsViewModel = newsViewModel,
                    userViewModel = userViewModel,
                    paddingValues = paddingValues
                )
            }
        }

        // Pantalla del perfil
        composable("profile") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    googleViewModel = googleViewModel
                )
            }
        }

        // Pantalla de lista de juegos
        composable("gamelist") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                GVSharedElementProvider(animatedVisibilityScope = this@composable) {
                GameListScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    paddingValues = paddingValues,
                    libraryViewModel = libraryViewModel,
                    userViewModel = userViewModel,
                    socialViewModel = socialViewModel
                )
                }
            }
        }

        // Pantalla de búsqueda de juegos
        composable("gameSearch") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                GVSharedElementProvider(animatedVisibilityScope = this@composable) {
                SearchScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    viewModel = searchViewModel,
                    paddingValues = paddingValues
                )
                }
            }
        }

        // Pantalla de registro
        composable("register") {
            RegisterScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                userViewModel = userViewModel
            )
        }

        // Pantalla de recuperación de contraseña
        composable("passrecover") {
            PassScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                userViewModel = userViewModel // SSOT compartido
            )
        }

        // Pantalla de edición del perfil
        // Ajustes (iteración 02/10): modo noche real + esqueleto de secciones
        composable("ajustes") {
            SettingsScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("editProfile") {
            EditProfileScreen(
                isDarkTheme = isDarkTheme,
                paddingValues = PaddingValues(),
                navController = navController,
                userViewModel = userViewModel,
                socialViewModel = socialViewModel,
                libraryViewModel = libraryViewModel
            )
        }

        // Pantalla de estadísticas (F1 — Bloque 3)
        composable("stats") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                StatsScreen(
                    navController = navController,
                    paddingValues = paddingValues,
                    libraryViewModel = libraryViewModel,
                    userViewModel = userViewModel
                )
            }
        }

        // Pantalla del diario (F1 — Bloque 2)
        composable("diary") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                DiaryScreen(
                    navController = navController,
                    paddingValues = paddingValues,
                    libraryViewModel = libraryViewModel,
                    userViewModel = userViewModel
                )
            }
        }

        // Pantalla de la red social (timeline)
        composable("social") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                SocialScreen(
                    isDarkTheme = isDarkTheme,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    libraryViewModel = libraryViewModel,
                    // OJO (probado en E2E): sin esto se evalúa el default viewModel() del
                    // composable, que no tiene la factory de @HiltViewModel y CRASHEA al
                    // abrir la pestaña social. Todas las rutas pasan sus ViewModels explícitos.
                    socialViewModel = socialViewModel
                )
            }
        }

        // Pantalla de detalles del juego
        composable("gameDetails/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull()
            val parentEntry = remember(backStackEntry) {
                // Verifica si la ruta "gameSearch" está en la pila de navegación
                if (navController.currentBackStackEntry?.destination?.route == "gameSearch") {
                    navController.getBackStackEntry("gameSearch")
                } else {
                    backStackEntry
                }
            }
            val sharedViewModel: SearchViewModel = viewModel(parentEntry)

            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                GVSharedElementProvider(animatedVisibilityScope = this@composable) {
                gameId?.let {
                    GameDetails(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        viewModel = sharedViewModel,
                        paddingValues = paddingValues,
                        gameId = it,
                        libraryViewModel = libraryViewModel,
                        userViewModel = userViewModel,
                        socialViewModel = socialViewModel
                    )
                }
                }
            }
        }

        // Perfil público de otro jugador (F2/T2.4): {uid} es la clave — ADR-0008.
        composable(
            route = "publicProfile/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileUid = backStackEntry.arguments?.getString("uid").orEmpty()
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                PublicProfileScreen(
                    profileUid = profileUid,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    socialViewModel = socialViewModel
                )
            }
        }

        // Pantalla de amigos
        composable("friendlist") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                FriendsList(
                    isDarkTheme = isDarkTheme,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    socialViewModel = socialViewModel
                )
            }
        }
    }
}