package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.PlaySession
import es.androidtfm.gamevision.data.library.RewindData
import es.androidtfm.gamevision.data.library.RewindGame
import es.androidtfm.gamevision.data.library.RewindUtils
import es.androidtfm.gamevision.data.storage.ShareImageStorage
import es.androidtfm.gamevision.ui.designsystem.GVMotion
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.LocalReduceMotion
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Pantalla del GameVision Rewind (F3 — Bloque B, T3.7).
 *
 * Es la celebración del año: lo que el usuario comparte. Reglas que aplica:
 *  - El cálculo es lógica pura y vive en RewindUtils (T3.6); aquí solo se pinta.
 *  - D3.6: **celebrar siempre**. Si el año no tiene actividad, la pantalla NO
 *    enseña un estado vacío ni ceros: enseña su titular y una frase de bienvenida
 *    (CA3.5).
 *  - ADR-0013 §5.1: la entrada es fade + slide de 16 dp con el easing del sistema
 *    (el único `tween` permitido), y el escalonado se consulta con
 *    `LocalReduceMotion`: con «reducir movimiento» activo no hay ni retardo ni
 *    desplazamiento, solo aparece.
 *  - ADR-0012: los datos van en tinta y se jerarquizan por tamaño. Nada de acento
 *    en las cifras.
 *
 * Sobre el scroll: se usa `verticalScroll` y no `LazyColumn` porque el contenido
 * es un conjunto fijo y corto de tarjetas y **no** hay animación ligada al scroll
 * (que es el caso en el que `LazyColumn` sí es obligatorio).
 */

