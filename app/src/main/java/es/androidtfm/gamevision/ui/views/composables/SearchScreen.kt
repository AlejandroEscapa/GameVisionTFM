package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GameGridSkeleton
import es.androidtfm.gamevision.ui.designsystem.components.GVSearchField
import es.androidtfm.gamevision.ui.designsystem.gvSharedElement
import es.androidtfm.gamevision.ui.designsystem.components.OfflineBanner
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.X

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

/**
 * Pantalla principal de búsqueda de juegos.
 *
 * @param navController Controlador de navegación.
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param viewModel ViewModel para manejar la lógica de búsqueda.
 * @param paddingValues Valores de padding para la pantalla.
 */

@Composable
fun SearchScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    viewModel: SearchViewModel,
    paddingValues: PaddingValues,
    /** Géneros favoritos del perfil (onboarding): siembran la fila «Para ti». */
    generosFavoritos: List<String> = emptyList()
) {
    // Estados locales para controlar la búsqueda, criterios de ordenación y visibilidad del menú.
    var searchQuery by remember { mutableStateOf("") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var sortCriteria by rememberSaveable { mutableStateOf("rating") }
    var sortAscending by rememberSaveable { mutableStateOf(false) } // Orden descendente por defecto

    // Se recogen los estados de la lista de juegos, indicador de carga y errores desde el ViewModel.
    val games by viewModel.games.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val fromCache by viewModel.fromCache.collectAsStateWithLifecycle()
    // En modo búsqueda lo decide el ViewModel: con búsqueda en vivo el término
    // llega en cada carácter y la pantalla no puede adivinarlo.
    val hasSearched by viewModel.hasSearched.collectAsStateWithLifecycle()
    // Bloque B: filas de descubrimiento cuando no hay término escrito
    val populares by viewModel.populares.collectAsStateWithLifecycle()
    val paraTi by viewModel.paraTi.collectAsStateWithLifecycle()
    val descubre by viewModel.descubre.collectAsStateWithLifecycle()
    val cargandoDescubrimiento by viewModel.cargandoDescubrimiento.collectAsStateWithLifecycle()
    // La fila «Para ti» se siembra con los géneros del perfil (onboarding).
    LaunchedEffect(generosFavoritos) {
        viewModel.cargarDescubrimiento(generosFavoritos)
    }

    // BÚSQUEDA EN VIVO (iteración 02/10, segunda vuelta): se busca al escribir,
    // con debounce en el ViewModel. La lupa sigue ahí para cerrar el teclado y
    // forzar la búsqueda ya, no para que ocurra algo.
    LaunchedEffect(searchQuery) {
        viewModel.buscarEnVivo(searchQuery)
    }
    // Al salir de la pantalla se limpia: volver a Buscar debe enseñar el
    // descubrimiento, no los resultados de la búsqueda anterior.
    DisposableEffect(Unit) {
        onDispose { viewModel.limpiarBusquedaEnVivo() }
    }

    // Cálculo de la lista ordenada según el criterio y orden especificado.
    val sortedGames = when (sortCriteria) {
        "rating" -> if (sortAscending) games.sortedBy { it.rating } else games.sortedByDescending { it.rating }
        "name" -> if (sortAscending) games.sortedBy { it.name } else games.sortedByDescending { it.name }
        "release" -> if (sortAscending) games.sortedBy { it.released } else games.sortedByDescending { it.released }
        else -> games
    }

    // Contenedor principal de la pantalla
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Cabecera con el título y el ORDEN como acción (iteración 02/10,
                // segunda vuelta). Antes la pantalla no tenía cabecera —empezaba
                // con un campo de texto— y el orden vivía en un FloatingActionButton
                // con sombra de 8 dp y `primaryContainer`, el patrón Material 2 que
                // el re-anclaje retiró, flotando sobre un dock ya presente.
                GVScreenHeader(
                    title = "Buscar",
                    modifier = Modifier.padding(horizontal = GVSpacing.screenPadding),
                    actions = {
                        if (games.isNotEmpty()) {
                            Box {
                                IconButton(onClick = { sortMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Lucide.ArrowUpDown,
                                        contentDescription = "Ordenar resultados",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    OpcionOrden(
                                        texto = "Mejor nota",
                                        seleccionada = sortCriteria == "rating" && !sortAscending
                                    ) {
                                        sortCriteria = "rating"; sortAscending = false
                                        sortMenuExpanded = false
                                    }
                                    OpcionOrden(
                                        texto = "Peor nota",
                                        seleccionada = sortCriteria == "rating" && sortAscending
                                    ) {
                                        sortCriteria = "rating"; sortAscending = true
                                        sortMenuExpanded = false
                                    }
                                    OpcionOrden(
                                        texto = "Nombre (A-Z)",
                                        seleccionada = sortCriteria == "name"
                                    ) {
                                        sortCriteria = "name"; sortAscending = true
                                        sortMenuExpanded = false
                                    }
                                    OpcionOrden(
                                        texto = "Más recientes",
                                        seleccionada = sortCriteria == "release" && !sortAscending
                                    ) {
                                        sortCriteria = "release"; sortAscending = false
                                        sortMenuExpanded = false
                                    }
                                    OpcionOrden(
                                        texto = "Más antiguos",
                                        seleccionada = sortCriteria == "release" && sortAscending
                                    ) {
                                        sortCriteria = "release"; sortAscending = true
                                        sortMenuExpanded = false
                                    }
                                }
                            }
                        }
                    }
                )

                // Barra de búsqueda para introducir el término de búsqueda
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        // La lupa no dispara la búsqueda (ya ocurre al escribir):
                        // cierra el teclado y la fuerza sin esperar al debounce.
                        viewModel.fetchGames(searchQuery)
                    },
                    buscando = isLoading
                )

                // F0/T0.5: aviso discreto de modo degradado (datos servidos desde caché)
                if (fromCache && !isLoading) {
                    OfflineBanner(
                        text = "Sin conexión · mostrando resultados guardados",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Sin término escrito: la pantalla se llena de descubrimiento, no
                // de un mensaje vacío (bloque B). Si aún no hay datos, skeletons.
                if (!hasSearched && games.isEmpty()) {
                    if (cargandoDescubrimiento) {
                        GameGridSkeleton(modifier = Modifier.padding(top = 16.dp))
                    } else {
                        FilaDescubrimiento(
                            titulo = "Populares ahora",
                            juegos = populares,
                            navController = navController
                        )
                        FilaDescubrimiento(
                            titulo = if (generosFavoritos.isEmpty()) {
                                "Novedades del catálogo"
                            } else {
                                "Porque te gusta " + generosFavoritos.take(2).joinToString(" y ")
                            },
                            juegos = paraTi,
                            navController = navController
                        )
                        FilaDescubrimiento(
                            titulo = "Descubre · sorpresa del día",
                            juegos = descubre,
                            navController = navController
                        )
                    }
                }

                // Indicador de carga mientras se obtienen los datos
                if (isLoading) {
                    GameGridSkeleton(modifier = Modifier.padding(top = 16.dp))
                }

                // Muestra un mensaje de error en caso de producirse alguno
                error?.let {
                    // El mensaje ya viene traducido por el ViewModel
                    // ("Sin conexión. Comprueba tu red…"): el prefijo "Error:" era
                    // jerga de motor en pantalla.
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = GVSpacing.screenPadding)
                    )
                }

                // Lista de juegos obtenidos de la búsqueda
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    if (hasSearched && games.isEmpty() && !isLoading) {
                        // Mensaje cuando no se encuentran resultados
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No se encontraron resultados")
                            }
                        }
                    } else {
                        // Se muestran los juegos ordenados según los criterios seleccionados
                        items(sortedGames) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

/** Opción del menú de orden, con el check del sistema en vez de un "✓" en texto. */
@Composable
private fun OpcionOrden(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(texto) },
        onClick = onClick,
        trailingIcon = {
            if (seleccionada) {
                Icon(
                    imageVector = Lucide.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

/**
 * Componente que muestra la tarjeta de un juego.
 *
 * @param game Juego a mostrar.
 * @param navController Controlador de navegación.
 */
@Composable
fun GameCard(
    game: CatalogGame,
    navController: NavController
) {
    // FILA, no tarjeta full-bleed (iteración 02/10, segunda vuelta). Antes: 200 dp
    // de portada recortada con un panel translúcido al 90% encima — el patrón
    // "scrim + texto sobre imagen" de 2018. En una lista de resultados lo que
    // hace falta es ESCANEAR: portada pequeña, nombre y datos, todos legibles sin
    // depender de la foto que haya debajo.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GVShapes.medium)
            .clickable { navController.navigate("gameDetails/${game.id}") }
            .padding(vertical = GVSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameCover(
            imageUrl = game.coverUrl,
            title = game.name,
            shape = GVShapes.medium,
            modifier = Modifier
                .size(width = 64.dp, height = 86.dp)
                .gvSharedElement(key = "cover-${game.id}")
        )

        Spacer(Modifier.width(GVSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(GVSpacing.xs))
            // Año y géneros en una línea: sin etiquetas "Año:"/"Géneros:", que era
            // jerga de formulario dentro de una tarjeta.
            val anio = anioDe(game.released)
            val meta = listOfNotNull(
                anio,
                game.genres.take(2).joinToString(", ").takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // La nota, si la hay: dato, no decoración. Sin emoji: la estrella es del
        // set de iconos del sistema (Lucide), no un glifo de otra fuente.
        if (game.rating > 0.0) {
            Spacer(Modifier.width(GVSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.Star,
                    contentDescription = null,
                    // Dato, no acción: tinta suave. El acento queda para lo que
                    // se toca (decisión de sistema del 02/10).
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(GVSpacing.xs))
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", game.rating),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Barra de búsqueda de la pantalla Buscar.
 *
 * Re-anclada al design system (iteración 02/10, segunda vuelta): era un
 * `TextField` relleno con `surfaceVariant`, sombra de 4 dp y esquinas de 24 dp
 * — un buscador de Material 2 en una app que ya no usa ni sombras ni rellenos.
 * Ahora es **`GVSearchField`**, el único buscador del sistema, el mismo que usa
 * el Top 4 y Social.
 *
 * @param buscando muestra progreso en el hueco de la acción derecha.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    buscando: Boolean = false
) {
    GVSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = "Buscar juegos...",
        buscando = buscando,
        onSearch = onSearch,
        modifier = Modifier.padding(
            horizontal = GVSpacing.screenPadding,
            vertical = GVSpacing.sm
        )
    )
}

@Preview(showBackground = true)
@Composable
@Suppress("ViewModelConstructorInComposable") // Aquí es un preview: SearchViewModel() usa su constructor explícito sin args para previsualización.
fun SearchScreenPreview() {

    // Se crea un ViewModel de ejemplo para la vista previa.
    val fakeViewModel = SearchViewModel()
    SearchScreen(
        navController = NavController(LocalContext.current),
        isDarkTheme = false,
        viewModel = fakeViewModel,
        paddingValues = PaddingValues()
    )
}

/**
 * Fila horizontal de descubrimiento (bloque B, iteración 02/10).
 *
 * Carrusel de carátulas con su título. Se usa cuando la pantalla Buscar no tiene
 * término escrito: en vez de un mensaje vacío, contenido que invita a explorar.
 * LazyRow — la auditoría UI detectó que la app no tenía ninguno.
 */
@Composable
private fun FilaDescubrimiento(
    titulo: String,
    juegos: List<CatalogGame>,
    navController: NavController
) {
    if (juegos.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(juegos, key = { it.id }) { juego ->
                Column(
                    modifier = Modifier
                        .width(132.dp)
                        // El clip vive en la CARÁTULA (que ya trae su propia forma):
                        // aquí recortaba el texto de debajo y cortaba la primera letra.
                        .clickable { navController.navigate("gameDetails/${juego.id}") }
                ) {
                    GameCover(
                        imageUrl = juego.coverUrl,
                        title = juego.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                    )
                    Text(
                        text = juego.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
