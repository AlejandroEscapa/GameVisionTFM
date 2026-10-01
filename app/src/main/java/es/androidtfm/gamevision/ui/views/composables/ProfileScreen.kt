package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults
import com.composables.icons.lucide.ChartBarBig
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Users
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.data.library.TopGamesLogic
import es.androidtfm.gamevision.data.model.TopGame
import es.androidtfm.gamevision.ui.designsystem.components.GVSkeleton
import es.androidtfm.gamevision.ui.views.composables.profile.CrearListaDialog
import es.androidtfm.gamevision.ui.views.composables.profile.MisListasSection
import es.androidtfm.gamevision.ui.views.composables.profile.TopGamesCard
import es.androidtfm.gamevision.ui.views.composables.profile.resolvedorDesdeBiblioteca
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.X

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 18/01/2025
 * Descripción:
 */

/**
 * Pantalla de perfil del usuario logueado.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param paddingValues Valores de padding para la pantalla.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para manejar los datos del usuario.
 * @param googleViewModel ViewModel para manejar la autenticación con Google.
 * @param onThemeChange Función para cambiar el tema.
 */
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?, // Se usa para acceder al SavedStateHandle y para la navegación
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    libraryViewModel: LibraryViewModel,
    socialViewModel: SocialViewModel,
    searchViewModel: SearchViewModel
) {
    // Perfil en vivo desde el SSOT (UserViewModel.profile): se actualiza solo
    // cuando cambia el documento en Firestore, sin refetch manual por pantalla.
    val profile by userViewModel.profile.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val profileImageData by userViewModel.profileImageData.collectAsStateWithLifecycle()
    val imageError by userViewModel.imageError.collectAsStateWithLifecycle()
    val currentUid by userViewModel.currentUid.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // ---- Top 4 (T2.5, revisión 02/10): vive en el perfil, no en «Editar perfil»
    // La biblioteca solo hace falta como RESPALDO de las cuentas antiguas, que
    // guardaron los ids sin miniatura (ver UserProfile.topGamesResolved).
    val library by libraryViewModel.observeLibrary(currentUid.orEmpty())
        .collectAsStateWithLifecycle(initialValue = null)
    val seleccionTop = remember(profile.topGameIds, profile.topGames, library) {
        profile.topGamesResolved(resolvedorDesdeBiblioteca(library?.getOrNull()))
    }

    // Buscador del Top 4: estado propio (no comparte el de la pantalla Buscar).
    var consultaTop by remember { mutableStateOf("") }
    val resultadosTop by searchViewModel.topSearchResults.collectAsStateWithLifecycle()
    val buscandoTop by searchViewModel.topSearchLoading.collectAsStateWithLifecycle()
    val errorTop by searchViewModel.topSearchError.collectAsStateWithLifecycle()
    LaunchedEffect(consultaTop) {
        searchViewModel.buscarParaTop(consultaTop)
    }

    // ---- Listas curadas (T2.6): se crean y se ven AQUÍ, no en «Editar perfil»
    val listas by socialViewModel.lists.collectAsStateWithLifecycle()
    var mostrarCrearLista by remember { mutableStateOf(false) }
    LaunchedEffect(currentUid) {
        currentUid?.takeIf { it.isNotBlank() }?.let { socialViewModel.loadLists(it) }
    }

    // Reconciliación del Top 4 con su documento de lista. Hace falta para las
    // cuentas que YA tenían Top antes de esta revisión (solo `topGameIds`): sin
    // esto su lista `top4` no existiría hasta que volvieran a tocar el Top, y el
    // Top 4 no es un dato que caduque (si no existe, la lista está incompleta).
    // Se escribe UNA vez por valor y sesión de pantalla, no en cada recomposición.
    val topFirmado = profile.topGameIds.joinToString(",")
    var topSincronizado by remember(currentUid) { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUid, topFirmado, profile.isPrivate) {
        val uid = currentUid.orEmpty()
        if (uid.isNotBlank() && topSincronizado != topFirmado) {
            topSincronizado = topFirmado
            socialViewModel.syncTop4List(uid, profile.topGameIds, !profile.isPrivate) {
                socialViewModel.loadLists(uid)
            }
        }
    }

    fun persistirTop(nuevos: List<TopGame>) {
        // Solo el perfil: el documento de lista `top4` lo reconcilia el efecto de
        // arriba al cambiar `topGameIds`. Un único camino de sincronización.
        userViewModel.updateProfile(
            mapOf(
                "topGameIds" to TopGamesLogic.ids(nuevos),
                "topGames" to TopGame.toMapList(nuevos)
            )
        )
    }

    fun anadirAlTop(juego: TopGame) {
        val nuevos = TopGamesLogic.add(seleccionTop, juego) ?: return
        persistirTop(nuevos)
    }

    fun quitarDelTop(gameId: String) {
        persistirTop(TopGamesLogic.remove(seleccionTop, gameId))
    }

    // Imagen a mostrar: la gestionada por la app (data URI desde Firestore) o, si no,
    // una URL antigua que ya estuviera guardada en el perfil.
    val imageModel: String? = profileImageData
        ?: profile.imageUri.takeIf { it.isNotBlank() && !it.startsWith("firestore://") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // El padding del Scaffold va FUERA del scroll: si va dentro, el hueco
                // del dock no se reserva y el último elemento cae debajo de la barra.
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección del encabezado con fondo gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            ) {
                // Acceso a Ajustes (el modo noche vive allí desde la iteración 02/10)
                IconButton(
                    onClick = { navController?.navigate("ajustes") },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Lucide.Settings,
                        contentDescription = "Ajustes",
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }

            if (isLoading) {
                // Indicador de carga mientras se obtiene la información del perfil
                ProfileLoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 60.dp, bottom = 20.dp)
                ) {
                    // Imagen de perfil con efecto de offset
                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.CenterHorizontally)
                            .offset(y = (-25).dp)
                    ) {
                        ProfileImage(
                            imageUri = imageModel,
                            onImagePicked = { uri ->
                                coroutineScope.launch { userViewModel.updateProfileImage(uri) }
                            }
                        )
                    }

                    // Aviso si la foto elegida no cumple el límite de tamaño (escalabilidad del plan gratuito).
                    if (imageError != null) {
                        ProfileImageErrorBanner(
                            message = imageError.orEmpty(),
                            onDismiss = { userViewModel.clearImageError() },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Sección del encabezado del perfil
                    ProfileHeaderSection(
                        name = profile.nameSurname,
                        username = profile.username,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Tarjeta de detalles del perfil
                    ProfileDetailsCard(
                        description = profile.description,
                        country = profile.country,
                        email = profile.email
                    )

                    // ---- Top 4 (T2.5) ------------------------------------
                    // Justo debajo de la información del perfil: es la carta de
                    // presentación del jugador, antes que las acciones de la cuenta.
                    Spacer(modifier = Modifier.height(24.dp))
                    TopGamesCard(
                        seleccionados = seleccionTop,
                        editable = true,
                        resultados = resultadosTop,
                        buscando = buscandoTop,
                        errorBusqueda = errorTop,
                        onBuscar = { consultaTop = it },
                        onLimpiarBusqueda = {
                            consultaTop = ""
                            searchViewModel.limpiarBusquedaTop()
                        },
                        onAnadir = { anadirAlTop(it) },
                        onQuitar = { quitarDelTop(it) }
                    )

                    // ---- Mis listas (T2.6) -------------------------------
                    Spacer(modifier = Modifier.height(24.dp))
                    MisListasSection(
                        listas = listas?.getOrDefault(emptyList()).orEmpty(),
                        onCrear = { mostrarCrearLista = true },
                        onAbrirBiblioteca = { navController?.navigate("gamelist") }
                    )

                    // Sección de acciones del perfil (incluye el botón de "Cerrar sesión")
                    ProfileActionsSection(
                        navController = navController,
                        userViewModel = userViewModel,
                        googleViewModel = googleViewModel,
                        onNuevaLista = { mostrarCrearLista = true },
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    // Espacio adicional
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (mostrarCrearLista) {
        CrearListaDialog(
            onDismiss = { mostrarCrearLista = false },
            onCrear = { nombre, descripcion, esPublica ->
                mostrarCrearLista = false
                val uid = currentUid.orEmpty()
                if (uid.isNotBlank()) {
                    socialViewModel.createList(uid, nombre, descripcion, esPublica) { result ->
                        if (result.isSuccess) {
                            socialViewModel.loadLists(uid)
                        } else {
                            userViewModel.setMessage("No se pudo crear la lista")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileImage(
    imageUri: String?,
    onImagePicked: (Uri) -> Unit
) {    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // F0/T0.12: la imagen se sube a Storage desde el ViewModel; aquí solo se
        // entrega el Uri de contenido. La foto ya no se copia a filesDir (antes la
        // ruta local se guardaba en Firestore y no viajaba entre dispositivos).
        uri?.let(onImagePicked)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!imageUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { pickImageLauncher.launch("image/*") }
            )
        } else {
            Icon(
                imageVector = Lucide.Pencil,
                contentDescription = "Añadir foto de perfil",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clickable { pickImageLauncher.launch("image/*") }
            )
        }
    }
}

@Composable
private fun ProfileImageErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    name: String,
    username: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@$username",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileDetailsCard(description: String, country: String, email: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Biografía",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (description.isEmpty()) "Aún no has escrito tu biografía." else description,
                maxLines = 6,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailItem(
                icon = Lucide.MapPin,
                title = "Location",
                value = if (country.isEmpty()) "Not specified" else country
            )
            ProfileDetailItem(
                icon = Lucide.Mail,
                title = "Contact",
                value = email
            )
        }
    }
}

@Composable
private fun ProfileDetailItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileActionsSection(
    navController: NavController?,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    onNuevaLista: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Acciones principales: grid 1x4 de cuadrados (criterio del propietario:
    // 1x3 + «Nueva lista», que es la acción que da sentido a la sección de arriba)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccionCuadrada(
            label = "Estadísticas",
            icon = Lucide.ChartBarBig,
            modifier = Modifier.weight(1f),
            onClick = { navController?.navigate("stats") }
        )
        AccionCuadrada(
            label = "Seguidos",
            icon = Lucide.Users,
            modifier = Modifier.weight(1f),
            onClick = { navController?.navigate("friendlist") }
        )
        AccionCuadrada(
            label = "Nueva lista",
            icon = Lucide.Plus,
            modifier = Modifier.weight(1f),
            onClick = onNuevaLista
        )
        AccionCuadrada(
            label = "Editar",
            icon = Lucide.Pencil,
            modifier = Modifier.weight(1f),
            onClick = { navController?.navigate("editProfile") }
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Botón para cerrar sesión (al fondo, discreto)
    OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    // El cierre de sesión pasa por el SSOT: cierra Firebase Auth,
                    // limpia credenciales de Google y el estado local.
                    googleViewModel.clearCredentialState()
                    userViewModel.signOut()
                    navController?.navigate("main") {
                        popUpTo(id = navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Lucide.LogOut,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
}

/** Cuadrado de acción del perfil (grid 1x3): superficie por tono, sin sombra. */
@Composable
private fun AccionCuadrada(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(label, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ProfileLoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GVSkeleton(shape = CircleShape, width = 140.dp, height = 140.dp)
        GVSkeleton(width = 160.dp, height = 18.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 120.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 48.dp)
    }
}

