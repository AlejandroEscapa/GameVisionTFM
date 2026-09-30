package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * GameVision Design System — formas (re-ancladas, ADR-0010).
 *
 * Escala del documento: 8 (utilidad compacta), 11 (perla), 18 (tarjetas de
 * utilidad). La píldora (9999) queda reservada para la ACCIÓN: CTA primario,
 * chips de opción y búsqueda — la firma del sistema. Los tiles full-bleed son
 * rectangulares (0), pero eso lo decide cada pantalla, no la escala global.
 */

val GVShapes = Shapes(
    small = RoundedCornerShape(8.dp),       // utilidad compacta, badges
    medium = RoundedCornerShape(11.dp),     // botones perla, imágenes inline
    large = RoundedCornerShape(18.dp)       // tarjetas de utilidad, sheets
)

val GVShapeFull = RoundedCornerShape(percent = 50) // píldora: CTA, chips, avatares
