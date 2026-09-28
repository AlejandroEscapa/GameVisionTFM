package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.ui.designsystem.components.EmptyState
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.ui.designsystem.gvSharedElement
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 18/01/2025 (actualizado 28/09/2026 — F0-B/B2)
 * Descripción: pantalla con las listas de juegos.
 *
 * Desde B2 se alimenta de la biblioteca nueva (users/{uid}/library, con instantánea
 * de nombre/portada: sin peticiones por juego) y del historial local del dispositivo.
 * Pestañas: Jugando · Completados · Coleccionados · Deseados · Favoritos · Historial.
 */

/** Elemento de la lista: ficha de biblioteca o reciente local. */
private data class GameListItem(
    val gameId: String,
    val name: String,
    val coverUrl: String?,
    val released: String,
    val genres: List<String>,
    val entry: LibraryEntry?
)

/**
 * Pantalla con las listas de juegos (biblioteca e historial local).
 */
@Composable
fun GameListScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    paddingValues: PaddingValues,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel
) {
    var sortOption by rememberSaveable { mutableStateOf("Alfabético") }
    var selectedList by rememberSaveable { mutableStateOf("playing") }

    // Identidad desde el SSOT de sesión.
    val uid by userViewModel.currentUid.collectAsState()

    // Biblioteca en vivo (SSOT del usuario). null = primera carga.
    val library by remember(uid) { ddbbViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsState(initial = null)

    // Historial local (decisión F0-B: vive en el dispositivo, no en Firestore).
    val recents by ddbbViewModel.recentGames.collectAsState(initial = emptyList())

    // Elementos a mostrar según la pestaña seleccionada.
    val items: List<GameListItem>? = when {
        selectedList == "history" -> recents.map {
            GameListItem(it.gameId.toString(), it.name, it.coverUrl, "", emptyList(), null)
        }
        library == null -> null
        else -> {
            val entries = library?.getOrNull().orEmpty()
            entries
                .filter { entry ->
                    when (selectedList) {
                        "playing" -> entry.status == LibraryStatus.PLAYING
                        "completed" -> entry.status == LibraryStatus.COMPLETED
                        "collected" -> entry.status == LibraryStatus.COLLECTED
                        "wished" -> entry.status == LibraryStatus.WISHED
                        "favorites" -> entry.favorite
                        else -> true
                    }
                }
                .map { GameListItem(it.gameId, it.name, it.coverUrl, it.released, it.genres, it) }
        }
    }

    val sortedItems: List<GameListItem> = remember(items, sortOption) {
        val list = items.orEmpty()
        when (sortOption) {
            "Alfabético" -> list.sortedBy { it.name.lowercase() }
            "Año (Asc.)" -> list.sortedBy { it.released }
            "Año (Desc.)" -> list.sortedByDescending { it.released }
            "Más jugados" -> list.sortedByDescending { it.entry?.minutesTotal ?: 0 }
            else -> list
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                HeaderTitle(selectedList)
                Spacer(modifier = Modifier.width(8.dp))
                SelectListButton(
                    onListSelected = { selected ->
                        selectedList = selected
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                SortMenuButton(currentSortOption = sortOption) { sortOption = it }
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    items == null -> LoadingIndicator()
                    sortedItems.isEmpty() -> GameListEmptyState(selectedList)
                    else -> GameList(
                        gameItems = sortedItems,
                        navController = navController,
                        ddbbViewModel = ddbbViewModel,
                        userViewModel = userViewModel
                    )
                }
            }
        }
    )
}

/**
 * Indicador de carga para la pantalla de juegos (skeleton del design system).
 */
@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) { GameRowSkeleton() }
    }
}

/**
 * Estado vacío de la lista seleccionada (componente del design system).
 */
@Composable
fun GameListEmptyState(selectedList: String = "playing") {
    val (title, hint) = when (selectedList) {
        "playing" -> "No tienes juegos en curso" to "Busca un juego y añádelo como «Jugando»"
        "completed" -> "Aún no has completado ningún juego" to
            "Cuando termines uno, cámbiale el estado a «Completado»"
        "collected" -> "Aún no tienes juegos coleccionados" to
            "Marca «Coleccionado» cuando consigas todos sus logros"
        "wished" -> "Tu lista de deseos está vacía" to "Busca un juego y añádelo como «Deseado»"
        "favorites" -> "Sin favoritos todavía" to "Marca el corazón en los juegos que más te gusten"
        "history" -> "Aún no has visto ningún juego" to "Abre la ficha de un juego y aparecerá aquí"
        else -> "No tienes juegos añadidos aún" to "Busca un juego y añádelo a tu lista"
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = title,
            hint = hint,
            icon = Icons.Default.Clear
        )
    }
}

@Composable
private fun GameList(
    gameItems: List<GameListItem>,
    navController: NavController,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        items(gameItems, key = { it.gameId }) { item ->
            Box(Modifier.animateItem()) {
                GameListCard(
                    item = item,
                    navController = navController,
                    ddbbViewModel = ddbbViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@Composable
private fun GameListCard(
    item: GameListItem,
    navController: NavController,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val uid by userViewModel.currentUid.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(200.dp)
            .clickable { navController.navigate("gameDetails/${item.gameId}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo (shared element: vuela al detalle)
            GameCover(
                imageUrl = item.coverUrl,
                title = item.name,
                contentDescription = "Imagen del juego",
                modifier = Modifier
                    .fillMaxSize()
                    .gvSharedElement(key = "cover-${item.gameId}")
            )
            // Panel inferior con los datos del juego
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (item.released.length >= 4) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Año: ")
                                    }
                                    append(item.released.substring(0, 4))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.genres.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Géneros: ")
                                    }
                                    append(item.genres.joinToString(", "))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item.entry?.let { entry ->
                            Text(
                                text = "Estado: ${entry.status.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Solo las fichas de la biblioteca se pueden eliminar
                    // (el historial local no se toca desde aquí).
                    item.entry?.let { entry ->
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val userId = uid
                                    if (userId.isNullOrBlank()) {
                                        userViewModel.setMessage("Inicia sesión para gestionar tu biblioteca")
                                        return@launch
                                    }
                                    ddbbViewModel.removeFromLibrary(userId, entry).onFailure { error ->
                                        userViewModel.setMessage("No se pudo eliminar: ${error.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .padding(bottom = 10.dp, end = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar juego",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderTitle(selectedList: String) {
    val title = when (selectedList) {
        "playing" -> "Jugando"
        "completed" -> "Completados"
        "collected" -> "Coleccionados"
        "wished" -> "Deseados"
        "favorites" -> "Favoritos"
        "history" -> "Historial"
        else -> "Jugando"
    }
    Text(
        text = title,
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Start
    )
}

@Composable
fun SelectListButton(
    modifier: Modifier = Modifier,
    onListSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val listas = listOf(
        "Jugando" to "playing",
        "Completados" to "completed",
        "Coleccionados" to "collected",
        "Deseados" to "wished",
        "Favoritos" to "favorites",
        "Historial" to "history"
    )

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar lista",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listas.forEach { (displayName, param) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        expanded = false
                        onListSelected(param)
                    }
                )
            }
        }
    }
}

@Composable
fun SortMenuButton(currentSortOption: String, onSortSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = listOf(
        "Alfabético",
        "Año (Asc.)",
        "Año (Desc.)",
        "Más jugados"
    )

    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Ordenar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Ordenar por:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider()
            }
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSortSelected(option)
                    }
                )
            }
        }
    }
}
