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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Tag
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.RecommendationEngine
import es.androidtfm.gamevision.data.library.RecommendationEngine.Candidate
import es.androidtfm.gamevision.data.library.RecommendationEngine.Mood
import es.androidtfm.gamevision.data.model.GameList
import com.composables.icons.lucide.Gamepad2
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.views.composables.profile.CrearListaDialog
import es.androidtfm.gamevision.ui.views.composables.profile.TarjetaCrearLista
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Home «Para ti» (iteración 02/10, bloque F): la pantalla central.
 *
 * Responde «¿qué hago ahora?» en un scroll de secciones, mezclando lo tuyo
 * (biblioteca), el motor de recomendación (T3.1/T3.2), el catálogo y las
 * noticias — que dejan de ser pestaña y pasan a ser una sección más (D-F3).
 * El diario sale del dock: su histórico se abre desde «Continúa».
 *
 * Todo lo que se muestra usa datos que ya existían salvo los precios
 * (CheapShark, gratis y sin clave). Con biblioteca vacía la pantalla se llena
 * con catálogo, así que una cuenta nueva nunca ve huecos.
 */

private val OPCIONES_TIEMPO = listOf(
    "30 min" to 30,
    "2 h" to 120,
    "Una tarde" to 240
)

@Composable
fun ParaTiScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    userViewModel: UserViewModel,
    libraryViewModel: LibraryViewModel,
    searchViewModel: SearchViewModel,
    socialViewModel: SocialViewModel,
    generosFavoritos: List<String>
) {
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()
    // Perfil en vivo: decide si mostrar la invitación a conectar Steam.
    val perfilSteam by userViewModel.profile.collectAsStateWithLifecycle()
    var biblioteca by remember { mutableStateOf<List<es.androidtfm.gamevision.data.library.LibraryEntry>>(emptyList()) }
    var minutosDisponibles by remember { mutableStateOf(120) }
    var animo by remember { mutableStateOf(Mood.ANY) }

    // Listas curadas (T2.6): la Home ofrece crearlas y cuenta cuántas hay.
    val listas by socialViewModel.lists.collectAsStateWithLifecycle()
    var mostrarCrearLista by remember { mutableStateOf(false) }
    // El Top 4 es una lista real, pero la Home no lo cuenta como «tuya»: ya tiene
    // su propio hueco en el perfil y aquí sería confuso.
    val listasCount = listas?.getOrDefault(emptyList())
        ?.count { it.id != GameList.TOP4_ID } ?: 0
    LaunchedEffect(uid) {
        uid?.takeIf { it.isNotBlank() }?.let { socialViewModel.loadLists(it) }
    }

    // Biblioteca en vivo: alimenta «Continúa» y el motor de recomendación.
    LaunchedEffect(uid) {
        val id = uid ?: return@LaunchedEffect
        libraryViewModel.observeLibrary(id).collect { resultado ->
            biblioteca = resultado.getOrDefault(emptyList())
        }
    }

    // Catálogo para las secciones de descubrimiento (populares y novedades).
    val populares by searchViewModel.populares.collectAsStateWithLifecycle()
    val paraTiCatalogo by searchViewModel.paraTi.collectAsStateWithLifecycle()
    LaunchedEffect(generosFavoritos) {
        searchViewModel.cargarDescubrimiento(generosFavoritos)
    }

    val enCurso = biblioteca.filter { it.status == LibraryStatus.PLAYING }
    val deseados = biblioteca.filter { it.status == LibraryStatus.WISHED }

    // El motor (T3.1) recibe la biblioteca y el tiempo elegido.
    val recomendaciones = remember(biblioteca, minutosDisponibles, animo, generosFavoritos) {
        RecommendationEngine.recommend(
            candidates = biblioteca.map {
                Candidate(
                    gameId = it.gameId,
                    name = it.name,
                    status = it.status,
                    genres = it.genres,
                    rating = it.rating,
                    minutesTotal = it.minutesTotal,
                    durationMin = it.playtimeManual
                )
            },
            availableMinutes = minutosDisponibles,
            favoriteGenres = generosFavoritos.toSet(),
            mood = animo,
            max = 2
        )
    }

    // Cabecera FIJA (iteración 02/10): el título no se desplaza, solo el contenido.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        GVScreenHeader(title = "Para ti", modifier = Modifier.padding(horizontal = GVSpacing.screenPadding))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

        // ---- Tu Rewind (F3/T3.7): la celebración del año -----------------
        // Va el primero porque es el motor viral del producto: lo que se comparte.
        Spacer(Modifier.height(GVSpacing.md))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GVSpacing.screenPadding)
                .clickable { navController.navigate("rewind") },
            shape = GVShapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(GVSpacing.xl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Iconos decorativos: acompañan a un texto que ya lo dice, así que
                // van en tinta secundaria y sin descripción (ADR-0012).
                Icon(
                    Lucide.Sparkles,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(GVSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Tu Rewind",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "El resumen de tu año en juegos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Lucide.ArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(GVSpacing.xl))

        // ---- Continúa ---------------------------------------------------
        if (enCurso.isNotEmpty()) {
            SeccionCabecera("Continúa", "Tu diario") { navController.navigate("diary") }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enCurso, key = { it.gameId }) { entrada ->
                    Column(
                        modifier = Modifier
                            .width(132.dp)
                            // El clip vive en la CARÁTULA (GameCover ya trae la suya):
                            // aquí recortaba el texto de debajo y cortaba la 1ª letra.
                            .clickable { navController.navigate("gameDetails/${entrada.gameId}") }
                    ) {
                        GameCover(
                            imageUrl = entrada.coverUrl,
                            title = entrada.name,
                            modifier = Modifier.fillMaxWidth().height(176.dp)
                        )
                        Text(
                            text = entrada.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        if (entrada.minutesTotal > 0) {
                            Text(
                                text = "Llevas ${entrada.minutesTotal / 60} h",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ---- Tus listas (T2.6, revisión 02/10) --------------------------
        // Entrada LIGERA: en la Home no se crea una lista con un formulario en
        // medio del scroll, se ofrece el acceso. El diálogo es el mismo que usa
        // el perfil (un solo sitio con la regla de nombre/privacidad).
        SeccionCabecera("Tus listas", null) { }
        Column(modifier = Modifier.padding(horizontal = GVSpacing.screenPadding)) {
            TarjetaCrearLista(
                onCrear = { mostrarCrearLista = true },
                listasCount = listasCount
            )
        }

        // ---- ¿Qué juego ahora? (T3.2) -----------------------------------
        SeccionCabecera("¿Qué juego ahora?", subtitulo = null) { }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OPCIONES_TIEMPO.forEach { (etiqueta, minutos) ->
                    GVChip(
                        text = etiqueta,
                        selected = minutosDisponibles == minutos,
                        onClick = { minutosDisponibles = minutos }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Mood.ANY, Mood.SHORT, Mood.STORY).forEach { m ->
                    GVChip(
                        text = m.label,
                        selected = animo == m,
                        onClick = { animo = m }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (recomendaciones.isEmpty()) {
                Text(
                    text = "Añade juegos a tu biblioteca y aquí aparecerá qué jugar hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recomendaciones.forEach { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { navController.navigate("gameDetails/${rec.gameId}") },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Lucide.Sparkles,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = rec.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            // D3.2: la recomendación SIEMPRE se explica.
                            Text(
                                text = rec.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ---- Te está esperando (ofertas de la wishlist) ------------------
        if (deseados.isNotEmpty()) {
            SeccionCabecera("Te está esperando", "En tu lista de deseos") { }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deseados, key = { it.gameId }) { entrada ->
                    Column(
                        modifier = Modifier
                            .width(132.dp)
                            // El clip vive en la CARÁTULA (GameCover ya trae la suya):
                            // aquí recortaba el texto de debajo y cortaba la 1ª letra.
                            .clickable { navController.navigate("gameDetails/${entrada.gameId}") }
                    ) {
                        GameCover(
                            imageUrl = entrada.coverUrl,
                            title = entrada.name,
                            modifier = Modifier.fillMaxWidth().height(176.dp)
                        )
                        Text(
                            text = entrada.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Lucide.Tag,
                                contentDescription = null,
                                modifier = Modifier.width(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " Pulsa para ver ofertas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ---- Novedades en tus géneros -----------------------------------
        val generos = if (generosFavoritos.isEmpty()) populares else paraTiCatalogo
        if (generos.isNotEmpty()) {
            SeccionCabecera(
                titulo = if (generosFavoritos.isEmpty()) "Populares ahora"
                else "Porque te gusta " + generosFavoritos.take(2).joinToString(" y "),
                subtitulo = null
            ) { }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(generos, key = { it.id }) { juego ->
                    Column(
                        modifier = Modifier
                            .width(132.dp)
                            .clickable { navController.navigate("gameDetails/${juego.id}") }
                    ) {
                        GameCover(
                            imageUrl = juego.coverUrl,
                            title = juego.name,
                            modifier = Modifier.fillMaxWidth().height(176.dp)
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

        // ---- Steam (bloque 5): invitación indirecta, oculta si ya está vinculada
        if (perfilSteam.steamId.isBlank()) {
            Column(modifier = Modifier.padding(horizontal = GVSpacing.screenPadding)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Lucide.Gamepad2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(GVSpacing.sm))
                            Text("Tu tiempo real", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(GVSpacing.xs))
                        Text(
                            text = "Conecta Steam y tus horas jugadas entrarán solas en tu " +
                                "biblioteca: sin apuntar nada a mano.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(GVSpacing.md))
                        GVButton(
                            text = "Conectar Steam",
                            onClick = { navController.navigate("steam") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ---- Lo que está pasando (noticias, ya no pestaña: D-F3) ---------
        SeccionCabecera("Lo que está pasando", "Ver todas") { navController.navigate("news") }
        Text(
            text = "Los titulares de videojuegos del día tienen su pantalla completa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarCrearLista) {
        CrearListaDialog(
            onDismiss = { mostrarCrearLista = false },
            onCrear = { nombre, descripcion, esPublica ->
                mostrarCrearLista = false
                val miUid = uid.orEmpty()
                if (miUid.isNotBlank()) {
                    socialViewModel.createList(miUid, nombre, descripcion, esPublica) { result ->
                        if (result.isSuccess) {
                            socialViewModel.loadLists(miUid)
                        } else {
                            userViewModel.setMessage("No se pudo crear la lista")
                        }
                    }
                }
            }
        )
    }
}

/** Título de sección con acción opcional a la derecha. */
@Composable
private fun SeccionCabecera(
    titulo: String,
    subtitulo: String?,
    accion: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Más aire arriba (iteración 02/10): el título de página en Home va
            // seguido de OTRO título grande; con 8dp el bloque se sentía pegado
            // comparado con pantallas donde debajo hay tarjetas.
            .padding(horizontal = GVSpacing.screenPadding, vertical = 8.dp)
            .padding(top = GVSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            if (subtitulo != null) {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (subtitulo == "Ver todas" || subtitulo == "Tu diario") {
            TextButton(onClick = accion) {
                Text(subtitulo)
                Icon(
                    imageVector = Lucide.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.width(16.dp)
                )
            }
        }
    }
}

@Composable
private fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
