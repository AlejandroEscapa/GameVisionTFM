package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.PlaySession
import es.androidtfm.gamevision.data.library.RewindUtils
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests del recap anual (F3/T3.6). Cubren CA3.6 (biblioteca vacía, un solo juego,
 * ausencia de diario, valores límite) y CA3.5 (poca actividad sigue celebrando),
 * además de los siete datos que pide T3.6.
 *
 * Las fechas se construyen con `java.time` y la zona del sistema, igual que la
 * lógica de producción: así el test no depende de la zona horaria de la máquina.
 */
class RewindUtilsTest {

    private val YEAR = 2026

    private fun dia(year: Int = YEAR, month: Int, dayOfMonth: Int): Long =
        LocalDate.of(year, month, dayOfMonth)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    private fun entry(
        id: String,
        status: LibraryStatus = LibraryStatus.PLAYING,
        rating: Double? = null,
        genres: List<String> = emptyList(),
        platform: String? = null,
        minutesTotal: Int = 0,
        startedAt: Long? = null,
        finishedAt: Long? = null,
        addedAt: Long? = null,
        name: String = id
    ) = LibraryEntry(
        gameId = id, status = status, rating = rating, genres = genres,
        lastPlatform = platform, minutesTotal = minutesTotal, startedAt = startedAt,
        finishedAt = finishedAt, addedAt = addedAt, name = name
    )

    private fun sesion(gameId: String, date: Long, minutes: Int) =
        PlaySession(id = "$gameId-$date", gameId = gameId, date = date, minutes = minutes)

    // ------------------------------------------------------------------
    // CA3.5 — poca actividad y biblioteca vacía siguen celebrando
    // ------------------------------------------------------------------

    @Test
    fun biblioteca_vacia_no_rompe_y_tiene_algo_que_decir() {
        val r = RewindUtils.compute(emptyList(), emptyList(), YEAR)

        assertFalse(r.hasActivity)
        assertEquals(0, r.minutesTotal)
        assertNull(r.topGame)
        assertNull(r.ratingAverage)
        assertNull(r.busiestMonth)
        assertEquals(0, r.longestStreakDays)
        // El suelo del recap: nunca una lista vacía (CA3.5).
        assertTrue("el recap nunca queda vacío", r.highlights.isNotEmpty())
        assertTrue("el titular no culpabiliza", r.headline.contains("a tu ritmo"))
    }

    @Test
    fun un_solo_juego_sin_fechas_ni_diario_sigue_teniendo_recap() {
        val r = RewindUtils.compute(listOf(entry("solo", name = "Celeste")), emptyList(), YEAR)

        // Sin minutos ni fechas no hay actividad que contar, pero el recap no puede
        // quedar vacío ni con tono negativo (CA3.5).
        assertFalse(r.hasActivity)
        assertTrue("el recap nunca queda vacío", r.highlights.isNotEmpty())
        assertTrue(r.headline.contains("a tu ritmo"))
    }

