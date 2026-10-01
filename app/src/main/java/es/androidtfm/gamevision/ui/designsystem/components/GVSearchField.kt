package es.androidtfm.gamevision.ui.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import es.androidtfm.gamevision.ui.designsystem.GVShapeFull
import es.androidtfm.gamevision.ui.designsystem.GameVisionTheme

/*
 * GameVision Design System — GVSearchField (iteración 02/10, segunda vuelta).
 *
 * El sistema tenía TRES buscadores distintos: la barra de la pantalla Buscar
 * (TextField relleno con sombra de 4 dp y esquinas de 24 — Material 2 puro), el
 * campo del Top 4 (píldora con borde) y el de Social. La misma acción resuelta de
 * tres formas es exactamente la falta de cohesión que rompe un design system.
 *
 * Aquí queda UNA: píldora (la forma reservada a la ACCIÓN), borde hairline en
 * reposo y acento al enfocar, lupa a la izquierda y, a la derecha, lo que toque:
 * progreso mientras busca, ✕ para limpiar o el disparador manual.
 *
 * La búsqueda EN VIVO es responsabilidad de quien lo usa: este componente solo
 * pinta el campo.
 */

@Composable
fun GVSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar",
    /** true mientras hay una consulta en vuelo: sustituye la acción por progreso. */
    buscando: Boolean = false,
    /**
     * Acción explícita (IME o botón). Con búsqueda en vivo es opcional: sirve para
     * forzarla sin esperar al debounce o para cerrar el teclado.
     */
    onSearch: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = GVShapeFull,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Icon(
                imageVector = Lucide.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            when {
                buscando -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                value.isNotBlank() -> IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Borrar búsqueda",
                        modifier = Modifier.size(18.dp)
                    )
                }
                onSearch != null -> IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = "Buscar ahora",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GVSearchFieldPreview() {
    GameVisionTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GVSearchField(value = "", onValueChange = {}, placeholder = "Buscar juegos...")
            GVSearchField(value = "dark souls", onValueChange = {}, buscando = true)
        }
    }
}
