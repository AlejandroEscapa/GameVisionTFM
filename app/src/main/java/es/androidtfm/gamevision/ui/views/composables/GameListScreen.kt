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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.EmptyState
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GameCarouselSkeleton
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GVSkeleton
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.composables.icons.lucide.Lucide
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
 * Grupos apilados (DX.9): Jugando · En pausa · Deseados · Completados · Coleccionados ·
 * Favoritos · Retirados · Abandonados · Historial; con búsqueda/filtros se aplana.
 */

/** Elemento de la lista: ficha de biblioteca o reciente local. */
internal data class GameListItem(
    val gameId: String,
    val name: String,
    val coverUrl: String?,
    val released: String,
    val genres: List<String>,
    val entry: LibraryEntry?
)

/** Grupo de la biblioteca apilada (DX.9): clave, título y sus juegos. */
internal data class GrupoBiblioteca(
    val clave: String,
    val titulo: String,
    val items: List<GameListItem>
)

/** Máximo visible por grupo antes de «Mostrar más» (DX.9). */
internal const val MAX_POR_GRUPO = 5

/**
 * Agrupa por estado en orden fijo (Jugando → … → Historial), sin los vacíos.
 * Pura para poder testearse en JVM. Los favoritos duplican a propósito: es un
 * conjunto curado, no un estado (igual que el Top 4).
 */
internal fun agruparBiblioteca(items: List<GameListItem>): List<GrupoBiblioteca> {
    if (items.isEmpty()) return emptyList()
    fun grupo(clave: String, pred: (GameListItem) -> Boolean): GrupoBiblioteca? {
        val propios = items.filter(pred)
        return if (propios.isEmpty()) null else GrupoBiblioteca(clave, headerTitle(clave), propios)
    }
    return listOfNotNull(
        grupo("playing") { it.entry?.status == LibraryStatus.PLAYING },
        grupo("paused") { it.entry?.status == LibraryStatus.PAUSED },
        grupo("wished") { it.entry?.status == LibraryStatus.WISHED },
        grupo("completed") { it.entry?.status == LibraryStatus.COMPLETED },
        grupo("collected") { it.entry?.status == LibraryStatus.COLLECTED },
        grupo("favorites") { it.entry?.favorite == true },
        grupo("retired") { it.entry?.status == LibraryStatus.RETIRED },
        grupo("abandoned") { it.entry?.status == LibraryStatus.ABANDONED },
        grupo("history") { it.entry == null }
    )
}

