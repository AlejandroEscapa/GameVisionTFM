package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.SubcomposeAsyncImage
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GameCover (ver DESIGN.md §6).
 *
 * Portada de juego con crossfade y placeholder monocromo con iniciales.
 * La imagen manda: sin bordes, sin elevación, clip al shape indicado.
 */

@Composable
fun GameCover(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = GVShapes.medium,
    contentDescription: String? = null
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription ?: title,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape),
        loading = {
            CoverPlaceholder(title, Modifier.fillMaxSize())
        },
        error = {
            CoverPlaceholder(title, Modifier.fillMaxSize())
        }
    )
}

@Composable
private fun CoverPlaceholder(title: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.take(2).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
