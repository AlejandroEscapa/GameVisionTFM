package es.androidtfm.gamevision.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.androidtfm.gamevision.datastore.ThemeDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 16/01/2025
 * Descripción: 
 */

/**
 * ViewModel para gestionar el tema de la aplicación.
 *
 * Se encarga de manejar el estado del tema (claro/oscuro) y proporcionar métodos para alternarlo.
 */

class ThemeViewModel(
    application: Application,
    // Inyectable para pruebas; por defecto usa el DataStore de la aplicación
    val themeDataStore: ThemeDataStore = ThemeDataStore(application),
    // Scope inyectable para pruebas; por defecto usa el viewModelScope
    externalScope: CoroutineScope? = null
) : AndroidViewModel(application) {

    private val scope: CoroutineScope = externalScope ?: viewModelScope

    // Exponemos el estado del tema como un `StateFlow`
    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme.stateIn(
        scope, // Scope del ViewModel (o el inyectado en pruebas)
        SharingStarted.Lazily, // Inicia la recolección de datos cuando hay al menos un observador
        false // Valor inicial (por defecto modo claro)
    )

    /**
     * Función para alternar el tema entre claro y oscuro.
     */
    fun toggleTheme() {
        scope.launch {
            // Obtiene el estado actual del tema
            val currentTheme = isDarkTheme.value
            // Cambia el tema al estado opuesto
            themeDataStore.swapTheme(!currentTheme)
        }
    }
}