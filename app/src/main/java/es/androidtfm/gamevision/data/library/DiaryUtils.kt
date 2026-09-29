package es.androidtfm.gamevision.data.library

import java.util.Calendar
import java.util.Locale

/*
 * Lógica pura del diario (F1 — Bloque 2, T1.8/T1.10).
 *
 * Agrupa sesiones por mes, calcula totales y formatea duraciones. Sin Android,
 * para poder testearlo en JVM.
 */

/** Un mes del diario con sus sesiones (ya ordenadas de más reciente a más antigua). */
data class DiaryMonth(
    val year: Int,
    val month: Int, // 0-11
    val sessions: List<PlaySession>
) {
    /** Etiqueta del mes en español, p. ej. «septiembre de 2026». */
    val label: String
        get() = MESES_ES[month].replaceFirstChar { it.uppercase() } + " de " + year
}

/** Nombres de mes en español (0-11). */
private val MESES_ES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

object DiaryUtils {

    /**
     * Agrupa las sesiones por mes (más reciente primero) y, dentro de cada mes,
     * por fecha descendente. Pura: no lee ni escribe.
     */
    fun groupByMonth(sessions: List<PlaySession>): List<DiaryMonth> {
        val cal = Calendar.getInstance()
        return sessions
            .sortedByDescending { it.date }
            .groupBy { session ->
                cal.timeInMillis = session.date
                cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
            }
            .map { (yearMonth, list) ->
                DiaryMonth(year = yearMonth.first, month = yearMonth.second, sessions = list)
            }
            .sortedWith(compareByDescending<DiaryMonth> { it.year }.thenByDescending { it.month })
    }

    /** Total de minutos de una lista de sesiones. */
    fun totalMinutes(sessions: List<PlaySession>): Int = sessions.sumOf { it.minutes }

    /**
     * Minutos jugados en la semana natural (lunes a domingo) que contiene [now].
     */
    fun minutesThisWeek(sessions: List<PlaySession>, now: Long): Int {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
        }
        val start = cal.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
        return sessions
            .filter { it.date in start.timeInMillis until end.timeInMillis }
            .sumOf { it.minutes }
    }

    /** Formatea minutos como «2 h 15 min», «45 min» o «3 h». */
    fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0 min"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m min"
            m == 0 -> "$h h"
            else -> "$h h $m min"
        }
    }

    /** Etiqueta corta de un día, p. ej. «29 sep 2026». */
    fun formatDay(date: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = MESES_ES[cal.get(Calendar.MONTH)].take(3)
        val year = cal.get(Calendar.YEAR)
        return "$day $month $year".lowercase(Locale("es", "ES"))
    }
}
