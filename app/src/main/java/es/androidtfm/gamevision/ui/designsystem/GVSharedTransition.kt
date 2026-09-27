package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/*
 * GameVision Design System — shared elements (ver DESIGN.md §5).
 *
 * Infraestructura para las transiciones de elemento compartido (la portada de
 * un juego "vuela" desde la tarjeta hasta la cabecera del detalle). Los scopes
 * viajan por CompositionLocals para no ensuciar las firmas de los composables;
 * el helper Modifier.gvSharedElement es un no-op si no hay scopes (p. ej. en
 * previews), de modo que los componentes del sistema se usan igual en todas
 * partes.
 */

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Envuelve un destino de navegación para habilitar shared elements en su contenido.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GVSharedElementProvider(
    animatedVisibilityScope: AnimatedVisibilityScope,
    content: @Composable () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    CompositionLocalProvider(
        LocalNavAnimatedVisibilityScope provides animatedVisibilityScope,
        LocalSharedTransitionScope provides sharedTransitionScope,
        content = content
    )
}

/**
 * Marca un elemento como compartido entre pantallas (clave estable, p. ej.
 * "cover-12345"). Si faltan los scopes (preview, sin transición activa) es no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.gvSharedElement(key: String): Modifier = composed {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@composed.sharedElement(
                rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else {
        Modifier
    }
}
