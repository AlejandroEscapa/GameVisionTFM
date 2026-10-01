package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.androidtfm.gamevision.ui.designsystem.GVSpacing

/*
 * GameVision Design System — cabecera de pantalla (unificada, iteración 02/10).
 *
 * El título es TEXTO SIMPLE: sin tarjetas, sin fondos, sin estilos propios.
 * Toda pantalla con título usa este componente; su comentario anterior decía
 * "lo usan todas" cuando la mitad lo pintaba a mano — ahora es cierto.
 *
 * Reglas que fija:
 *  · Tipografía: headlineLarge, tinta sobre superficie. Nada de copias.
 *  · Margen horizontal: lo aporta el CONTENEDOR (GVSpacing.screenPadding = 16);
 *    título y primer elemento comparten línea.
 *  · Aire bajo el título: GVSpacing.headerGap (20) — el título no se pega al
 *    contenido.
 *  · Cabecera FIJA: se coloca FUERA del contenedor con scroll; solo se
 *    desplaza el contenido.
 *  · `leading` (opcional): acciones a la IZQUIERDA — la vuelta ("Volver")
 *    vive ahí, no en la zona de la derecha.
 */
@Composable
fun GVScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    /** Acciones a la izquierda de la pantalla (p. ej. botón "Volver"). */
    leading: @Composable RowScope.() -> Unit = {},
    /** Acciones a la derecha de la pantalla (iconos, menús). */
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = GVSpacing.headerGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, content = leading)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}
