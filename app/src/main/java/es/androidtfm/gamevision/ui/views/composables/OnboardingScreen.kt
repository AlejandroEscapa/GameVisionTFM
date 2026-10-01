package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.ui.designsystem.components.GVChip

/*
 * Onboarding post-registro (iteración 02/10, bloque C / D-C1).
 *
 * Tres pasos SALABLES y nunca bloqueantes (línea de D1.2): biografía,
 * localización y géneros favoritos. Los géneros se guardan como array en
 * `users/{uid}` y siembran el motor de recomendación y el descubrimiento —
 * por eso el paso existe: el onboarding deja de ser decorativo.
 *
 * Se muestra solo si el perfil no tiene `onboardingDone`. Al terminar o
 * saltar, se marca en el perfil (Firestore), así no se repite en ningún
 * dispositivo.
 */

/** Géneros ofrecidos (valores RAWG, en inglés como llegan del catálogo). */
private val GENEROS_ONBOARDING = listOf(
    "Action", "Adventure", "RPG", "Shooter", "Strategy", "Indie",
    "Puzzle", "Simulation", "Sports", "Racing", "Platformer", "Casual"
)

@Composable
fun OnboardingScreen(
    onFinish: (description: String, country: String, genres: List<String>) -> Unit
) {
    var paso by rememberSaveable { mutableStateOf(0) }
    var bio by rememberSaveable { mutableStateOf("") }
    var pais by rememberSaveable { mutableStateOf("") }
    var seleccionados by rememberSaveable { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        // Progreso: 3 puntos, el activo en el acento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == paso) 10.dp else 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i == paso) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        when (paso) {
            0 -> {
                Text(
                    "Cuéntanos quién eres",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Una biografía breve hace que tu perfil diga algo. Puedes cambiarla cuando quieras.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 280) bio = it },
                    label = { Text("Biografía") },
                    placeholder = { Text("Qué te gusta jugar, a qué dedicas el tiempo…") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text(
                    "${bio.length} / 280",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            1 -> {
                Text(
                    "¿Desde dónde juegas?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ayuda a que otros jugadores te sitúen. Es opcional.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = pais,
                    onValueChange = { pais = it },
                    label = { Text("Localización") },
                    placeholder = { Text("Madrid, España") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> {
                Text(
                    "¿Qué te gusta jugar?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Elige tus géneros y te recomendaremos juegos que encajen desde el primer día.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                // Chips en filas de 3 (contenido corto: no hace falta el flow layout)
                GENEROS_ONBOARDING.chunked(3).forEach { fila ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { genero ->
                            GVChip(
                                text = genero,
                                selected = genero in seleccionados,
                                onClick = {
                                    seleccionados = if (genero in seleccionados) {
                                        seleccionados - genero
                                    } else {
                                        seleccionados + genero
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        GVButton(
            text = if (paso < 2) "Continuar" else "Empezar",
            onClick = {
                if (paso < 2) paso++ else onFinish(bio, pais, seleccionados)
            },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = {
                // Saltar: se guarda lo elegido hasta ahora y se marca el onboarding.
                onFinish(bio, pais, seleccionados)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Saltar por ahora")
        }
    }
}
