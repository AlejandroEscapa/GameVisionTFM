package es.androidtfm.gamevision.ui.views.composables.profile

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.TopGamesLogic
import es.androidtfm.gamevision.data.model.TopGame
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GVSearchField
import es.androidtfm.gamevision.ui.designsystem.components.GameCover

/*
 * T2.5 (revisión 02/10) — Cuadro del Top 4 con buscador de catálogo.
 *
 * Vive en el PERFIL, justo debajo de la tarjeta de información. Sustituye al
 * antiguo `TopGamesEditor` de «Editar perfil», que solo ofrecía los juegos ya
 * presentes en la biblioteca (una lista corta y sin buscador).
 *
 * Dos modos con el MISMO componente:
 *  · [editable] = true  → tu perfil: buscador + botón de quitar en cada slot.
 *  · [editable] = false → perfil de otro: solo lectura, sin buscador.
 *
 * El buscador consulta el CATÁLOGO (RAWG), no la biblioteca: puedes elegir un
 * juego que no tengas. Por eso cada slot guarda su propia miniatura (ver TopGame).
 */

/** Filas de resultado que se muestran bajo el buscador (evita un scroll infinito). */
private const val MAX_RESULTADOS = 6

@Composable
fun TopGamesCard(
    seleccionados: List<TopGame>,
    editable: Boolean,
    modifier: Modifier = Modifier,
    resultados: List<CatalogGame> = emptyList(),
    buscando: Boolean = false,
    errorBusqueda: String? = null,
    onBuscar: (String) -> Unit = {},
    onLimpiarBusqueda: () -> Unit = {},
    onAnadir: (TopGame) -> Unit = {},
    onQuitar: (String) -> Unit = {},
    /** El buscador solo hace falta donde se edita; en lectura no se pinta. */
    mostrarBuscador: Boolean = editable
) {
    var query by remember { mutableStateOf("") }
    val completo = !TopGamesLogic.hasRoom(seleccionados)
    val keyboard = LocalSoftwareKeyboardController.current

    // Al quedarse sin hueco se cierra el buscador: no se puede añadir nada más.
    LaunchedEffect(completo) {
        if (completo) {
            query = ""
            onLimpiarBusqueda()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = GVShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(GVSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu Top 4",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (seleccionados.isEmpty()) {
                            "Tu carta de presentación: elige tus cuatro juegos."
                        } else {
                            "Lo que te define como jugador."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${seleccionados.size}/${TopGame.MAX}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(GVSpacing.md))

            // Los cuatro slots, SIEMPRE visibles: los vacíos invitan a rellenarlos.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GVSpacing.sm)
            ) {
                repeat(TopGame.MAX) { indice ->
                    val juego = seleccionados.getOrNull(indice)
                    SlotTop(
                        posicion = indice + 1,
                        juego = juego,
                        editable = editable,
                        onQuitar = { juego?.let { onQuitar(it.gameId) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (mostrarBuscador && !completo) {
                Spacer(Modifier.height(GVSpacing.md))
                // El mismo buscador del sistema que la pantalla Buscar y Social.
                GVSearchField(
                    value = query,
                    onValueChange = {
                        query = it
                        onBuscar(it)
                    },
                    placeholder = "Busca un juego para añadirlo",
                    buscando = buscando,
                    onSearch = {
                        keyboard?.hide()
                        onBuscar(query)
                    }
                )

                if (errorBusqueda != null) {
                    Spacer(Modifier.height(GVSpacing.sm))
                    Text(
                        text = errorBusqueda,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val visibles = resultados.take(MAX_RESULTADOS)
                if (query.trim().length >= 2 && visibles.isNotEmpty()) {
                    Spacer(Modifier.height(GVSpacing.sm))
                    Column(verticalArrangement = Arrangement.spacedBy(GVSpacing.xs)) {
                        visibles.forEach { juego ->
                            FilaResultado(
                                juego = juego,
                                yaEnElTop = TopGamesLogic.contains(seleccionados, juego.id.toString()),
                                onClick = {
                                    if (TopGamesLogic.contains(seleccionados, juego.id.toString())) {
                                        onQuitar(juego.id.toString())
                                    } else {
                                        onAnadir(
                                            TopGamesLogic.from(
                                                id = juego.id.toString(),
                                                name = juego.name,
                                                coverUrl = juego.coverUrl
                                            )
                                        )
                                        query = ""
                                        onLimpiarBusqueda()
                                        keyboard?.hide()
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (mostrarBuscador && completo) {
                Spacer(Modifier.height(GVSpacing.md))
                Text(
                    text = "Top completo. Toca la ✕ de un juego para cambiarlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Un slot del Top. Con juego: portada, nombre y posición; con [editable], una ✕
 * para liberarlo. Vacío: hueco punteado con el número de posición.
 */
@Composable
private fun SlotTop(
    posicion: Int,
    juego: TopGame?,
    editable: Boolean,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Micro-interacción: el slot entra con muelle cuando se rellena.
    val escala by animateFloatAsState(
        targetValue = if (juego != null) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 600f
        ),
        label = "slot-top"
    )

    Column(
        modifier = modifier.scale(escala),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (juego != null) {
                GameCover(
                    imageUrl = juego.coverUrl.ifBlank { null },
                    title = juego.name.ifBlank { "Juego" },
                    shape = GVShapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                )
                if (editable) {
                    // Caja táctil de 48 dp con el círculo visible de 24: el mínimo
                    // de accesibilidad no se negocia por estética (antes eran 24 dp
                    // de zona pulsable, media recomendación).
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(48.dp)
                            .clickable(onClick = onQuitar),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = GVSpacing.xs, end = GVSpacing.xs)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Lucide.X,
                                contentDescription = "Quitar ${juego.name} del Top",
                                // `inverseOnSurface` es el rol de tinta sobre scrim
                                // en ambos temas; nada de blanco fijo.
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                // Número de posición: el orden del Top es información, no decoración.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(GVSpacing.xs)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$posicion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                        .clip(GVShapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = GVShapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = "Hueco $posicion libre",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(GVSpacing.xs))
        Text(
            text = juego?.name?.ifBlank { "Juego" } ?: "Libre",
            style = MaterialTheme.typography.labelSmall,
            color = if (juego != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Resultado del buscador: portada pequeña, nombre, año y estado en el Top. */
@Composable
private fun FilaResultado(
    juego: CatalogGame,
    yaEnElTop: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GVShapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = GVSpacing.xs, horizontal = GVSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameCover(
            imageUrl = juego.coverUrl,
            title = juego.name,
            shape = GVShapes.small,
            modifier = Modifier.size(width = 34.dp, height = 46.dp)
        )
        Spacer(Modifier.width(GVSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = juego.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val anio = juego.released.take(4)
            if (anio.length == 4) {
                Text(
                    text = anio,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (yaEnElTop) {
            GVChip(text = "En el Top", selected = true, onClick = onClick)
        } else {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = "Añadir ${juego.name} al Top",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Top 4 en modo lectura COMPACTO (perfil de otro): solo la fila de portadas.
 * Se mantiene separado del modo editable para no arrastrar el buscador a una
 * pantalla donde no pinta nada.
 */
@Composable
fun TopGamesRow(
    juegos: List<TopGame>,
    modifier: Modifier = Modifier
) {
    if (juegos.isEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GVSpacing.sm)
    ) {
        juegos.take(TopGame.MAX).forEachIndexed { indice, juego ->
            Column(modifier = Modifier.weight(1f)) {
                Box {
                    GameCover(
                        imageUrl = juego.coverUrl.ifBlank { null },
                        title = juego.name.ifBlank { "Juego" },
                        shape = GVShapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(GVSpacing.xs)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${indice + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
                Spacer(Modifier.height(GVSpacing.xs))
                Text(
                    text = juego.name.ifBlank { "Juego" },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Resuelve el Top 4 de un perfil antiguo (solo ids) contra su biblioteca.
 *
 * Las cuentas anteriores al 02/10 guardaron `topGameIds` sin miniatura. Mientras
 * no vuelvan a tocar su Top, la portada y el nombre salen de aquí. Devuelve un
 * resolver listo para `UserProfile.topGamesResolved`.
 */
fun resolvedorDesdeBiblioteca(biblioteca: List<LibraryEntry>?): (String) -> Pair<String, String>? {
    val porId = biblioteca.orEmpty().associateBy { it.gameId }
    return { id ->
        porId[id]?.let { it.name to it.coverUrl.orEmpty() }
    }
}
