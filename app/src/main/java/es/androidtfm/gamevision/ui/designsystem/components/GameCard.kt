package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GameCard (ver DESIGN.md §6).
 *
 * Portada (relación 2:3 RAWG) con rating pill superpuesto y título debajo.
 * Sin sombra: la portada manda sobre superficie neutra.
 */

@Composable
fun GameCard(
    title: String,
    imageUrl: String?,
    rating: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            GameCover(
                imageUrl = imageUrl,
                title = title,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            rating?.let {
                RatingBadge(
                    rating = String.format(java.util.Locale.US, "%.1f", it),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun GameCardPreview() {
    GameVisionTheme(darkTheme = true) {
        GameCard(
            title = "Hollow Knight: Silksong",
            imageUrl = null,
            rating = 4.5,
            onClick = {},
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.5f)
        )
    }
}