/** Ordena con el criterio del menú (misma regla para grupos y lista plana). */
internal fun ordenarBiblioteca(items: List<GameListItem>, sortOption: String): List<GameListItem> =
    when (sortOption) {
        "Alfabético" -> items.sortedBy { it.name.lowercase() }
        "Año (Asc.)" -> items.sortedBy { it.released }
        "Año (Desc.)" -> items.sortedByDescending { it.released }
        "Más jugados" -> items.sortedByDescending { it.entry?.minutesTotal ?: 0 }
        "Mejor nota" -> items.sortedByDescending { it.entry?.rating ?: 0.0 }
        else -> items
    }

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
    // Grupos desplegados con «Mostrar más» (DX.9): sobrevive a la rotación.
    var expandidos by rememberSaveable { mutableStateOf(setOf<String>()) }
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

    // Elementos: biblioteca + historial local. Los recientes que ya están en la
    // biblioteca no se duplican en el grupo Historial.
    val items: List<GameListItem>? = when {
        library == null -> null
        else -> {
            val entries = library?.getOrNull().orEmpty()
            val enBiblio = entries.map { it.gameId }.toSet()
            entries.map {
                GameListItem(it.gameId, it.name, it.coverUrl, it.released, it.genres, it)
            } + recents
                .filter { it.gameId.toString() !in enBiblio }
                .map {
                    GameListItem(it.gameId.toString(), it.name, it.coverUrl, "", emptyList(), null)
                }
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
    // Con búsqueda o filtros se aplana (DX.9): buscar entre cabeceras confunde.
    val buscando = query.isNotBlank() || hasActiveFilters

    val ordenados: List<GameListItem> = remember(filteredItems, sortOption) {
        ordenarBiblioteca(filteredItems.orEmpty(), sortOption)
    }
    // Grupos apilados (DX.9): ordenados una vez, cada grupo conserva el orden.
    val grupos: List<GrupoBiblioteca> = remember(ordenados, buscando) {
        if (buscando) emptyList() else agruparBiblioteca(ordenados)
    }

    Scaffold(
        topBar = {
            GVScreenHeader(
                title = "Biblioteca",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
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
                        .padding(horizontal = GVSpacing.screenPadding)
                )

                // Panel de filtros (T1.14): género y plataforma
                if (filtersExpanded) {
                    Column(modifier = Modifier.padding(horizontal = GVSpacing.screenPadding, vertical = 4.dp)) {
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
                        items == null -> Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Carga con la forma de lo que viene: título + carrusel.
                            GVSkeleton(
                                Modifier.padding(horizontal = 16.dp).fillMaxWidth(0.4f),
                                height = 22.dp
                            )
                            GameCarouselSkeleton(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                            )
                        }
                        buscando && ordenados.isEmpty() -> EmptyState(
                            title = "Sin resultados",
                            hint = "Prueba a cambiar la búsqueda o quitar los filtros",
                            icon = Lucide.Search,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                        buscando -> ListaPlana(
                            gameItems = ordenados,
                            navController = navController,
                            libraryViewModel = libraryViewModel,
                            userViewModel = userViewModel,
                            socialViewModel = socialViewModel
                        )
                        grupos.isEmpty() -> GameListEmptyState("all")
                        else -> GruposBiblioteca(
                            grupos = grupos,
                            expandidos = expandidos,
                            onExpandir = { clave ->
                                expandidos = if (clave in expandidos) expandidos - clave
                                else expandidos + clave
                            },
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

/**
 * Biblioteca apilada (DX.9): un scroll gigante con un carrusel por grupo.
 * Cada grupo muestra 5 portadas; «Mostrar más» lo despliega como cuadrícula.
 */
@Composable
private fun GruposBiblioteca(
    grupos: List<GrupoBiblioteca>,
    expandidos: Set<String>,
    onExpandir: (String) -> Unit,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        grupos.forEach { grupo ->
            item(key = "cab-${grupo.clave}") {
                GrupoCabecera(titulo = "${grupo.titulo} (${grupo.items.size})")
            }
            val expandido = grupo.clave in expandidos
            if (!expandido) {
                item(key = "car-${grupo.clave}") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(grupo.items.take(MAX_POR_GRUPO), key = { it.gameId }) { item ->
                            FichaPortada(
                                item = item,
                                navController = navController,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            } else {
                grupo.items.chunked(3).forEachIndexed { fila, trozo ->
                    item(key = "grid-${grupo.clave}-$fila") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            trozo.forEach { item ->
                                FichaPortada(
                                    item = item,
                                    navController = navController,
                                    compacta = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - trozo.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            if (grupo.items.size > MAX_POR_GRUPO) {
                item(key = "mas-${grupo.clave}") {
                    TextButton(
                        onClick = { onExpandir(grupo.clave) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            if (expandido) "Mostrar menos"
                            else "Mostrar más (${grupo.items.size - MAX_POR_GRUPO})"
                        )
                    }
                }
            }
        }
    }
}

/** Cabecera de grupo con contador (TalkBack la anuncia como encabezado). */
@Composable
private fun GrupoCabecera(titulo: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { heading() }
        )
    }
}

/**
 * Portada compacta de grupo: carátula + título (2 líneas) + dato.
 * En cuadrícula va sin dato para densificar (el título ya identifica).
 */
@Composable
private fun FichaPortada(
    item: GameListItem,
    navController: NavController,
    modifier: Modifier = Modifier,
    compacta: Boolean = false
) {
    Column(modifier = modifier.clickable { navController.navigate("gameDetails/${item.gameId}") }) {
        GameCover(
            imageUrl = item.coverUrl,
            title = item.name,
            modifier = if (compacta) {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            } else {
                Modifier.size(width = 120.dp, height = 160.dp)
            }
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (!compacta) {
            MetaDato(
                item = item,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Dato bajo el título: horas si hay, si no el año (a salvo de vacíos). */
@Composable
private fun MetaDato(
    item: GameListItem,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    val horas = item.entry?.minutesTotal ?: 0
    val texto = if (horas > 0) "Llevas ${horas / 60} h"
    else anioDe(item.released).orEmpty()
    if (texto.isNotBlank()) {
        Text(
            text = texto,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}

@Composable
private fun ListaPlana(
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
                FilaCompacta(
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

/**
 * Fila compacta de la lista plana (búsqueda/filtros): misma geometría que
 * `GameRowSkeleton` (portada 64 + dos líneas), con las acciones de la tarjeta
 * a tamaño táctil completo (48 dp, DX-T18).
 */
@Composable
private fun FilaCompacta(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("gameDetails/${item.gameId}") }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameCover(
            imageUrl = item.coverUrl,
            title = item.name,
            contentDescription = "Imagen del juego",
            modifier = Modifier.size(64.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            MetaDato(
                item = item,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        // F2/T2.6: añadir/quitar de listas curadas (solo con sesión).
        if (socialViewModel != null && item.entry != null) {
            IconButton(
                onClick = { showListDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Lucide.ListPlus,
                    contentDescription = "Añadir a una lista",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Lucide.Trash2,
                    contentDescription = "Eliminar juego",
                    tint = MaterialTheme.colorScheme.error
                )
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
