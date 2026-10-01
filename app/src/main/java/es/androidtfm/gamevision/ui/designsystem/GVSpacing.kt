package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.ui.unit.dp

/*
 * GameVision Design System — escala de espaciado (iteración 02/10).
 *
 * Antes de esta escala NO existía: convivían 4/8/10/12/16/18/20/24 dp como
 * margen horizontal de pantalla, mezclados dentro de una misma pantalla. A
 * partir de aquí, el espaciado solo usa estos tokens — ningún dp suelto en
 * márgenes de pantalla o cabeceras.
 *
 *  · screenPadding: margen horizontal de TODA pantalla (16). Los contenidos
 *    se alinean con el título de la cabecera.
 *  · headerGap: aire entre el título de la cabecera y el primer elemento
 *    (20). El título respira y no se pega al contenido.
 *  · sm/md: separaciones dentro de componentes (8/12).
 *  · xl/xxl: separación entre secciones (24/32).
 */
object GVSpacing {
    /** Margen horizontal de pantalla: título y contenido comparten línea. */
    val screenPadding = 16.dp

    /** Aire entre el título de la cabecera y el primer elemento de debajo. */
    val headerGap = 20.dp

    /** Separación mínima dentro de un componente (icono↔texto, chips). */
    val xs = 4.dp
    val sm = 8.dp

    /** Separación entre elementos de contenido (tarjetas, filas). */
    val md = 12.dp

    /** Margen horizontal de pantalla. */
    val lg = 16.dp

    /** Separación entre secciones de una pantalla. */
    val xl = 24.dp
    val xxl = 32.dp
}
