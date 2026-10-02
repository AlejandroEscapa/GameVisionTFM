package es.androidtfm.gamevision.ui.designsystem

import android.animation.ValueAnimator
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/*
 * GameVision Design System — motion (ver DESIGN.md §5).
 *
 * Física de muelles, nunca tweens lineales. Entra con fade+slide 16 dp; máximo
 * 3 elementos animando a la vez.
 */

object GVMotion {
    const val DURATION_FAST = 150
    const val DURATION_NORMAL = 300
    const val DURATION_SLOW = 500
    const val STAGGER_INCREMENT_MS = 40L

    // Entrada estándar (fade + slide suave, emphasized decelerate)
    val EnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    // Salida (emphasized accelerate)
    val ExitEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    // Muelle posicional por defecto
    fun <T> springStandard() = spring<T>(
        dampingRatio = 0.9f,
        stiffness = 380f
    )

    // Muelle juguetón para micro-interacciones (badges, morph)
    fun <T> springBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 600f
    )
}

/**
 * true cuando el usuario pide reducir movimiento en el sistema: las
 * transiciones y staggered entries se sustituyen por fades directos.
 *
 * **Ya está conectado** (ADR-0013 §5.2): `MainActivity` lee la preferencia real
 * del sistema con [rememberSystemReduceMotion] y la provee aquí, así que
 * cualquier pantalla puede consultarla con `LocalReduceMotion.current`.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * Lee la preferencia de «reducir movimiento» del sistema.
 *
 * Se apoya en `ValueAnimator.areAnimatorsEnabled()` (API de plataforma, la misma
 * que respeta Compose por debajo) en lugar de un `CompositionLocal` de Compose,
 * porque `LocalMotionDurationScale` no existe en la versión de Compose de este
 * proyecto. Se vuelve a leer en cada `ON_RESUME`, que es cuando el usuario puede
 * haber cambiado el ajuste en Ajustes del sistema.
 */
@Composable
fun rememberSystemReduceMotion(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var animatorsEnabled by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                animatorsEnabled = ValueAnimator.areAnimatorsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return !animatorsEnabled
}
