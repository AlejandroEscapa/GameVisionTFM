package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.datastore.RecentGame
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GVSkeleton
import es.androidtfm.gamevision.ui.designsystem.components.RatingBadge
import es.androidtfm.gamevision.ui.designsystem.gvSharedElement
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.ui.designsystem.components.OfflineBanner
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/02/2025
 * Descripción: 
 */

/**
 * Pantalla de detalles del juego.
 *
 * @param navController Controlador de navegación.
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param paddingValues PaddingValues para ajustar el layout.
 * @param gameId Identificador del juego a mostrar.
 * @param ddbbViewModel ViewModel para operaciones con la base de datos.
 * @param userViewModel ViewModel para datos de usuario.
 * @param viewModel ViewModel para obtener detalles del juego (SearchViewModel).
 */

@Composable
fun GameDetails(
    navController: NavController,
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    gameId: Int,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel,
    viewModel: SearchViewModel = viewModel(
        viewModelStoreOwner = navController.getBackStackEntry("searchScreen")
    )
) {
    val context = LocalContext.current
    var addMenuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val uid by userViewModel.currentUid.collectAsState()

    LaunchedEffect(gameId, uid) {
        viewModel.fetchGameDetails(gameId)
    }

    val game by viewModel.gameDetails.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val error by viewModel.errorDetails.collectAsState()
    val fromCache by viewModel.fromCache.collectAsState()

    // Ficha en vivo del juego en la biblioteca (para añadir/cambiar estado/favorito).
    val library by remember(uid) { ddbbViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsState(initial = null)
    val entry = library?.getOrNull()?.firstOrNull { it.gameId == gameId.toString() }

    // Historial local de recientes (F0-B: ya no se guarda en Firestore).
    LaunchedEffect(game) {
        game?.let { g -> ddbbViewModel.addRecentGame(RecentGame(g.id, g.name, g.coverUrl)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // F0/T0.5: aviso discreto de modo degradado (ficha servida desde caché)
        if (fromCache && !isLoading) {
            OfflineBanner(
                text = "Sin conexión · mostrando la ficha guardada",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        when {
            isLoading -> FullScreenLoader()
            error != null -> ErrorMessage(error)
            game == null -> EmptyState()
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Detalles del juego",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(24.dp)
                    )

                    GameContent(game = game)


                    // Sección de botones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 16.dp, 16.dp, 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        // Botón Compartir
                        Button(
                            onClick = {
                                game?.let {
                                    val shareText =
                                        "¡Mira este juego! ${it.name} - ${it.coverUrl}"
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            },
                                            "Compartir juego"
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir")
                        }

                        // Botón Añadir con menú desplegable
                        Box {
                            Button(
                                onClick = { addMenuExpanded = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Añadir")
                                Spacer(Modifier.width(8.dp))
                                Text("Añadir")
                            }

                            DropdownMenu(
                                expanded = addMenuExpanded,
                                onDismissRequest = { addMenuExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                listOf(
                                    "Jugando" to "playing",
                                    "Deseado" to "wished",
                                    "Favorito (marcar/desmarcar)" to "favorite"
                                ).forEach { (label, action) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            addMenuExpanded = false
                                            coroutineScope.launch {
                                                val currentGame = game
                                                val userId = uid
                                                if (currentGame == null || userId.isNullOrBlank()) {
                                                    userViewModel.setMessage("Inicia sesión para guardar juegos")
                                                    return@launch
                                                }
                                                when (action) {
                                                    "playing", "wished" -> {
                                                        val target = if (action == "playing")
                                                            LibraryStatus.PLAYING else LibraryStatus.WISHED
                                                        val currentEntry = entry
                                                        if (currentEntry == null) {
                                                            ddbbViewModel.addToLibrary(
                                                                userId,
                                                                currentGame.toLibraryEntry(target)
                                                            ).onSuccess {
                                                                userViewModel.setMessage(
                                                                    if (target == LibraryStatus.PLAYING) "Añadido a Jugando"
                                                                    else "Añadido a Deseados"
                                                                )
                                                            }.onFailure { e ->
                                                                userViewModel.setMessage("No se pudo añadir: ${e.message}")
                                                            }
                                                        } else if (currentEntry.status == target) {
                                                            userViewModel.setMessage("Ya está en esa lista")
                                                        } else {
                                                            ddbbViewModel.updateStatus(
                                                                userId,
                                                                currentEntry.gameId,
                                                                currentEntry.status,
                                                                target
                                                            ).onSuccess {
                                                                userViewModel.setMessage(
                                                                    if (target == LibraryStatus.PLAYING) "Ahora está en Jugando"
                                                                    else "Ahora está en Deseados"
                                                                )
                                                            }.onFailure { e ->
                                                                userViewModel.setMessage("No se pudo actualizar: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                    "favorite" -> {
                                                        val currentEntry = entry
                                                        if (currentEntry == null) {
                                                            // Aún no está en la biblioteca: entra en Deseados con el corazón marcado.
                                                            ddbbViewModel.addToLibrary(
                                                                userId,
                                                                currentGame.toLibraryEntry(LibraryStatus.WISHED)
                                                                    .copy(favorite = true)
                                                            ).onSuccess {
                                                                userViewModel.setMessage("Añadido a Deseados y marcado como favorito")
                                                            }.onFailure { e ->
                                                                userViewModel.setMessage("No se pudo añadir: ${e.message}")
                                                            }
                                                        } else {
                                                            ddbbViewModel.setFavorite(userId, currentEntry.gameId, !currentEntry.favorite)
                                                                .onSuccess {
                                                                    userViewModel.setMessage(
                                                                        if (!currentEntry.favorite) "Marcado como favorito"
                                                                        else "Quitado de favoritos"
                                                                    )
                                                                }.onFailure { e ->
                                                                    userViewModel.setMessage("No se pudo actualizar: ${e.message}")
                                                                }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Ficha mínima con la instantánea del catálogo, lista para añadir a la biblioteca.
 */
private fun CatalogGame.toLibraryEntry(status: LibraryStatus) = LibraryEntry(
    gameId = id.toString(),
    status = status,
    name = name,
    coverUrl = coverUrl,
    released = released,
    genres = genres
)

@Composable
fun GameContent(game: CatalogGame?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 5.dp, 16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            GameCover(
                imageUrl = game?.coverUrl,
                title = game?.name ?: "??",
                contentDescription = "Imagen del juego",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .gvSharedElement(key = "cover-${game?.id ?: 0}")
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 120f,
                            endY = 320f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = game?.name ?: "Nombre no disponible",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                game?.released?.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            game?.rating?.takeIf { it > 0 }?.let {
                RatingBadge(
                    rating = String.format(java.util.Locale.US, "%.1f", it),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                game?.let { g ->
                    MetaDataRow(
                        icon = Icons.Default.Star,
                        label = "Veces recomendado",
                        value = g.suggestionsCount.toString()
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetaDataRow(
                        icon = Icons.Default.Star,
                        label = "Metacritic Score",
                        value = g.metacritic?.toString() ?: "N/A"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetaDataRow(
                        icon = Icons.Default.Star,
                        label = "RAWG Rating",
                        value = g.rating.toString()
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetaDataRow(
                        icon = Icons.Default.DateRange,
                        label = "Lanzamiento",
                        value = g.released,
                        extraPadding = true // Nuevo parámetro para padding adicional
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetaDataRow(
                        icon = Icons.Default.Info,
                        label = "Género",
                        value = g.genres.joinToString()
                    )
                }
            }
        }
    }
}

@Composable
fun MetaDataRow(icon: ImageVector, label: String, value: String, extraPadding: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (extraPadding) 12.dp else 0.dp), // Padding adicional vertical
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp)) // Espacio aumentado entre icono y texto
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp) // Espacio adicional bajo la etiqueta
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Componente que muestra un indicador de carga a pantalla completa.
 */
@Composable
private fun FullScreenLoader() {
    // Skeleton que replica el layout del detalle (hero + tarjeta de metadatos)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 5.dp, 16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GVSkeleton(Modifier.fillMaxWidth(), height = 320.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 14.dp)
        GVSkeleton(Modifier.fillMaxWidth(0.7f), height = 14.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 14.dp)
    }
}

/**
 * Componente que muestra un mensaje de error centrado en pantalla.
 *
 * @param error Mensaje de error a mostrar.
 */
@Composable
private fun ErrorMessage(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = error ?: "Error desconocido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Componente que muestra un estado vacío cuando no hay datos disponibles.
 */
@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sin datos",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No se encontraron detalles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
