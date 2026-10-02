package es.androidtfm.gamevision.data.notifications

import java.time.LocalDate

/*
 * Decisión del aviso de fin de año (F3/T3.11, decisión D3.7).
 *
 * Pura a propósito: es la única parte del aviso que se puede testear en JVM sin
 * Android, y es justo donde está el riesgo de producto. Las tres reglas salen de
 * decisiones ya cerradas:
 *
 *  - **D3.6 (celebrar, nunca culpabilizar):** sin actividad no se avisa. Un «tu
 *    Rewind está listo» a quien no jugó es exactamente el mensaje tóxico que la
 *    investigación desaconseja.
 *  - **El Rewind del año que cierra solo tiene sentido cuando el año ha cerrado:**
 *    se avisa del año anterior, nunca del que está en curso (en diciembre el
 *    resumen estaría a medias).
 *  - **Una sola vez por año:** se persiste el último año avisado, así que si el
 *    móvil estuvo apagado el 1 de enero, el aviso sale igual el 3 (el trabajo es
 *    diario) sin repetirse después.
 */
object RewindAviso {

    /** El año que recapituclar: el que acaba de cerrar. */
    fun anioQueCierra(hoy: LocalDate): Int = hoy.year - 1

    /**
     * ¿Hay que avisar hoy?
     *
     * @param minutosDelAnioQueCierra minutos jugados en el año que acaba de cerrar.
     * @param ultimoAnioAvisado último año del que ya se avisó (null si nunca).
     */
    fun debeAvisar(
        minutosDelAnioQueCierra: Int,
        ultimoAnioAvisado: Int?,
        hoy: LocalDate
    ): Boolean {
        if (minutosDelAnioQueCierra <= 0) return false
        val anio = anioQueCierra(hoy)
        return ultimoAnioAvisado == null || ultimoAnioAvisado < anio
    }
}
