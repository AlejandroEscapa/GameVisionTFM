package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.DiaryUtils
import es.androidtfm.gamevision.data.library.PlaySession
import es.androidtfm.gamevision.data.library.Platforms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/*
 * Tests de la lógica del diario (F1 — Bloque 2, T1.8/T1.10).
 * JVM puro: sin Android ni Firebase.
 */
class DiaryUtilsTest {

    private fun dayMillis(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply {
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun session(id: String, gameId: String, date: Long, minutes: Int) =
        PlaySession(id = id, gameId = gameId, date = date, minutes = minutes)

    @Test
    fun `agrupa las sesiones por mes, mas reciente primero`() {
        val list = listOf(
            session("a", "1", dayMillis(2026, Calendar.SEPTEMBER, 10), 60),
            session("b", "1", dayMillis(2026, Calendar.SEPTEMBER, 28), 30),
            session("c", "2", dayMillis(2026, Calendar.AUGUST, 5), 90)
        )
        val months = DiaryUtils.groupByMonth(list)
        assertEquals(2, months.size)
        assertEquals(Calendar.SEPTEMBER, months[0].month)
        assertEquals(2026, months[0].year)
        // Dentro del mes, la más reciente primero.
        assertEquals(listOf("b", "a"), months[0].sessions.map { it.id })
        assertEquals(Calendar.AUGUST, months[1].month)
        assertTrue(months[0].label.startsWith("Septiembre"))
    }

    @Test
    fun `un mes sin sesiones no aparece`() {
        val list = listOf(session("a", "1", dayMillis(2026, Calendar.JANUARY, 3), 45))
        assertEquals(1, DiaryUtils.groupByMonth(list).size)
    }

    @Test
    fun `el total suma todos los minutos`() {
        val list = listOf(
            session("a", "1", dayMillis(2026, Calendar.SEPTEMBER, 10), 60),
            session("b", "1", dayMillis(2026, Calendar.SEPTEMBER, 11), 30),
            session("c", "2", dayMillis(2026, Calendar.AUGUST, 5), 90)
        )
        assertEquals(180, DiaryUtils.totalMinutes(list))
        assertEquals(0, DiaryUtils.totalMinutes(emptyList()))
    }

    @Test
    fun `la semana natural va de lunes a domingo`() {
        // Miércoles 2026-09-30 como "ahora".
        val now = dayMillis(2026, Calendar.SEPTEMBER, 30)
        val monday = dayMillis(2026, Calendar.SEPTEMBER, 28)
        val sundayBefore = dayMillis(2026, Calendar.SEPTEMBER, 27)
        val list = listOf(
            session("lunes", "1", monday, 40),
            session("ahora", "1", now, 20),
            session("domingo anterior", "1", sundayBefore, 999)
        )
        // Solo cuentan lunes (28) y miércoles (30).
        assertEquals(60, DiaryUtils.minutesThisWeek(list, now))
    }

    @Test
    fun `formatea minutos en horas y minutos`() {
        assertEquals("0 min", DiaryUtils.formatMinutes(0))
        assertEquals("45 min", DiaryUtils.formatMinutes(45))
        assertEquals("3 h", DiaryUtils.formatMinutes(180))
        assertEquals("2 h 15 min", DiaryUtils.formatMinutes(135))
    }

    @Test
    fun `hay plataformas suficientes y sin duplicados`() {
        assertTrue(Platforms.ALL.size >= 8)
        assertEquals(Platforms.ALL.size, Platforms.ALL.toSet().size)
        assertTrue(Platforms.ALL.contains("PC"))
    }
}