    @Test
    fun un_solo_juego_terminado_en_el_ano_ya_es_un_recap() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry(
                    "solo", name = "Celeste",
                    status = LibraryStatus.COMPLETED,
                    finishedAt = dia(month = 2, dayOfMonth = 3)
                )
            ),
            sessions = emptyList(),
            year = YEAR
        )

        assertTrue(r.hasActivity)
        assertEquals(1, r.gamesCompleted)
        assertTrue(r.highlights.any { it.contains("terminado") })
    }

    @Test
    fun un_ano_con_poca_actividad_cuenta_las_horas_y_celebra() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a", addedAt = dia(month = 3, dayOfMonth = 2))),
            sessions = listOf(sesion("a", dia(month = 3, dayOfMonth = 2), 45)),
            year = YEAR
        )

        assertTrue(r.hasActivity)
        assertEquals(45, r.minutesTotal)
        assertTrue(r.highlights.any { it.contains("45 min") || it.contains("menos de 1 h") })
        assertFalse("el titular de un año con actividad no es el de poca actividad", r.headline.contains("a tu ritmo"))
    }

    // ------------------------------------------------------------------
    // Exactitud de los datos (CA3.3)
    // ------------------------------------------------------------------

    @Test
    fun las_horas_del_ano_salen_del_diario_y_no_del_contador_de_la_ficha() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a", minutesTotal = 9999)),
            sessions = listOf(
                sesion("a", dia(month = 1, dayOfMonth = 10), 60),
                sesion("a", dia(month = 1, dayOfMonth = 11), 30)
            ),
            year = YEAR
        )

        // Con diario, el diario manda: 90, no 9999.
        assertEquals(90, r.minutesTotal)
    }

    @Test
    fun sin_diario_el_recap_usa_el_contador_de_la_ficha() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a", minutesTotal = 300, startedAt = dia(month = 5, dayOfMonth = 1)),
                entry("b", minutesTotal = 120, finishedAt = dia(month = 6, dayOfMonth = 1))
            ),
            sessions = emptyList(),
            year = YEAR
        )

        assertEquals(420, r.minutesTotal)
        assertEquals("a", r.topGame?.gameId)
        assertEquals(300, r.topGame?.minutes)
    }

    @Test
    fun el_juego_del_ano_es_el_de_mas_minutos_del_ano() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a", name = "Hades", minutesTotal = 5000),
                entry("b", name = "Elden Ring", minutesTotal = 100)
            ),
            sessions = listOf(
                sesion("a", dia(month = 1, dayOfMonth = 5), 60),
                sesion("b", dia(month = 1, dayOfMonth = 6), 600)
            ),
            year = YEAR
        )

        // Gana el del año (600), no el acumulado histórico (5000).
        assertEquals("b", r.topGame?.gameId)
        assertEquals("Elden Ring", r.topGame?.name)
        assertEquals(600, r.topGame?.minutes)
    }

    @Test
    fun la_nota_media_es_la_de_los_juegos_del_ano() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a", rating = 4.0),
                entry("b", rating = 5.0),
                entry("c", rating = 1.0) // fuera del año
            ),
            sessions = listOf(
                sesion("a", dia(month = 2, dayOfMonth = 1), 30),
                sesion("b", dia(month = 2, dayOfMonth = 2), 30)
            ),
            year = YEAR
        )

        assertEquals(4.5, r.ratingAverage!!, 0.001)
        assertEquals(2, r.ratedCount)
    }

    @Test
    fun sin_notas_en_el_ano_la_media_cae_a_la_de_toda_la_biblioteca() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a"),               // el único del año, y sin nota
                entry("b", rating = 3.0), // fuera del año
                entry("c", rating = 4.0)
            ),
            sessions = listOf(sesion("a", dia(month = 2, dayOfMonth = 1), 30)),
            year = YEAR
        )

        assertEquals(3.5, r.ratingAverage!!, 0.001)
    }

    @Test
    fun los_generos_dominantes_salen_de_los_juegos_del_ano() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a", genres = listOf("RPG", "Aventura")),
                entry("b", genres = listOf("RPG")),
                entry("c", genres = listOf("Deportes"))
            ),
            sessions = listOf(
                sesion("a", dia(month = 4, dayOfMonth = 1), 30),
                sesion("b", dia(month = 4, dayOfMonth = 2), 30)
            ),
            year = YEAR
        )

        assertEquals("RPG" to 2, r.topGenres.first())
        assertFalse("el género del juego que no se jugó no entra", r.topGenres.any { it.first == "Deportes" })
    }

    @Test
    fun la_plataforma_principal_es_la_de_mas_minutos_del_ano() {
        val r = RewindUtils.compute(
            entries = listOf(
                entry("a", platform = "PC"),
                entry("b", platform = "Switch")
            ),
            sessions = listOf(
                sesion("a", dia(month = 7, dayOfMonth = 1), 60),
                sesion("b", dia(month = 7, dayOfMonth = 2), 600)
            ),
            year = YEAR
        )

        assertEquals("Switch", r.mainPlatform)
    }

    @Test
    fun el_mes_mas_activo_es_el_de_mas_minutos_y_lleva_etiqueta() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a")),
            sessions = listOf(
                sesion("a", dia(month = 9, dayOfMonth = 1), 60),
                sesion("a", dia(month = 9, dayOfMonth = 2), 60),
                sesion("a", dia(month = 12, dayOfMonth = 1), 30)
            ),
            year = YEAR
        )

        assertEquals(8, r.busiestMonth?.index)
        assertEquals("Septiembre", r.busiestMonth?.label)
        assertEquals(120, r.busiestMonth?.minutes)
    }

    // ------------------------------------------------------------------
    // Racha: días naturales consecutivos
    // ------------------------------------------------------------------

    @Test
    fun la_racha_cuenta_dias_consecutivos_y_no_sesiones() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a")),
            sessions = listOf(
                // Tres sesiones el mismo día cuentan como un día de racha.
                sesion("a", dia(month = 3, dayOfMonth = 1), 10),
                sesion("a", dia(month = 3, dayOfMonth = 1), 10),
                sesion("a", dia(month = 3, dayOfMonth = 2), 10),
                sesion("a", dia(month = 3, dayOfMonth = 3), 10),
                // Hueco del 4 al 9.
                sesion("a", dia(month = 3, dayOfMonth = 10), 10)
            ),
            year = YEAR
        )

        assertEquals(3, r.longestStreakDays)
    }

    @Test
    fun la_racha_cruza_el_cambio_de_mes() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a")),
            sessions = listOf(
                sesion("a", dia(month = 1, dayOfMonth = 30), 10),
                sesion("a", dia(month = 1, dayOfMonth = 31), 10),
                sesion("a", dia(month = 2, dayOfMonth = 1), 10)
            ),
            year = YEAR
        )

        assertEquals(3, r.longestStreakDays)
    }

    @Test
    fun la_racha_cruza_el_cambio_de_ano_solo_dentro_del_ano_pedido() {
        val sessions = listOf(
            sesion("a", dia(2025, 12, 30), 10),
            sesion("a", dia(2025, 12, 31), 10),
            sesion("a", dia(2026, 1, 1), 10),
            sesion("a", dia(2026, 1, 2), 10)
        )

        // Dentro de 2026 la racha es de 2 (1 y 2 de enero); el 31/12/2025 no entra.
        assertEquals(2, RewindUtils.longestStreakDays(sessions, 2026))
        // Y dentro de 2025, de 2 (30 y 31 de diciembre).
        assertEquals(2, RewindUtils.longestStreakDays(sessions, 2025))
    }

    @Test
    fun las_sesiones_de_otro_ano_no_cuentan_en_el_recap() {
        val r = RewindUtils.compute(
            entries = listOf(entry("a")),
            sessions = listOf(
                sesion("a", dia(2025, 6, 1), 500),
                sesion("a", dia(YEAR, 6, 1), 30)
            ),
            year = YEAR
        )

        assertEquals(30, r.minutesTotal)
        assertEquals(1, r.longestStreakDays)
    }

    @Test
    fun una_sesion_suelta_da_racha_de_uno_y_sin_sesiones_de_cero() {
        assertEquals(
            1,
            RewindUtils.compute(
                entries = listOf(entry("a")),
                sessions = listOf(sesion("a", dia(month = 8, dayOfMonth = 4), 20)),
                year = YEAR
            ).longestStreakDays
        )
        assertEquals(
            0,
            RewindUtils.compute(listOf(entry("a")), emptyList(), YEAR).longestStreakDays
        )
    }

    // ------------------------------------------------------------------
    // Utilidades de apoyo
    // ------------------------------------------------------------------

    @Test
    fun los_anos_disponibles_incluyen_los_del_diario_y_las_fichas() {
        val years = RewindUtils.availableYears(
            entries = listOf(
                entry("a", finishedAt = dia(2024, 5, 1)),
                entry("b", addedAt = dia(2025, 5, 1))
            ),
            sessions = listOf(sesion("a", dia(2023, 5, 1), 10)),
            now = dia(2026, 10, 2)
        )

        assertEquals(listOf(2026, 2025, 2024, 2023), years)
    }

    @Test
    fun el_formato_de_horas_totales_es_compacto() {
        assertEquals("0 h", RewindUtils.formatHoursTotal(0))
        assertEquals("menos de 1 h", RewindUtils.formatHoursTotal(45))
        assertEquals("1 h", RewindUtils.formatHoursTotal(90))
        assertEquals("312 h", RewindUtils.formatHoursTotal(312 * 60 + 59))
    }

    @Test
    fun el_ordinal_de_dia_avanza_de_uno_en_uno() {
        assertEquals(1L, RewindUtils.dayOrdinal(dia(month = 3, dayOfMonth = 2)) -
            RewindUtils.dayOrdinal(dia(month = 3, dayOfMonth = 1)))
        assertEquals(1L, RewindUtils.dayOrdinal(dia(month = 1, dayOfMonth = 1)) -
            RewindUtils.dayOrdinal(dia(2025, 12, 31)))
    }

    @Test
    fun el_recap_no_pasa_de_topN_juegos() {
        val entries = (1..8).map { entry("g$it") }
        val sessions = (1..8).map { i -> sesion("g$i", dia(month = 5, dayOfMonth = i), i * 10) }

        val r = RewindUtils.compute(entries, sessions, YEAR, topN = 4)

        assertEquals(4, r.topGames.size)
        assertEquals("g8", r.topGames.first().gameId)
    }
}
