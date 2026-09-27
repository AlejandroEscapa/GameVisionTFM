package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — NewsCard (ver DESIGN.md §6).
 *
 * Imagen 16:9 + titular + fuente/fecha en labelSmall gris.
 */

@Composable
fun NewsCard(
    title: String,
    imageUrl: String?,
    sourceAndDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameCover(
            imageUrl = imageUrl,
            title = title,
            contentDescription = title,
            shape = GVShapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sourceAndDate.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun NewsCardPreview() {
    GameVisionTheme(darkTheme = true) {
        NewsCard(
            title = "Nintendo anuncia el próximo Direct para esta semana",
            imageUrl = null,
            sourceAndDate = "IGN · 14:30 05-01-2026",
            onClick = {},
            modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)
        )
    }
}
