package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.ui.designsystem.gvSharedElement
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.ListFilter
import com.composables.icons.lucide.ListPlus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X

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
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel,
    // F2/T2.6: si llega, las tarjetas ofrecen «añadir/quitar de una lista».
    socialViewModel: SocialViewModel? = null
) {
    var sortOption by rememberSaveable { mutableStateOf("Alfabético") }
    var selectedList by rememberSaveable { mutableStateOf("playing") }
    // Filtros y búsqueda dentro de la biblioteca (F1 — Bloque 4, T1.14/T1.15)
    var query by rememberSaveable { mutableStateOf("") }
    var genreFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var platformFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var filtersExpanded by rememberSaveable { mutableStateOf(true) }

    // Identidad desde el SSOT de sesión.
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()

    // Biblioteca en vivo (SSOT del usuario). null = primera carga.
    val library by remember(uid) { libraryViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)

    // Historial local (decisión F0-B: vive en el dispositivo, no en Firestore).
    val recents by libraryViewModel.recentGames.collectAsStateWithLifecycle(initialValue = emptyList())

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
                        "all" -> true
                        "playing" -> entry.status == LibraryStatus.PLAYING
                        "completed" -> entry.status == LibraryStatus.COMPLETED
                        "collected" -> entry.status == LibraryStatus.COLLECTED
                        "paused" -> entry.status == LibraryStatus.PAUSED
                        "retired" -> entry.status == LibraryStatus.RETIRED
                        "abandoned" -> entry.status == LibraryStatus.ABANDONED
                        "wished" -> entry.status == LibraryStatus.WISHED
                        "favorites" -> entry.favorite
                        else -> true
                    }
                }
                .map { GameListItem(it.gameId, it.name, it.coverUrl, it.released, it.genres, it) }
        }
    }

    // Filtros combinados (T1.14) + búsqueda en la biblioteca (T1.15).
    val criteria = es.androidtfm.gamevision.data.library.LibraryFilters.Criteria(
        query = query,
        genre = genreFilter,
        platform = platformFilter
    )
    val filteredItems: List<GameListItem>? = items?.filter { item ->
        es.androidtfm.gamevision.data.library.LibraryFilters.matches(
            name = item.name,
            genres = item.genres,
            platform = item.entry?.lastPlatform,
            criteria = criteria
        )
    }

    // Opciones de filtro disponibles: solo lo que el usuario tiene (sin ruido).
    val availableGenres: List<String> = remember(library) {
        library?.getOrNull().orEmpty().flatMap { it.genres }.map { it.trim() }
            .filter { it.isNotBlank() }.distinct().sorted()
    }
    val availablePlatforms: List<String> = remember(library) {
        library?.getOrNull().orEmpty().mapNotNull { it.lastPlatform?.takeIf { p -> p.isNotBlank() } }
            .distinct().sorted()
    }
    val hasActiveFilters = criteria.isActive

    val sortedItems: List<GameListItem> = remember(filteredItems, sortOption) {
        val list = filteredItems.orEmpty()
        when (sortOption) {
            "Alfabético" -> list.sortedBy { it.name.lowercase() }
            "Año (Asc.)" -> list.sortedBy { it.released }
            "Año (Desc.)" -> list.sortedByDescending { it.released }
            "Más jugados" -> list.sortedByDescending { it.entry?.minutesTotal ?: 0 }
            "Mejor nota" -> list.sortedByDescending { it.entry?.rating ?: 0.0 }
            else -> list
        }
    }

    Scaffold(
        topBar = {
            GVScreenHeader(
                title = headerTitle(selectedList),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SelectListButton(
                    onListSelected = { selected ->
                        selectedList = selected
                    }
                )
                IconButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Icon(
                        imageVector = Lucide.ListFilter,
                        contentDescription = "Filtros",
                        tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                SortMenuButton(currentSortOption = sortOption) { sortOption = it }
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Búsqueda en la biblioteca (T1.15)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar en tu biblioteca…") },
                    leadingIcon = { Icon(Lucide.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Lucide.X, contentDescription = "Borrar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                // Panel de filtros (T1.14): género y plataforma
                if (filtersExpanded) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        if (availableGenres.isNotEmpty()) {
                            Text(
                                "Género",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableGenres.forEach { g ->
                                    GVChip(
                                        text = g,
                                        selected = genreFilter == g,
                                        onClick = { genreFilter = if (genreFilter == g) null else g }
                                    )
                                }
                            }
                        }
                        if (availablePlatforms.isNotEmpty()) {
                            Text(
                                "Plataforma",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availablePlatforms.forEach { p ->
                                    GVChip(
                                        text = p,
                                        selected = platformFilter == p,
                                        onClick = { platformFilter = if (platformFilter == p) null else p }
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        items == null -> LoadingIndicator()
                        sortedItems.isEmpty() && hasActiveFilters -> EmptyState(
                            title = "Sin resultados",
                            hint = "Prueba a cambiar la búsqueda o quitar los filtros",
                            icon = Lucide.Search,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                        sortedItems.isEmpty() -> GameListEmptyState(selectedList)
                        else -> GameList(
                            gameItems = sortedItems,
                            navController = navController,
                            libraryViewModel = libraryViewModel,
                            userViewModel = userViewModel,
                            socialViewModel = socialViewModel
                        )
                    }
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
        "all" -> "Tu biblioteca" to "Busca un juego y añádelo cuando quieras"
        "playing" -> "No tienes juegos en curso" to "Busca un juego y añádelo como «Jugando»"
        "completed" -> "Aún no has completado ningún juego" to
            "Cuando termines uno, cámbiale el estado a «Completado»"
        "collected" -> "Aún no tienes juegos coleccionados" to
            "Marca «Coleccionado» cuando consigas todos sus logros"
        "paused" -> "Nada en pausa ahora mismo" to "Pausa un juego que tengas en curso"
        "retired" -> "Sin juegos retirados" to "Retira un juego del que te hayas cansado"
        "abandoned" -> "Sin juegos abandonados" to "Aquí quedan los que dejaste a medias"
        "wished" -> "Tu lista de deseos está vacía" to "Busca un juego y añádelo como «Deseado»"
        "favorites" -> "Sin favoritos todavía" to "Marca el corazón en los juegos que más te gusten"
        "history" -> "Aún no has visto ningún juego" to "Abre la ficha de un juego y aparecerá aquí"
        else -> "No tienes juegos añadidos aún" to "Busca un juego y añádelo a tu lista"
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = title,
            hint = hint,
            icon = Lucide.X
        )
    }
}

@Composable
private fun GameList(
    gameItems: List<GameListItem>,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel? = null
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
                    libraryViewModel = libraryViewModel,
                    userViewModel = userViewModel,
                    socialViewModel = socialViewModel
                )
            }
        }
    }
}

@Composable
private fun GameListCard(
    item: GameListItem,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()

    // F2/T2.6: listas curadas del usuario (para el diálogo de añadir/quitar).
    val lists by socialViewModel?.lists?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<Result<List<es.androidtfm.gamevision.data.model.GameList>>?>(null) }
    var showListDialog by remember { mutableStateOf(false) }

    if (showListDialog && socialViewModel != null) {
        AddToListDialog(
            gameName = item.name,
            gameId = item.gameId,
            lists = lists?.getOrDefault(emptyList()).orEmpty(),
            onLoadLists = { socialViewModel.loadLists(uid.orEmpty()) },
            onToggle = { list, checked ->
                val current = uid.orEmpty()
                if (current.isBlank()) return@AddToListDialog
                val next = if (checked) list.gameIds + item.gameId else list.gameIds - item.gameId
                socialViewModel.saveList(current, list.copy(gameIds = next)) { result ->
                    if (result.isSuccess) socialViewModel.loadLists(current)
                }
            },
            onDismiss = { showListDialog = false }
        )
    }

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
                            entry.rating?.let { nota ->
                                Text(
                                    text = "Tu nota: " + formatRating(nota),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Acciones de tarjeta: añadir a lista JUNTO a eliminar,
                    // misma fila, mismo tamaño, sin paddings internos que las separen.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // F2/T2.6: añadir/quitar de listas curadas (solo con sesión).
                        if (socialViewModel != null && item.entry != null) {
                            IconButton(
                                onClick = { showListDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.ListPlus,
                                    contentDescription = "Añadir a una lista",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
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
                                        libraryViewModel.removeFromLibrary(userId, entry).onFailure { error ->
                                            userViewModel.setMessage("No se pudo eliminar: ${error.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.Trash2,
                                    contentDescription = "Eliminar juego",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun headerTitle(selectedList: String): String = when (selectedList) {
        "all" -> "Biblioteca"
        "playing" -> "Jugando"
        "completed" -> "Completados"
        "collected" -> "Coleccionados"
        "paused" -> "En pausa"
        "retired" -> "Retirados"
        "abandoned" -> "Abandonados"
        "wished" -> "Deseados"
        "favorites" -> "Favoritos"
        "history" -> "Historial"
        else -> "Jugando"
    }

@Composable
fun SelectListButton(
    modifier: Modifier = Modifier,
    onListSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val listas = listOf(
        "Todos" to "all",
        "Jugando" to "playing",
        "Completados" to "completed",
        "Coleccionados" to "collected",
        "En pausa" to "paused",
        "Retirados" to "retired",
        "Abandonados" to "abandoned",
        "Deseados" to "wished",
        "Favoritos" to "favorites",
        "Historial" to "history"
    )

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Lucide.ChevronDown,
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
        "Más jugados",
        "Mejor nota"
    )

    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Lucide.EllipsisVertical,
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

/**
 * F2/T2.6 — Diálogo para añadir/quitar un juego de las listas curadas del
 * usuario. Guardar una lista con un id menos dispara el hito list_published
 * (deteterminista) desde saveList, que vuelve a verificar isPublic.
 */
@Composable
private fun AddToListDialog(
    gameName: String,
    gameId: String,
    lists: List<es.androidtfm.gamevision.data.model.GameList>,
    onLoadLists: () -> Unit,
    onToggle: (es.androidtfm.gamevision.data.model.GameList, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) { onLoadLists() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir «$gameName» a una lista") },
        text = {
            Column {
                if (lists.isEmpty()) {
                    Text(
                        "Todavía no tienes listas. Créalas desde Editar perfil.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                lists.forEach { list ->
                    val checked = list.gameIds.contains(gameId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggle(list, it) }
                        )
                        Text(list.name.ifBlank { "Lista" }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Listo") } }
    )
}
