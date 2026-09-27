package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import es.androidtfm.gamevision.ui.designsystem.GVShapeFull

/*
 * GameVision Design System — RatingBadge (ver DESIGN.md §6).
 *
 * Pill completa con fondo verde ácido y texto oscuro: el único elemento
 * "de color" de una tarjeta de juego.
 */

@Composable
fun RatingBadge(
    rating: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(GVShapeFull)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "★ $rating",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
