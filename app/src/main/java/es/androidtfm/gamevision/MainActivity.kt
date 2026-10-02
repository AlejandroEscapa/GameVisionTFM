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
import es.androidtfm.gamevision.ui.designsystem.LocalReduceMotion
import es.androidtfm.gamevision.ui.designsystem.rememberSystemReduceMotion
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
import es.androidtfm.gamevision.viewmodel.SteamViewModel
import es.androidtfm.gamevision.viewmodel.ThemeViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Retorno del OpenID de Steam (bloque 5): el navegador devuelve a
    // gamevision://steam/linked?steamid=…; onNewIntent lo publica y el
    // NavHost lo consume (se anula al leerlo).
    private val steamLinkState = androidx.compose.runtime.mutableStateOf<String?>(null)

    // Aviso de fin de año (F3/T3.11): la notificación abre gamevision://rewind y el
    // NavHost navega al Rewind y consume la marca, igual que hace con Steam.
    private val abrirRewindState = androidx.compose.runtime.mutableStateOf(false)

    /** Lee la URI del intent (tanto el inicial como los posteriores). */
    private fun registrarIntentDelAviso(intent: android.content.Intent?) {
        val uri = intent?.data?.toString() ?: return
        if (uri.startsWith("gamevision://steam/linked")) {
            steamLinkState.value = uri.substringAfter("steamid=")
        }
        if (uri.startsWith("gamevision://rewind")) {
            abrirRewindState.value = true
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        registrarIntentDelAviso(intent)
    }
    private val googleViewModel: GoogleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // La app puede nacer de la notificación del Rewind: hay que leer el intent inicial,
        // no solo los posteriores (`onNewIntent`).
        registrarIntentDelAviso(intent)

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
            val steamViewModel: SteamViewModel = viewModel()

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
                searchViewModel = searchViewModel, // Si NewsScreen u otras pantallas lo requieren
                steamViewModel = steamViewModel,
                steamLink = steamLinkState.value,
                onSteamLinkConsumido = { steamLinkState.value = null },
                abrirRewind = abrirRewindState.value,
                onRewindConsumido = { abrirRewindState.value = false }
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
    searchViewModel: SearchViewModel,
    steamViewModel: SteamViewModel,
    steamLink: String?,
    onSteamLinkConsumido: () -> Unit,
    abrirRewind: Boolean = false,
    onRewindConsumido: () -> Unit = {}
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val themeDataStore = themeViewModel.themeDataStore
    // Estado de invitado (derivado del SSOT de sesión)
    val isGuest by userViewModel.isGuest.collectAsStateWithLifecycle()

    var isNavHostInitialized by remember { mutableStateOf(false) }

    // Preferencia de «reducir movimiento» del sistema (ADR-0013 §5.2). Se lee una
    // vez aquí y se provee a toda la app: hasta ahora LocalReduceMotion estaba
    // declarado en GVMotion con valor por defecto fijo y nadie lo conectaba.
    val reduceMotion = rememberSystemReduceMotion()

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
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this,
                    LocalReduceMotion provides reduceMotion
                ) {
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
                        searchViewModel = searchViewModel,
                        steamViewModel = steamViewModel,
                        steamLink = steamLink,
                        onSteamLinkConsumido = onSteamLinkConsumido,
                        abrirRewind = abrirRewind,
                        onRewindConsumido = onRewindConsumido
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
    // reabrir la app con sesión activa) se entra en la pantalla principal — o en
    // el onboarding, si el perfil aún no lo tiene marcado (bloque C). Este es el
    // ÚNICO punto de navegación post-login: si hubiera otro, se pisarían.
    val session by userViewModel.session.collectAsStateWithLifecycle()
    val perfilSesion by userViewModel.profile.collectAsStateWithLifecycle()
    // Se resuelve una sola vez por sesión: si el perfil cambia después (editar
    // perfil, terminar el onboarding), no queremos arrastrar al usuario de vuelta.
    var arranqueResuelto by remember { mutableStateOf(false) }
    val perfilCargado = perfilSesion.email.isNotBlank() // el documento real siempre trae email
    LaunchedEffect(isNavHostInitialized, session, perfilCargado) {
        if (isNavHostInitialized && session is SessionState.LoggedIn && perfilCargado && !arranqueResuelto) {
            arranqueResuelto = true
            val destino = if (perfilSesion.onboardingDone) "home" else "onboarding"
            navController.navigate(destino) {
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