package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * GameVision Design System — formas (ver DESIGN.md §4).
 *
 * Elevación por tono de superficie, nunca por sombra. La portada de juego manda
 * (16 dp, sin borde, sin elevación).
 */

val GVShapes = Shapes(
    small = RoundedCornerShape(8.dp),      // chips, badges, inputs
    medium = RoundedCornerShape(16.dp),    // tarjetas, portadas
    large = RoundedCornerShape(24.dp)      // sheets, diálogos, hero cards
)

val GVShapeFull = RoundedCornerShape(percent = 50) // avatares, pills, botones redondos
