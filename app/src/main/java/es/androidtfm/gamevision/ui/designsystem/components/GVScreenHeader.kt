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

/*
 * GameVision Design System — cabecera de pantalla (unidad de criterio F3).
 *
 * TODAS las pantallas con título usan este componente: misma tipografía
 * (headlineLarge, tinta sobre superficie), misma posición (arriba-izquierda)
 * y mismos paddings verticales. El padding horizontal lo aporta el contenedor
 * de cada pantalla (16 dp en toda la app). Las acciones (iconos) van a la
 * derecha, alineadas al centro.
 */
@Composable
fun GVScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}