@Composable
fun RewindScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel
) {
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()
    val library by remember(uid) { libraryViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)
    val sessions by remember(uid) { libraryViewModel.observeSessions(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) { libraryViewModel.logScreen("rewind") }

    val entries: List<LibraryEntry>? = library?.getOrNull()
    val sessionList: List<PlaySession>? = sessions?.getOrNull()
    val now = remember { System.currentTimeMillis() }

    val years = remember(entries, sessionList) {
        RewindUtils.availableYears(entries.orEmpty(), sessionList.orEmpty(), now)
    }
    // El año elegido sobrevive a la rotación (rememberSaveable).
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    val year = selectedYear ?: years.firstOrNull() ?: RewindUtils.yearOf(now) ?: 0

    val rewind: RewindData? = remember(entries, sessionList, year) {
        entries?.let { RewindUtils.compute(it, sessionList.orEmpty(), year) }
    }

    val reduceMotion = LocalReduceMotion.current

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Capa gráfica donde se dibuja el póster: es lo que se convierte en imagen (T3.8).
    // Se captura lo que está en pantalla, así que la imagen compartida es exactamente
    // lo que el usuario ve. Sin plantillas paralelas que se desincronicen del diseño.
    val posterLayer = rememberGraphicsLayer()

    fun compartirRewind() {
        val datos = rewind ?: return
        scope.launch {
            val bitmap = posterLayer.toImageBitmap().asAndroidBitmap()
            val uri = ShareImageStorage.writeToCache(
                context = context,
                bitmap = bitmap,
                fileName = "gamevision-rewind-${datos.year}.png"
            )
            val enviar = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Mi Rewind ${datos.year} en GameVision: " +
                        "${RewindUtils.formatHoursTotal(datos.minutesTotal)} jugadas"
                )
                // Sin este permiso el receptor no puede leer el content:// URI.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(enviar, "Compartir tu Rewind"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = GVSpacing.screenPadding)
    ) {
        // Cabecera FIJA: no se desplaza, solo el contenido (DESIGN.md §7).
        GVScreenHeader(
            title = "Rewind $year",
            leading = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                }
            },
            actions = {
                // Icono INTERACTIVO: sí lleva acento (ADR-0012). Se deshabilita
                // cuando no hay actividad, porque no habría póster que capturar.
                IconButton(
                    onClick = { compartirRewind() },
                    enabled = rewind?.hasActivity == true
                ) {
                    Icon(
                        Lucide.Share2,
                        contentDescription = "Compartir tu Rewind",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (rewind == null) {
                Column(verticalArrangement = Arrangement.spacedBy(GVSpacing.md)) {
                    repeat(3) { GameRowSkeleton() }
                }
            } else {
                if (years.size > 1) {
                    Spacer(Modifier.height(GVSpacing.md))
                    FilaDeAnos(years = years, selected = year, onSelect = { selectedYear = it })
                }

                Spacer(Modifier.height(GVSpacing.headerGap))
                RevealItem(index = 0, reduceMotion = reduceMotion) {
                    TitularDelAno(rewind)
                }

                if (rewind.hasActivity) {
                    Spacer(Modifier.height(GVSpacing.md))
                    RevealItem(index = 1, reduceMotion = reduceMotion) {
                        Box(
                            modifier = Modifier.drawWithContent {
                                posterLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(posterLayer)
                            }
                        ) {
                            PosterDelRewind(rewind)
                        }
                    }
                    Spacer(Modifier.height(GVSpacing.md))
                    RevealItem(index = 2, reduceMotion = reduceMotion) {
                        TarjetaDeMomentos(rewind)
                    }
                    if (rewind.topGames.isNotEmpty()) {
                        Spacer(Modifier.height(GVSpacing.xl))
                        RevealItem(index = 3, reduceMotion = reduceMotion) {
                            TusJuegosDelAno(rewind.topGames)
                        }
                    }
                } else {
                    // Caso «poca actividad» (CA3.5): nunca un estado vacío ni un cero.
                    Spacer(Modifier.height(GVSpacing.md))
                    RevealItem(index = 1, reduceMotion = reduceMotion) {
                        TarjetaDeBienvenida(rewind.highlights.firstOrNull())
                    }
                }

                Spacer(Modifier.height(GVSpacing.xxl))
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Animación de entrada
// ----------------------------------------------------------------------------

/**
 * Revela su contenido con fade + slide de 16 dp, escalonado por [index].
 *
 * El paso del escalonado es `duración / 3` como mínimo, porque `DESIGN.md` §5
 * limita a **3 los elementos animando a la vez**: con un paso menor se solaparían
 * cuatro o más. Con «reducir movimiento» activo aparece sin retardo ni
 * desplazamiento (ADR-0013 §5.2).
 */
@Composable
private fun RevealItem(
    index: Int,
    reduceMotion: Boolean,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val slide = with(LocalDensity.current) { 16.dp.roundToPx() }
    val pasoMs = maxOf(GVMotion.STAGGER_INCREMENT_MS, (GVMotion.DURATION_NORMAL / 3).toLong())

    LaunchedEffect(reduceMotion, index) {
        if (reduceMotion) {
            visible = true
        } else {
            delay(index.coerceAtMost(6) * pasoMs)
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(GVMotion.DURATION_NORMAL, easing = GVMotion.EnterEasing)) +
            slideInVertically(
                animationSpec = tween(GVMotion.DURATION_NORMAL, easing = GVMotion.EnterEasing)
            ) { slide }
    ) {
        content()
    }
}

// ----------------------------------------------------------------------------
// Secciones
// ----------------------------------------------------------------------------

@Composable
private fun FilaDeAnos(years: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(GVSpacing.sm)
    ) {
        years.forEach { year ->
            GVChip(
                text = year.toString(),
                selected = year == selected,
                onClick = { onSelect(year) }
            )
        }
    }
}

@Composable
private fun TitularDelAno(rewind: RewindData) {
    Column {
        Text(
            text = rewind.headline,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(GVSpacing.sm))
        Text(
            text = if (rewind.hasActivity) {
                "Esto es lo que jugaste. Sin rankings ni comparaciones."
            } else {
                "Un año tranquilo también cuenta. Aquí está tu resumen."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * El póster del Rewind: lo que se comparte (T3.8).
 *
 * **Sin altura forzada, a propósito.** La primera versión era un cuadrado con
 * `aspectRatio(1f)` y `SpaceBetween`; al agrandar la portada el contenido desbordó y
 * el pie quedó **recortado en la imagen compartida** (visto en el emulador el
 * 02/10/2026). Como la imagen se captura tal cual se ve, un póster recortado es un
 * póster roto: la altura la dicta el contenido y así no hay ancho de pantalla que
 * lo corte. Sin acento y sin sombra: el póster no tiene nada que se toque (ADR-0012),
 * así que se sostiene con superficie, tinta y escala tipográfica.
 */
@Composable
private fun PosterDelRewind(rewind: RewindData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GVShapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(GVSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(GVSpacing.xl)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.gamevisionnoletters),
                    contentDescription = null,
                    modifier = Modifier.height(24.dp)
                )
                Spacer(Modifier.width(GVSpacing.sm))
                Text(
                    text = "GameVision",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column {
                Text(
                    text = "Rewind ${rewind.year}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = RewindUtils.formatHoursTotal(rewind.minutesTotal),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "jugadas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rewind.topGames.isNotEmpty()) {
                // Altura fija de portada: así el póster mide lo mismo con 1 juego que con
                // 3 y la imagen compartida no baila de tamaño entre usuarios.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = GVSpacing.sm,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    rewind.topGames.take(3).forEach { juego ->
                        GameCover(
                            imageUrl = juego.coverUrl,
                            title = juego.name,
                            modifier = Modifier
                                .height(140.dp)
                                .aspectRatio(0.66f),
                            contentDescription = null
                        )
                    }
                }
            }

            Column {
                rewind.topGame?.let { juego ->
                    Text(
                        text = "Tu juego del año: ${juego.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = RewindUtils.resumenDeJuegos(rewind.gamesPlayed, rewind.gamesCompleted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaDeMomentos(rewind: RewindData) {
    TarjetaDelSistema {
        Column(Modifier.padding(GVSpacing.xl), verticalArrangement = Arrangement.spacedBy(GVSpacing.sm)) {
            rewind.topGenres.firstOrNull()?.let { (genero, veces) ->
                DatoLineal("Género dominante", "$genero ($veces)")
            }
            rewind.busiestMonth?.let { mes ->
                DatoLineal("Mes más activo", "${mes.label}, ${RewindUtils.formatHoursTotal(mes.minutes)}")
            }
            if (rewind.longestStreakDays > 1) {
                DatoLineal("Racha más larga", "${rewind.longestStreakDays} días seguidos")
            }
            rewind.mainPlatform?.let { plataforma ->
                DatoLineal("Plataforma principal", plataforma)
            }
            rewind.ratingAverage?.let { nota ->
                DatoLineal("Nota media", RewindUtils.formatNota(nota))
            }
        }
    }
}

@Composable
private fun TarjetaDeBienvenida(mensaje: String?) {
    TarjetaDelSistema {
        Column(Modifier.padding(GVSpacing.xl)) {
            Text(
                text = mensaje ?: "Tu biblioteca te está esperando",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(GVSpacing.sm))
            Text(
                text = "Cuando apuntes tus primeras partidas, aquí verás tu año entero.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TusJuegosDelAno(juegos: List<RewindGame>) {
    Column {
        Text(
            text = "Tus juegos del año",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(GVSpacing.md))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(GVSpacing.md)
        ) {
            juegos.forEach { juego ->
                Column(modifier = Modifier.width(120.dp)) {
                    GameCover(
                        imageUrl = juego.coverUrl,
                        title = juego.name,
                        modifier = Modifier
                            .width(120.dp)
                            .height(180.dp),
                        contentDescription = "Carátula de ${juego.name}"
                    )
                    Spacer(Modifier.height(GVSpacing.sm))
                    Text(
                        text = juego.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = RewindUtils.formatHoursTotal(juego.minutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Piezas del sistema
// ----------------------------------------------------------------------------

/** Tarjeta del sistema: superficie de contenedor, radio 18, SIN sombra (§6). */
@Composable
private fun TarjetaDelSistema(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GVShapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) { content() }
}

@Composable
private fun DatoLineal(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
