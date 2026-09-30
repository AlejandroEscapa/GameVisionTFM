package es.androidtfm.gamevision

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import es.androidtfm.gamevision.ui.designsystem.LocalSharedTransitionScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import es.androidtfm.gamevision.data.session.SessionState
import dagger.hilt.android.AndroidEntryPoint
import es.androidtfm.gamevision.ui.navigation.NavHost
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.ThemeViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val googleViewModel: GoogleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Observar el estado del inicio de sesión de Google
        googleViewModel.signInState.observe(this) { state ->
            when (state) {
                is GoogleViewModel.SignInState.Success -> {
                    Log.d("MainActivity", "Usuario autenticado con UID: ${state.userUid}")
                }
                is GoogleViewModel.SignInState.Error -> {
                    Log.e("MainActivity", state.message)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* Estado Idle o Loading: No hacer nada */ }
            }
        }

        setContent {
            // Se instancian los ViewModels compartidos a nivel de actividad (Hilt los provee)
            val themeViewModel: ThemeViewModel = viewModel()
            val userViewModel: UserViewModel = viewModel()
            val newsViewModel: NewsViewModel = viewModel()
            val libraryViewModel: LibraryViewModel = viewModel()
            val socialViewModel: SocialViewModel = viewModel()
            val searchViewModel: SearchViewModel = viewModel()

            MainScreen(
                window = window,
                themeViewModel = themeViewModel,
                userViewModel = userViewModel,
                googleViewModel = googleViewModel,
                newsViewModel = newsViewModel,
                onGoogleSignInClick = {
                    lifecycleScope.launch { googleViewModel.signIn(this@MainActivity) }
                },
                libraryViewModel = libraryViewModel,
                socialViewModel = socialViewModel,
                searchViewModel = searchViewModel // Si NewsScreen u otras pantallas lo requieren
            )
        }
    }
}

@Composable
fun MainScreen(
    window: Window,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    newsViewModel: NewsViewModel,
    onGoogleSignInClick: () -> Unit,
    libraryViewModel: LibraryViewModel,
    socialViewModel: SocialViewModel,
    searchViewModel: SearchViewModel
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val themeDataStore = themeViewModel.themeDataStore
    // Estado de invitado (derivado del SSOT de sesión)
    val isGuest by userViewModel.isGuest.collectAsStateWithLifecycle()

    var isNavHostInitialized by remember { mutableStateOf(false) }

    GameVisionTheme(darkTheme = isDarkTheme) {
        // Apariencia de los iconos de las barras del sistema (edge-to-edge: las barras son transparentes)
        SystemBarAppearance(window, isDarkTheme)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // El contenido se retira de las barras del sistema; el fondo llega hasta el borde
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {
            // Shared elements habilitados para todo el grafo (la portada vuela al detalle)
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    // Se invoca el NavGraph compartido, pasando los ViewModels ya instanciados
                    NavHost(
                        navController = navController,
                        themeDataStore = themeDataStore,
                        onThemeChange = { themeViewModel.toggleTheme() },
                        userViewModel = userViewModel,
                        googleViewModel = googleViewModel,
                        newsViewModel = newsViewModel,
                        onGoogleSignInClick = onGoogleSignInClick,
                        libraryViewModel = libraryViewModel,
                        socialViewModel = socialViewModel,
                        isGuest = isGuest,
                        searchViewModel = searchViewModel
                    )
                }

                // Se controla la inicialización del NavHost
                LaunchedEffect(navController) {
                    isNavHostInitialized = true
                }
            }
            }
        }
    }

    // Navegación automática guiada por el SSOT de sesión: al iniciar sesión (o al
    // reabrir la app con sesión activa) se entra en la pantalla principal.
    val session by userViewModel.session.collectAsStateWithLifecycle()
    LaunchedEffect(isNavHostInitialized, session) {
        if (isNavHostInitialized && session is SessionState.LoggedIn) {
            navController.navigate("news") {
                popUpTo("main") { inclusive = true }
            }
        }
    }
}

/*
 * Apariencia de los iconos de las barras del sistema (claro/oscuro).
 * Con edge-to-edge las barras son transparentes: el color lo pone el fondo del contenido.
 */
@Composable
fun SystemBarAppearance(window: Window, isDarkTheme: Boolean) {
    SideEffect {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }
}