package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

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
 */
val LocalReduceMotion = staticCompositionLocalOf { false }
