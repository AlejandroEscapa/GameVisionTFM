package es.androidtfm.gamevision.ui.views.composables.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Plus
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVButton

/*
 * T2.6 (revisión 02/10) — Listas curadas: dónde se crean y cómo se ven.
 *
 * Antes la creación vivía en «Editar perfil» (un formulario más, entre los campos
 * de la cuenta) y el diálogo de la biblioteca mandaba allí. Ahora la creación es
 * una ACCIÓN del perfil, junto al Top 4, que es donde el usuario espera ver su
 * contenido. Dos superficies con el mismo diálogo:
 *  · Perfil  → [MisListasSection]: cabecera con acción + tus listas.
 *  · Para ti → [TarjetaCrearLista]: entrada ligera (una línea, sin formulario).
 */

/**
 * Sección «Mis listas» del perfil propio.
 *
 * @param listas listas del usuario (el Top 4 se filtra: ya tiene su cuadro).
 * @param onCrear abre el diálogo de creación.
 * @param onAbrirBiblioteca acción de la lista vacía (añadir juegos).
 */
@Composable
fun MisListasSection(
    listas: List<GameList>,
    onCrear: () -> Unit,
    onAbrirBiblioteca: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El Top 4 es una lista REAL (documento `top4`), pero ya tiene su propio
    // cuadro arriba: repetirlo aquí sería contar lo mismo dos veces.
    val visibles = listas.filter { it.id != GameList.TOP4_ID }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mis listas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (visibles.isEmpty()) {
                        "Agrupa juegos con un criterio tuyo."
                    } else {
                        "${visibles.size} ${if (visibles.size == 1) "lista" else "listas"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onCrear) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(GVSpacing.xs))
                Text("Nueva lista")
            }
        }

        if (visibles.isEmpty()) {
            Spacer(Modifier.height(GVSpacing.sm))
            TarjetaListaVacia(onCrear = onCrear, onAbrirBiblioteca = onAbrirBiblioteca)
        } else {
            Spacer(Modifier.height(GVSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(GVSpacing.sm)) {
                visibles.forEach { lista -> TarjetaLista(lista) }
            }
        }
    }
}

/** Tarjeta de una lista propia: nombre, descripción, privacidad y nº de juegos. */
@Composable
private fun TarjetaLista(lista: GameList) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = GVShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(GVSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lista.name.ifBlank { "Lista" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (lista.description.isNotBlank()) {
                    Text(
                        text = lista.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${lista.gameIds.size} " +
                        if (lista.gameIds.size == 1) "juego" else "juegos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!lista.isPublic) {
                Icon(
                    imageVector = Lucide.Lock,
                    contentDescription = "Lista privada",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** Estado vacío con la acción principal: crear la primera lista. */
@Composable
private fun TarjetaListaVacia(onCrear: () -> Unit, onAbrirBiblioteca: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = GVShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(GVSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Todavía no tienes listas",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(GVSpacing.xs))
            Text(
                text = "«Saga que quiero terminar», «Para jugar con amigos»…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(GVSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(GVSpacing.sm)) {
                GVButton(text = "Crear lista", onClick = onCrear)
                GVButton(
                    text = "Mi biblioteca",
                    onClick = onAbrirBiblioteca,
                    secondary = true
                )
            }
        }
    }
}

/**
 * Entrada LIGERA para «Para ti»: una sola línea con acción.
 *
 * No repite el formulario: al tocar abre el mismo diálogo que el perfil. La idea
 * es que crear una lista esté a la vista sin ocupar media pantalla en la Home.
 */
@Composable
fun TarjetaCrearLista(
    onCrear: () -> Unit,
    modifier: Modifier = Modifier,
    listasCount: Int = 0
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCrear),
        shape = GVShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(GVSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(GVShapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(GVSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Crea una lista",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (listasCount == 0) {
                        "Sagas, pendientes, joyas ocultas: con nombre y tuyas."
                    } else {
                        "Tienes $listasCount. Añade otra cuando quieras."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Diálogo de creación de lista. ÚNICO para perfil y Home: misma decisión
 * (nombre, descripción, pública o privada) y un solo sitio que mantener.
 *
 * @param onCrear recibe (nombre, descripción, pública).
 */
@Composable
fun CrearListaDialog(
    onDismiss: () -> Unit,
    onCrear: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var listPublic by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = GVShapes.large,
        title = { Text("Nueva lista", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la lista") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = GVShapes.medium
                )
                Spacer(Modifier.height(GVSpacing.sm))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = GVShapes.medium
                )
                Spacer(Modifier.height(GVSpacing.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = listPublic, onCheckedChange = { listPublic = it })
                    Spacer(Modifier.width(GVSpacing.sm))
                    Column {
                        Text(
                            text = if (listPublic) "Pública" else "Privada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (listPublic) {
                                "La verán en tu perfil"
                            } else {
                                "Solo la ves tú"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCrear(name, description, listPublic) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/** Fila de lista en modo LECTURA (perfil de otro): sin acciones. */
@Composable
fun ListaPublicaCard(lista: GameList, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = GVShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(GVSpacing.md)) {
            Text(lista.name.ifBlank { "Lista" }, style = MaterialTheme.typography.titleSmall)
            if (lista.description.isNotBlank()) {
                Text(
                    lista.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(GVSpacing.xs))
            Text(
                "${lista.gameIds.size} juegos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
