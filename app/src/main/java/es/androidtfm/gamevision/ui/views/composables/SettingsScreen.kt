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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Trash2
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Ajustes v1 (iteración 02/10, D-E1/E2): modo noche real, privacidad movida
 * aquí y el esqueleto de secciones con «Próximamente» donde aún no hay
 * funcionalidad. Vive en la cabecera del Perfil, no en el dock (D-E2).
 */
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val profile by userViewModel.profile.collectAsStateWithLifecycle()

    // Cabecera FIJA (iteración 02/10): el título no se desplaza.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GVScreenHeader(
            title = "Ajustes",
            modifier = Modifier.padding(horizontal = GVSpacing.screenPadding),
            // La vuelta vive a la IZQUIERDA (regla de GVScreenHeader, iteración 02/10)
            leading = {
                IconButton(onClick = onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

        Seccion("Apariencia")
        FilaAjuste(
            icono = Lucide.Moon,
            titulo = "Modo noche",
            descripcion = "Oscuro suave derivado de los tiles",
            control = {
                Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
            }
        )

        Seccion("Privacidad")
        FilaAjuste(
            icono = Lucide.Lock,
            titulo = "Perfil privado",
            descripcion = "Solo tus seguidores ven tu actividad",
            control = {
                Switch(
                    checked = profile?.isPrivate == true,
                    onCheckedChange = { privado ->
                        userViewModel.updateProfile(mapOf("isPrivate" to privado)) { }
                    }
                )
            }
        )

        Seccion("Notificaciones")
        PlaceholderAjuste(Lucide.Bell, "Hitos de quienes sigues")
        PlaceholderAjuste(Lucide.Download, "Ofertas de tu wishlist")

        Seccion("Integraciones")
        PlaceholderAjuste(Lucide.Gamepad2, "Vincular Steam")

        Seccion("Datos")
        PlaceholderAjuste(Lucide.Download, "Exportar mi biblioteca")
        PlaceholderAjuste(Lucide.CloudOff, "Imágenes solo con Wi-Fi")

        Seccion("Cuenta")
        PlaceholderAjuste(Lucide.Trash2, "Borrar cuenta")

        Seccion("Acerca de")
        PlaceholderAjuste(Lucide.Info, "Versión 2.0.0 · Licencias")

        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Seccion(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

/** Fila real de ajuste: control a la derecha. */
@Composable
private fun FilaAjuste(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        control()
    }
}

/** Placeholder honesto: «Próximamente», sin funcionalidad falsa (D-E1). */
@Composable
private fun PlaceholderAjuste(icono: ImageVector, titulo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "Próximamente",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
