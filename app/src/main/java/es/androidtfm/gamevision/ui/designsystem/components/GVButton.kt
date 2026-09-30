package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.androidtfm.gamevision.ui.designsystem.GVShapeFull
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GVButton y GVChip (re-anclado, ADR-0010).
 *
 * Primary: píldora Action Blue con texto blanco (claro) / Sky Blue con tinta
 * oscura (oscuro). Secondary: píldora outline.
 * GVChip: outline; seleccionado relleno del acento.
 */

@Composable
fun GVButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    enabled: Boolean = true
) {
    if (secondary) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(48.dp),
            shape = GVShapeFull,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(48.dp),
            shape = GVShapeFull,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun GVChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = GVShapeFull,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        label = { Text(text, style = MaterialTheme.typography.labelLarge) }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun GVButtonPreview() {
    GameVisionTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            GVButton("JUGAR", onClick = {})
            GVButton("GUARDAR", onClick = {}, secondary = true)
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                GVChip("Acción", selected = true, onClick = {})
                GVChip("RPG", selected = false, onClick = {})
            }
        }
    }
}
