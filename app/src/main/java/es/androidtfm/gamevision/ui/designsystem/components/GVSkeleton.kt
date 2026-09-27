package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GVSkeleton (ver DESIGN.md §6).
 *
 * Bloque con shimmer que replica la forma del contenido que carga.
 * PROHIBIDO el spinner circular genérico: las cargas se muestran con skeletons.
 */

@Composable
fun GVSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = GVShapes.medium,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp
) {
    val transition = rememberInfiniteTransition(label = "gv-shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing)
        ),
        label = "gv-shimmer-progress"
    )
    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = androidx.compose.ui.geometry.Offset(
                        x = shimmerProgress * 800f - 400f,
                        y = 0f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = shimmerProgress * 800f,
                        y = 40f
                    )
                )
            )
    )
}

/**
 * Skeleton de fila de juego: portada + dos líneas de texto (usar en listas).
 */
@Composable
fun GameRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GVSkeleton(shape = GVShapes.medium, width = 64.dp, height = 64.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            GVSkeleton(Modifier.fillMaxWidth(), height = 14.dp)
            GVSkeleton(Modifier.fillMaxWidth(0.5f), height = 12.dp)
        }
    }
}

/**
 * Skeleton de grid de portadas.
 */
@Composable
fun GameGridSkeleton(modifier: Modifier = Modifier, items: Int = 6) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        repeat(items / 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GVSkeleton(
                    Modifier.weight(1f),
                    shape = GVShapes.medium,
                    height = 180.dp
                )
                GVSkeleton(
                    Modifier.weight(1f),
                    shape = GVShapes.medium,
                    height = 180.dp
                )
            }
            Box(Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun GVSkeletonPreview() {
    GameVisionTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GVSkeleton(width = 200.dp, height = 200.dp)
            GameRowSkeleton()
        }
    }
}
