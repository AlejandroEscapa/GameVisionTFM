package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import com.composables.icons.lucide.ArrowLeft
import androidx.compose.material3.IconButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.User

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 12/02/2025
 * Descripción: 
 */

/**
 * Pantalla de edición de perfil.
 *
 * Esta función muestra la interfaz para editar el perfil del usuario,
 * incluyendo los campos de texto y un botón para guardar los cambios.
 *
 * @param isDarkTheme: Indica si el tema es oscuro.
 * @param paddingValues: Valores de relleno para el diseño.
 * @param navController: Controlador de navegación.
 * @param userViewModel: ViewModel para el usuario.
 */

@Composable
fun EditProfileScreen(
    paddingValues: PaddingValues,
    navController: NavController?,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel
) {
    // Campos de edición, precargados una vez desde el perfil del SSOT.
    val profile by userViewModel.profile.collectAsStateWithLifecycle()
    val loginFields by userViewModel.formFields.collectAsStateWithLifecycle()

    // Rellena el formulario con el perfil actual la primera vez que llega el dato.
    LaunchedEffect(profile) {
        if (profile.email.isNotBlank() && loginFields["email"].isNullOrBlank()) {
            userViewModel.updateFormField("nameSurname", profile.nameSurname)
            userViewModel.updateFormField("username", profile.username)
            userViewModel.updateFormField("description", profile.description)
            // D2.2: estado del interruptor de privacidad (default público).
            userViewModel.updateFormField("country", profile.country)
            userViewModel.updateFormField("email", profile.email)
        }
    }

    var isPrivate by remember { mutableStateOf(profile.isPrivate) }
    // Cabecera FIJA (iteración 02/10): la pantalla tenía un Spacer de 150 dp en
    // vez de un título; ahora tiene header unificado con vuelta a la izquierda.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        GVScreenHeader(
            title = "Editar perfil",
            modifier = Modifier.padding(horizontal = GVSpacing.screenPadding),
            leading = {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // Se utiliza un componente personalizado que agrupa los campos del perfil.
        // ---- F2 — Social: privacidad (D2.2) ----
        // El Top 4 (T2.5) y las listas curadas (T2.6) YA NO viven aquí: son
        // contenido del perfil, no edición de la cuenta. Se movieron a la
        // pantalla de perfil, con el buscador de catálogo y su sección propia
        // (revisión 02/10). Editar perfil se queda con lo que dice su nombre.
        PrivacyCard(isPrivate = isPrivate, onToggle = { isPrivate = it })
        ProfileCard(loginFields, userViewModel) { updatedFields ->
            // Al hacer clic en guardar, se lanza una corrutina para actualizar la información del usuario.
            // El perfil se guarda a través del SSOT y el flujo en vivo refleja el
            // cambio en el resto de pantallas; al terminar se vuelve atrás.
            userViewModel.updateProfile(updatedFields + ("isPrivate" to isPrivate)) {
                // Cambiar la privacidad MUEVE la lista del Top 4 de colección
                // (pública ↔ privada). La sincronización es silenciosa: no toca
                // el feed. Sin esto, la lista se quedaría en el lado antiguo.
                val uid = userViewModel.currentUid.value.orEmpty()
                if (uid.isNotBlank()) {
                    socialViewModel.syncTop4List(uid, profile.topGameIds, !isPrivate)
                }
                navController?.popBackStack()
            }
        }
        }
    }
}

/**
 * Tarjeta de perfil.
 *
 * Este componente encapsula la interfaz que contiene los campos de edición del perfil
 * y el botón para guardar los cambios.
 */
@Composable
fun ProfileCard(
    loginFields: Map<String, String>,
    userViewModel: UserViewModel,
    onSaveClick: (HashMap<String, String?>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Se itera sobre los campos del perfil para generar cada campo de texto.
            listOf(
                "nameSurname" to "Nombre completo",
                "username" to "Nombre de usuario",
                "description" to "Descripción",
                "country" to "País"
            ).forEach { (fieldKey, _) ->
                ProfileField(
                    fieldKey = fieldKey,
                    value = loginFields[fieldKey].orEmpty(),
                    userViewModel = userViewModel
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Botón para guardar los cambios realizados en el perfil.
            SaveButton {
                onSaveClick(
                    hashMapOf(
                        "nameSurname" to loginFields["nameSurname"],
                        "username" to loginFields["username"],
                        "description" to loginFields["description"],
                        "country" to loginFields["country"]
                    )
                )
            }
        }
    }
}

/**
 * Campo de texto para editar el perfil.
 *
 * Muestra un campo de texto personalizado con una etiqueta y un ícono
 * dependiendo del campo que se esté editando.
 */
@Composable
fun ProfileField(
    fieldKey: String,
    value: String,
    userViewModel: UserViewModel
) {
    // Mapa que relaciona cada campo con su etiqueta y su ícono correspondiente.
    val fieldData = mapOf(
        "nameSurname" to ("Introduce tu nombre completo" to Lucide.CircleUserRound),
        "username" to ("Introduce tu nombre de usuario" to Lucide.User),
        "description" to ("Añade una descripción" to Lucide.Info),
        "country" to ("Indica tu país" to Lucide.MapPin)
    )

    // Se obtienen la etiqueta y el ícono según el campo; se usa un valor por defecto si no se encuentra.
    val (labelText, icon) = fieldData[fieldKey] ?: ("" to Lucide.Pencil)

    OutlinedTextField(
        value = value,
        onValueChange = { userViewModel.onFormFieldChange(fieldKey, it) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        label = {
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Botón de guardado.
 *
 * Componente de botón que muestra la acción para guardar los cambios del perfil.
 */
@Composable
fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Guardar cambios", style = MaterialTheme.typography.labelLarge)
    }
}


/**
 * F2/D2.2 — Interruptor de cuenta privada. Default público; el cambio solo
 * se persiste al guardar el perfil (updateProfile incluye isPrivate).
 */
@Composable
private fun PrivacyCard(isPrivate: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cuenta privada", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Si la activas, solo tú verás tu biblioteca y tus estadísticas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(checked = isPrivate, onCheckedChange = onToggle)
        }
    }
}

/**
 * F2/T2.5 — El editor del Top 4 se movió al PERFIL (revisión 02/10):
 * `ui/views/composables/profile/TopGamesCard.kt`. Ahora tiene buscador de
 * catálogo (puedes elegir juegos que no están en tu biblioteca) y el slot guarda
 * su propia miniatura. Aquí solo quedaba una lista sin buscador ni portadas,
 * limitada a los 12 primeros juegos de la biblioteca.
 */

/**
 * Vista previa de la pantalla de edición de perfil.
 */
