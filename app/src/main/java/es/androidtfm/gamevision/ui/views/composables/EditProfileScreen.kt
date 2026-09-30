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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

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
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    ddbbViewModel: DDBBViewModel
) {
    // Campos de edición, precargados una vez desde el perfil del SSOT.
    val profile by userViewModel.profile.collectAsState()
    val loginFields by userViewModel.formFields.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
    // El uid también es estado composable: leer .value directo en composición dispara lint.
    val currentUid by userViewModel.currentUid.collectAsState()
    val library by ddbbViewModel.observeLibrary(currentUid.orEmpty())
        .collectAsState(initial = null)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(150.dp))
        // Se utiliza un componente personalizado que agrupa los campos del perfil.
        // ---- F2 — Social: privacidad (D2.2), Top 4 (T2.5), listas (T2.6) ----
        PrivacyCard(isPrivate = isPrivate, onToggle = { isPrivate = it })
        TopGamesEditor(
            selectedIds = profile.topGameIds,
            library = library?.getOrNull(),
            onPersist = { ids -> userViewModel.updateProfile(mapOf("topGameIds" to ids)) }
        )
        CreateListCard(
            onCreate = { name, description, listPublic ->
                val currentUid = userViewModel.currentUid.value.orEmpty()
                if (currentUid.isNotBlank() && name.isNotBlank()) {
                    socialViewModel.saveList(
                        currentUid,
                        GameList(name = name.trim(), description = description.trim(), isPublic = listPublic)
                    ) { result ->
                        if (result.isSuccess) socialViewModel.loadLists(currentUid)
                    }
                }
            },
            onOpenLibrary = { navController?.navigate("gamelist") }
        )
        ProfileCard(loginFields, userViewModel) { updatedFields ->
            // Al hacer clic en guardar, se lanza una corrutina para actualizar la información del usuario.
            // El perfil se guarda a través del SSOT y el flujo en vivo refleja el
            // cambio en el resto de pantallas; al terminar se vuelve atrás.
            userViewModel.updateProfile(updatedFields + ("isPrivate" to isPrivate)) {
                navController?.popBackStack()
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
        "nameSurname" to ("Introduce tu nombre completo" to Icons.Outlined.AccountCircle),
        "username" to ("Introduce tu nombre de usuario" to Icons.Outlined.Person),
        "description" to ("Añade una descripción" to Icons.Outlined.Info),
        "country" to ("Indica tu país" to Icons.Outlined.LocationOn)
    )

    // Se obtienen la etiqueta y el ícono según el campo; se usa un valor por defecto si no se encuentra.
    val (labelText, icon) = fieldData[fieldKey] ?: ("" to Icons.Outlined.Edit)

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
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
 * F2/T2.5 — Editor del Top 4: hasta 4 juegos de tu biblioteca, orden según
 * selección. Persistencia inmediata (updateProfile topGameIds).
 */
@Composable
private fun TopGamesEditor(
    selectedIds: List<String>,
    library: List<LibraryEntry>?,
    onPersist: (List<String>) -> Unit
) {
    val entries = library.orEmpty()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tu Top 4", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Elige hasta 4 juegos de tu biblioteca: serán tu carta de presentación.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (entries.isEmpty()) {
                Text(
                    "Añade juegos a tu biblioteca para elegir tu Top.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                entries.take(12).forEach { entry ->
                    val selected = entry.gameId in selectedIds
                    val enabled = selected || selectedIds.size < 4
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled) {
                                val next = if (selected) selectedIds - entry.gameId
                                else (selectedIds + entry.gameId).take(4)
                                onPersist(next)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selected) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            entry.name.ifBlank { entry.gameId },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * F2/T2.6 — Creación rápida de listas curadas (públicas o privadas).
 */
@Composable
private fun CreateListCard(
    onCreate: (String, String, Boolean) -> Unit,
    onOpenLibrary: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var listPublic by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Crear una lista", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la lista") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = listPublic, onCheckedChange = { listPublic = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (listPublic) "Pública" else "Privada", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCreate(name, description, listPublic); name = ""; description = "" }, enabled = name.isNotBlank()) {
                    Text("Crear")
                }
                OutlinedButton(onClick = onOpenLibrary) { Text("Ir a mi biblioteca") }
            }
        }
    }
}
/**
 * Vista previa de la pantalla de edición de perfil.
 */
