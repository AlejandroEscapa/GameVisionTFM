package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.androidtfm.gamevision.data.library.LibraryStatus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.StarHalf

/*
 * Componentes de la biblioteca rica (F1 — Bloque 1).
 *
 *  - RatingStars       → nota personal con medias estrellas (0,5–5,0; ADR-0003).
 *  - LibraryStatusRow  → selector de los 7 estados.
 *  - ReviewEditor      → reseña escrita (corta, cultura "una línea").
 *
 * Son presentacionales: reciben el valor y devuelven el cambio; la persistencia
 * la hace la pantalla a través del ViewModel.
 */

/**
 * Fila de 5 estrellas para elegir la nota con medias estrellas.
 * Tocar la **mitad izquierda** de una estrella pone `n.5`; la **derecha**, `n.0`.
 * Si ya hay nota, aparece «Quitar» para dejarla vacía.
 *
 * @param rating nota actual (null = sin nota).
 * @param onRatingChange nueva nota (null al quitarla).
 * @param starSize tamaño de cada estrella.
 */
@Composable
fun RatingStars(
    rating: Double?,
    onRatingChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
    enabled: Boolean = true
) {
    val current = rating ?: 0.0
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val starWidthPx = starSize.toPx() + 2.dp.toPx()
                    val starIndex = (offset.x / starWidthPx).toInt() + 1 // 1..5
                    val withinStar = offset.x % starWidthPx
                    val isLeftHalf = withinStar < starWidthPx / 2f
                    val value = starIndex - if (isLeftHalf) 0.5 else 0.0
                    onRatingChange(es.androidtfm.gamevision.data.library.RatingUtils.snap(value))
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val fill = current - i + 1 // >0 relleno; 0.5 mitad; <=0 vacío
                val icon = when {
                    fill >= 1.0 -> Lucide.Star
                    fill >= 0.5 -> Lucide.StarHalf
                    else -> Lucide.Star
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "$i estrellas",
                    tint = if (fill > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(starSize)
                )
            }
        }
        if (rating != null && enabled) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatRating(rating),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { onRatingChange(null) }) {
                Text("Quitar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Formatea la nota como «4,5» (coma decimal, estilo español). */
fun formatRating(rating: Double): String {
    val rounded = es.androidtfm.gamevision.data.library.RatingUtils.round1(rating)
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()},0" else rounded.toString().replace('.', ',')
}

/**
 * Selector de los 7 estados de la biblioteca, como chips en filas.
 * El estado actual aparece seleccionado; tocar otro dispara [onSelect].
 */
@Composable
fun LibraryStatusSelector(
    current: LibraryStatus?,
    onSelect: (LibraryStatus) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LibraryStatus.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { status ->
                    es.androidtfm.gamevision.ui.designsystem.components.GVChip(
                        text = status.label,
                        selected = current == status,
                        onClick = { if (enabled) onSelect(status) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Editor de reseña (F1/T1.4): campo de texto corto con contador.
 * No bloquea: la reseña siempre es opcional (decisión D1.2).
 */
@Composable
fun ReviewEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxChars: Int = 280
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxChars) onValueChange(it) },
        label = { Text("Tu reseña (opcional)") },
        placeholder = { Text("@`Una línea` que resuma lo que te dejó") },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(12.dp),
        supportingText = {
            Text(
                text = "${value.length}/$maxChars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Aviso de estado de la ficha (usado al completar/coleccionar con nota sugerida).
 * Se mantiene aquí para que la ficha y la biblioteca compartan el mismo lenguaje.
 */
@Composable
fun LibraryStatusHint(status: LibraryStatus, modifier: Modifier = Modifier) {
    val hint = when (status) {
        LibraryStatus.COMPLETED -> "¡Completado! ¿Le pones nota?"
        LibraryStatus.COLLECTED -> "¡Coleccionado al 100 %! ¿Le pones nota?"
        LibraryStatus.PLAYING -> "En curso"
        LibraryStatus.PAUSED -> "En pausa"
        LibraryStatus.RETIRED -> "Retirado"
        LibraryStatus.ABANDONED -> "Abandonado"
        LibraryStatus.WISHED -> "En tu lista de deseos"
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
