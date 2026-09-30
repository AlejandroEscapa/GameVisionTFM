package es.androidtfm.gamevision.ui.designsystem.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/*
 * GameVision Design System — conectividad (F2/Bloque 1.3).
 *
 * Estado de red real del dispositivo para banners de modo degradado
 * (ver DESIGN.md §6). La plataforma no ofrece observación declarativa:
 * ConnectivityManager + NetworkCallback es la forma canónica de saber si
 * hay transporte con INTERNET validado, sin ping ni heurísticas.
 */

/**
 * Observa la conectividad del dispositivo y la expone como estado Compose.
 *
 * @return `true` mientras exista una red con capacidad validada de INTERNET;
 * `false` en modo avión o sin transporte usable. Empieza con una lectura
 * síncrona para no parpadear el banner en el primer frame.
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val isOnline = remember { mutableStateOf(readIsOnline(context)) }

    DisposableEffect(context) {
        val connectivityManager =
            ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            // Ni onAvailable ni onLost implican internet usable: onAvailable dispara
            // al asociarse el transporte (puede carecer de salida real), así que
            // re-lee VALIDATED en lugar de fiarse del evento.
            override fun onAvailable(network: Network) { isOnline.value = readIsOnline(context) }
            override fun onLost(network: Network) { isOnline.value = readIsOnline(context) }
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                isOnline.value =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        }
        val current = isOnline.value
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                callback
            )
            // Cubre la carrera del onCapabilitiesChanged inicial: si la lectura
            // síncrona decía online y el callback aún no ha refrescado, re-lee.
            if (current) isOnline.value = readIsOnline(context)
        }
        onDispose {
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }

    return isOnline
}

/** Lectura síncrona: ¿hay algún transporte con INTERNET validado? */
private fun readIsOnline(context: Context): Boolean {
    val connectivityManager =
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
