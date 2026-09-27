package es.androidtfm.gamevision.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Preferencias persistentes de sesión.
 *
 * El modo invitado se guarda para que un invitado no sea expulsado al login al
 * reiniciar la app (antes vivía solo en memoria).
 */

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sessionSettings")

class SessionPreferences(context: Context) {

    private val dataStore = context.sessionDataStore
    private val guestKey = booleanPreferencesKey("guest")

    val isGuest: Flow<Boolean> = dataStore.data.map { it[guestKey] ?: false }

    suspend fun setGuest(isGuest: Boolean) {
        dataStore.edit { it[guestKey] = isGuest }
    }
}
