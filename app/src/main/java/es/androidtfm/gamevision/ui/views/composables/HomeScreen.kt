package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sun
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVButton

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: portada de entrada (sin sesión).
 *
 * Re-anclada al design system (iteración 02/10, segunda vuelta). Esta pantalla
 * es el `startDestination` del NavHost, o sea LA PRIMERA IMPRESIÓN del producto,
 * y era la más anticuada de la app:
 *
 *  · Un `delay(1000)` que pintaba esqueletos grises durante un segundo entero
 *    antes de enseñar nada (una lista falsa como bienvenida).
 *  · `isDarkTheme!!` sobre un parámetro nullable: si el tema aún no había
 *    resuelto, la primera pantalla del producto crasheaba.
 *  · Gradiente de fondo + botones de esquina 5 dp + `FontWeight.Black` (900) con
 *    el texto en azul: el look de 2021 que el ADR-0010 retiró.
 *  · Dos botones PRIMARIOS rellenos: "Iniciar sesión" y "Continuar como
 *    invitado" pesaban igual, así que la pantalla no decía cuál es el camino.
 *
 * Ahora: sin retardo ni esqueletos falsos, jerarquía clara (una sola acción
 * primaria), alternancia de tema en el icono de siempre y aire de la escala.
 */

@Composable
fun HomeScreen(
    isDarkTheme: Boolean?,
    onThemeChange: (Boolean) -> Unit,
    navController: NavHostController,
    isGuest: Boolean,
    onGuestStatusChange: (Boolean) -> Unit
) {
    // El tema llega nullable desde el DataStore. Antes reventaba con `!!`; ahora
    // se asume el del sistema hasta que el valor real llegue (que es lo que ve
    // el usuario mientras tanto).
    val oscuro = isDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = GVSpacing.screenPadding)
    ) {
        // Cambio de tema: esquina superior derecha, discreto. El icono dice el
        // estado al que se CAMBIA, y el icono es del sistema (Lucide), no un
        // drawable antiguo.
        IconButton(
            onClick = { onThemeChange(!oscuro) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = if (oscuro) Lucide.Sun else Lucide.Moon,
                contentDescription = if (oscuro) "Modo claro" else "Modo noche"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // La marca manda: es el único elemento con peso visual propio.
            Image(
                painter = painterResource(
                    id = if (oscuro) R.drawable.gamevisionnight else R.drawable.gamevision2
                ),
                contentDescription = "GameVision",
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .widthIn(max = 320.dp)
            )
            Spacer(Modifier.height(GVSpacing.lg))
            Text(
                text = "Tu biblioteca, tu tiempo y la gente con la que juegas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = GVSpacing.sm)
            )

            Spacer(Modifier.height(GVSpacing.xxl))

            // UNA acción primaria: entrar. La píldora es del sistema (GVButton) y
            // ocupa el ancho de la columna, no toda la pantalla.
            GVButton(
                text = "Iniciar sesión",
                onClick = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(GVSpacing.md))

            // El invitado es la salida secundaria: existe, se lee, pero no compite
            // con la conversión. Antes era un segundo botón relleno idéntico; ahora
            // ni siquiera lleva el acento, que es de la acción principal.
            TextButton(
                onClick = {
                    onGuestStatusChange(true)
                    navController.navigate("news")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Explorar sin cuenta",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isGuest) {
                Spacer(Modifier.height(GVSpacing.sm))
                Text(
                    text = "Estás explorando como invitado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(
        isDarkTheme = false,
        onThemeChange = { },
        navController = navController,
        isGuest = false,
        onGuestStatusChange = { }
    )
}
