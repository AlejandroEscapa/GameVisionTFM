package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import es.androidtfm.gamevision.ui.designsystem.GVShapeFull
import es.androidtfm.gamevision.ui.designsystem.GVSpacing

/*
 * GameVision Design System — RatingBadge (ver DESIGN.md §6).
 *
 * Píldora con el acento de ACCIÓN y su tinta: el único elemento "de color" de una
 * tarjeta de juego. La estrella es icono del sistema (Lucide), no el carácter "★":
 * un glifo de otra fuente en el mismo renglón desalinea la línea tipográfica.
 *
 * KDoc corregido el 02/10/2026: decía "fondo verde ácido", el color que el
 * re-anclaje (ADR-0010) retiró hace dos iteraciones. Un comentario que miente
 * sobre el sistema hace que el siguiente que lo lea "arregle" el componente para
 * que coincida con su documentación.
 */

@Composable
fun RatingBadge(
    rating: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(GVShapeFull)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = GVSpacing.sm, vertical = GVSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = rating,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = GVSpacing.xs)
        )
    }
}
