package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import es.androidtfm.gamevision.viewmodel.SocialViewModel
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
    socialViewModel: SocialViewModel,
    viewModel: SearchViewModel = viewModel(
        viewModelStoreOwner = navController.getBackStackEntry("searchScreen")
    )
) {
    val context = LocalContext.current
    var showLibraryPanel by remember { mutableStateOf(false) }
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

    // Duración estimada (F1/T1.11): se pide con caché cuando se conoce el nombre.
    var playtimes by remember(gameId) {
        mutableStateOf<es.androidtfm.gamevision.data.hltb.HltbPlaytimes?>(null)
    }
    LaunchedEffect(game?.name) {
        val nombre = game?.name ?: return@LaunchedEffect
        playtimes = ddbbViewModel.playtimesFor(nombre)
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
            error != null -> ErrorMessage(error, onRetry = { viewModel.fetchGameDetails(gameId) })
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

                    // Duración estimada (T1.11): el valor manual del usuario gana;
                    // si no hay dato ni manual, la tarjeta no se muestra.
                    DurationCard(entry = entry, playtimes = playtimes)


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

                        // Botón que abre la gestión de biblioteca (estado, nota, reseña)
                        Button(
                            onClick = { showLibraryPanel = !showLibraryPanel },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (entry != null) Icons.Default.Star else Icons.Default.Add,
                                contentDescription = "Biblioteca"
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (entry != null) entry!!.status.label else "Añadir")
                        }
                    }

                    // Panel de gestión de la biblioteca (F1 — Bloque 1): los 7 estados,
                    // nota con medias estrellas, reseña y favorito.
                    if (showLibraryPanel) {
                        LibraryPanel(
                            entry = entry,
                            onAdd = { status ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentGame = game
                                    if (userId.isNullOrBlank() || currentGame == null) {
                                        userViewModel.setMessage("Inicia sesión para guardar juegos")
                                        return@launch
                                    }
                                    ddbbViewModel.addToLibrary(userId, currentGame.toLibraryEntry(status))
                                        .onSuccess { userViewModel.setMessage("Añadido a ${status.label}") }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo añadir: ${e.message}") }
                                }
                            },
                            onChangeStatus = { to ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.updateStatus(userId, currentEntry.gameId, currentEntry.status, to)
                                        .onSuccess {
                                            userViewModel.setMessage("Estado: ${to.label}")
                                            // F2/D2.3: hito de completado en el feed (determinista).
                                            if (to == LibraryStatus.COMPLETED && currentEntry.status != LibraryStatus.COMPLETED) {
                                                socialViewModel.publishCompletedMilestone(
                                                    uid = userId,
                                                    gameId = currentEntry.gameId,
                                                    gameName = currentEntry.name.ifBlank { game?.name.orEmpty() },
                                                    gameCover = currentEntry.coverUrl.orEmpty()
                                                )
                                            }
                                        }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo cambiar: ${e.message}") }
                                }
                            },
                            onRate = { rating ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.setRating(userId, currentEntry.gameId, currentEntry.rating, rating)
                                        .onSuccess { userViewModel.setMessage(if (rating == null) "Nota quitada" else "Nota guardada") }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo guardar la nota: ${e.message}") }
                                }
                            },
                            onReview = { text ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.setReview(userId, currentEntry.gameId, text.ifBlank { null })
                                        .onSuccess { userViewModel.setMessage("Reseña guardada") }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo guardar la reseña: ${e.message}") }
                                }
                            },
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.setFavorite(userId, currentEntry.gameId, !currentEntry.favorite)
                                        .onSuccess { userViewModel.setMessage(if (!currentEntry.favorite) "Marcado como favorito" else "Quitado de favoritos") }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo actualizar: ${e.message}") }
                                }
                            },
                            onRemove = {
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.removeFromLibrary(userId, currentEntry)
                                        .onSuccess { userViewModel.setMessage("Quitado de tu biblioteca") }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo quitar: ${e.message}") }
                                }
                            },
                            onManualPlaytime = { minutos ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.setManualPlaytime(userId, currentEntry.gameId, minutos)
                                        .onSuccess {
                                            userViewModel.setMessage(
                                                if (minutos == null) "Duración manual quitada"
                                                else "Duración manual guardada"
                                            )
                                        }
                                        .onFailure { e -> userViewModel.setMessage("No se pudo guardar: ${e.message}") }
                                }
                            },
                            onNewRun = { platform ->
                                coroutineScope.launch {
                                    val userId = uid
                                    val currentEntry = entry ?: return@launch
                                    if (userId.isNullOrBlank()) return@launch
                                    ddbbViewModel.createLog(
                                        userId,
                                        es.androidtfm.gamevision.data.library.GameLog(
                                            gameId = currentEntry.gameId,
                                            runIndex = 2,
                                            platform = platform,
                                            minutes = null
                                        )
                                    ).onSuccess {
                                        userViewModel.setMessage(if (platform != null) "Rejugada iniciada en $platform" else "Rejugada iniciada")
                                    }.onFailure { e ->
                                        userViewModel.setMessage("No se pudo iniciar la rejugada: ${e.message}")
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
private fun ErrorMessage(error: String?, onRetry: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = error ?: "No se pudo cargar la información",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
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

/**
 * Panel de gestión de la biblioteca (F1 — Bloque 1 + rejugada del Bloque 2).
 *
 * Reúne, en un mismo sitio, lo que define la "biblioteca rica":
 *  - los 7 estados (T1.1/T1.2),
 *  - la nota con medias estrellas (T1.3),
 *  - la reseña escrita (T1.4),
 *  - el favorito y quitar de la biblioteca (T1.7),
 *  - la rejugada con plataforma (T1.5/T1.6).
 *
 * Si el juego aún no está en la biblioteca, solo ofrece añadirlo eligiendo estado.
 */
@Composable
private fun LibraryPanel(
    entry: LibraryEntry?,
    onAdd: (LibraryStatus) -> Unit,
    onChangeStatus: (LibraryStatus) -> Unit,
    onRate: (Double?) -> Unit,
    onReview: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    onNewRun: (platform: String?) -> Unit,
    onManualPlaytime: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (entry == null) {
                Text(
                    text = "Añadir a tu biblioteca",
                    style = MaterialTheme.typography.titleMedium
                )
                LibraryStatusSelector(current = null, onSelect = onAdd)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tu biblioteca",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (entry.favorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = if (entry.favorite) "Quitar de favoritos"
                            else "Marcar como favorito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Quitar de la biblioteca",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                LibraryStatusHint(entry.status)
                LibraryStatusSelector(current = entry.status, onSelect = onChangeStatus)

                HorizontalDivider()

                Text(text = "Tu nota", style = MaterialTheme.typography.titleSmall)
                RatingStars(rating = entry.rating, onRatingChange = onRate)

                HorizontalDivider()

                var reviewText by remember(entry.gameId) { mutableStateOf(entry.review.orEmpty()) }
                ReviewEditor(value = reviewText, onValueChange = { reviewText = it })
                Button(
                    onClick = { onReview(reviewText) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar reseña")
                }

                HorizontalDivider()

                // Duración manual (F1/T1.11): gana sobre el dato de HowLongToBeat.
                Text("Duración manual (horas)", style = MaterialTheme.typography.titleSmall)
                var manualText by remember(entry.gameId) {
                    mutableStateOf(
                        entry.playtimeManual?.let { m ->
                            val h = m / 60.0
                            if (h % 1.0 == 0.0) h.toInt().toString() else h.toString()
                        }.orEmpty()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { txt -> manualText = txt.filter { it.isDigit() || it == '.' }.take(6) },
                        label = { Text("Horas") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val horas = manualText.toDoubleOrNull()
                            onManualPlaytime(horas?.let { (it * 60).toInt() })
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Guardar") }
                }

                HorizontalDivider()

                // Rejugada con plataforma (F1/T1.5 y T1.6)
                Text("Nueva partida (rejugada)", style = MaterialTheme.typography.titleSmall)
                var runPlatform by remember(entry.gameId) { mutableStateOf<String?>(entry.lastPlatform) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    es.androidtfm.gamevision.data.library.Platforms.ALL.take(8).forEach { p ->
                        es.androidtfm.gamevision.ui.designsystem.components.GVChip(
                            text = p,
                            selected = runPlatform == p,
                            onClick = { runPlatform = if (runPlatform == p) null else p }
                        )
                    }
                }
                Button(
                    onClick = { onNewRun(runPlatform) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Empezar rejugada")
                }
            }
        }
    }
}


/**
 * Tarjeta de duración estimada (F1/T1.11).
 *
 * Prioridad: valor MANUAL del usuario > dato de HowLongToBeat. Si no hay
 * ninguno de los dos, la tarjeta NO se muestra (sin dato, se oculta).
 */
@Composable
private fun DurationCard(
    entry: LibraryEntry?,
    playtimes: es.androidtfm.gamevision.data.hltb.HltbPlaytimes?,
    modifier: Modifier = Modifier
) {
    val manual = entry?.playtimeManual
    val hasHltb = playtimes?.hasData == true
    if (manual == null && !hasHltb) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Duración estimada", style = MaterialTheme.typography.titleMedium)

            if (manual != null) {
                Text(
                    text = "Tu estimación: " + es.androidtfm.gamevision.data.hltb.HltbUtils.format(manual),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                playtimes?.mainMinutes?.let { DurationRow("Historia principal", it) }
                playtimes?.plusMinutes?.let { DurationRow("Historia + extras", it) }
                playtimes?.completeMinutes?.let { DurationRow("Completista", it) }
                Text(
                    text = "Fuente: HowLongToBeat (orientativo)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DurationRow(label: String, minutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = es.androidtfm.gamevision.data.hltb.HltbUtils.format(minutes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
