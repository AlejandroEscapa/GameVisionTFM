package es.androidtfm.gamevision.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Implementación de `AnalyticsLogger` sobre Firebase Analytics (F1 — Bloque 4).
 *
 * Firebase Analytics ya estaba en el proyecto; aquí solo se centraliza su uso
 * para que el resto del código no lo toque y para poder falsearlo en tests.
 */

@Singleton
class FirebaseAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsLogger {

    override fun log(event: String, params: Map<String, Any?>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is String -> putString(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putLong(key, if (value) 1L else 0L)
                    else -> putString(key, value.toString())
                }
            }
        }
        analytics.logEvent(event, bundle)
    }

    override fun setUserId(uid: String?) {
        analytics.setUserId(uid)
    }

    override fun logScreen(screenName: String) {
        log("screen_view", mapOf("screen_name" to screenName))
    }

    companion object {
        private const val PARAM_SCREEN = "screen_name"
    }
}
