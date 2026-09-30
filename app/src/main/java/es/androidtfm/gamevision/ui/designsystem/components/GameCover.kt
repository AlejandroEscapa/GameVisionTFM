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
        drawText(
            textMeasurer = textMeasurer,
            text = title.take(2).uppercase(),
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = content
            ),
            maxLines = 1
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
