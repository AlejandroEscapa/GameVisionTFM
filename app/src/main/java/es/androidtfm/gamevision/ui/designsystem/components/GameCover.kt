package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GameCover (ver DESIGN.md §6).
 *
 * Portada de juego con placeholder monocromo con iniciales. La imagen manda:
 * sin bordes, sin elevación, clip al shape indicado.
 *
 * D-U1: AsyncImage (no SubcomposeAsyncImage) — el placeholder y el error van
 * como Painter, así los items de GameCard, Search, GameList, Diary y Details
 * no subcomponen un árbol por portada al hacer scroll.
 */

@Composable
fun GameCover(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = GVShapes.medium,
    contentDescription: String? = null
) {
    // Los colores de tema se leen en composición (no durante el draw) y quedan
    // congelados en el painter.
    val textMeasurer = rememberTextMeasurer()
    val container = MaterialTheme.colorScheme.surfaceContainer
    val content = MaterialTheme.colorScheme.onSurfaceVariant
    val placeholder = remember(title, textMeasurer, container, content) {
        CoverPlaceholderPainter(title, container, content, textMeasurer)
    }
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription ?: title,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape),
        placeholder = placeholder,
        error = placeholder
    )
}

/**
 * Placeholder monocromo con las iniciales del título. Painter (no composable)
 * para poder usarlo desde `placeholder`/`error` de AsyncImage (D-U1).
 */
private class CoverPlaceholderPainter(
    private val title: String,
    private val container: Color,
    private val content: Color,
    private val textMeasurer: TextMeasurer
) : Painter() {
    override val intrinsicSize = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(container)
        // Centrado y ESCALADO al tamaño de la portada (iteración 02/10): antes
        // se dibujaba en Offset.Zero con 32.sp fijos, así que las iniciales
        // quedaban pegadas a la esquina y el clip redondeado las cortaba —
        // exactamente el bug que vio el propietario ("un círculo y fuera el borde").
        val iniciales = title.take(2).uppercase()
        // DrawScope implementa Density: puedo convertir px -> sp directamente.
        val fontSize = if (size.width > 0f) (size.width * 0.34f).toSp() else 32.sp
        val medida = textMeasurer.measure(
            text = iniciales,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = content
            ),
            maxLines = 1
        )
        drawText(
            textLayoutResult = medida,
            topLeft = Offset(
                x = (size.width - medida.size.width) / 2f,
                y = (size.height - medida.size.height) / 2f
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun GameCoverPreview() {
    GameVisionTheme(darkTheme = true) {
        GameCover(
            imageUrl = null,
            title = "Hollow Knight",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}
