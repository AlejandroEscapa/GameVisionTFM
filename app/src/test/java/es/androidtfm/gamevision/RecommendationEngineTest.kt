package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.RecommendationEngine
import es.androidtfm.gamevision.data.library.RecommendationEngine.Candidate
import es.androidtfm.gamevision.data.library.RecommendationEngine.Mood
import es.androidtfm.gamevision.data.library.RecommendationEngine.Reason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests del motor "¿Qué juego ahora?" (F3/T3.1). Casos de CA3.6: biblioteca
 * vacía, un solo juego, duraciones extremas, sin tiempo indicado — más los
 * factores de D3.1 (tiempo, géneros, ánimo, nota) y la explicación (D3.2).
 */
class RecommendationEngineTest {

    private fun cand(
        id: String,
        status: LibraryStatus = LibraryStatus.PLAYING,
        genres: List<String> = emptyList(),
        rating: Double? = null,
        minutesTotal: Int = 0,
        durationMin: Int? = null,
        name: String = id
    ) = Candidate(
        gameId = id, name = name, status = status, genres = genres,
        rating = rating, minutesTotal = minutesTotal, durationMin = durationMin
    )

    // ------------------------------------------------------------------
    // Elegibilidad
    // ------------------------------------------------------------------

    @Test
    fun biblioteca_vacia_devuelve_vacio() {
        assertTrue(RecommendationEngine.recommend(emptyList(), 30).isEmpty())
    }

    @Test
    fun solo_recomienda_jugando_y_en_pausa() {
        val r = RecommendationEngine.recommend(
            listOf(
                cand("a", LibraryStatus.COMPLETED, durationMin = 60),
                cand("b", LibraryStatus.WISHED, durationMin = 60),
                cand("c", LibraryStatus.ABANDONED, durationMin = 60),
                cand("d", LibraryStatus.RETIRED, durationMin = 60),
                cand("e", LibraryStatus.PLAYING, durationMin = 60),
                cand("f", LibraryStatus.PAUSED, durationMin = 60)
            ),
            60
        )
        assertEquals(listOf("e", "f"), r.map { it.gameId })
    }

    @Test
    fun un_solo_juego_unico_candidato_con_explicacion() {
        val r = RecommendationEngine.recommend(listOf(cand("uno", durationMin = 120)), 30)
        assertEquals(1, r.size)
        assertEquals("uno", r[0].gameId)
        assertTrue(r[0].explanation.isNotBlank())
    }

    // ------------------------------------------------------------------
    // Factor tiempo (CA3.1/CA3.2): excluye el de 40 h con 30 min y explica
    // ------------------------------------------------------------------

    @Test
    fun con_30_min_excluye_el_juego_de_40h_y_explica_por_que() {
        val corto = cand("corto", durationMin = 30)
        val largo = cand("largo", durationMin = 40 * 60)
        val r = RecommendationEngine.recommend(listOf(corto, largo), 30)
        assertEquals(listOf("corto"), r.map { it.gameId })
        assertTrue(r[0].explanation.contains("cabe"))
        assertTrue(r[0].reasons.contains(Reason.CABE_HOY))
    }

    @Test
    fun el_tiempo_disponible_cambia_la_recomendacion_de_forma_coherente() {
        val sesion = cand("sesion", durationMin = 90)      // 1,5 h
        val tarde = cand("tarde", durationMin = 5 * 60)    // 5 h
        val candidates = listOf(sesion, tarde)

        val con30 = RecommendationEngine.recommend(candidates, 30)
        // Con 30 min nada cabe; se muestra la biblioteca con el que está más
        // cerca del final primero (anti-culpa: nunca una pantalla vacía).
        assertEquals("sesion", con30.first().gameId)

        val con2h = RecommendationEngine.recommend(candidates, 120)
        assertEquals("sesion", con2h.first().gameId) // 90 <= 120: cabe hoy

        val conTarde = RecommendationEngine.recommend(candidates, 6 * 60)
        assertTrue(RecommendationEngine.Reason.CABE_HOY in conTarde.first { it.gameId == "tarde" }.reasons)
    }

    @Test
    fun jugando_y_cerca_del_final_se_prioriza() {
        val casi = cand("casi", minutesTotal = 55, durationMin = 60) // quedan 5
        val lejano = cand("lejano", minutesTotal = 10, durationMin = 600)
        val r = RecommendationEngine.recommend(listOf(lejano, casi), 60)
        assertEquals("casi", r.first().gameId)
        assertTrue(Reason.CERCA_DEL_FINAL in r.first().reasons)
    }

    @Test
    fun juego_terminado_al_100_explica_marcarlo() {
        val r = RecommendationEngine.recommend(
            listOf(cand("hecho", minutesTotal = 600, durationMin = 600)), 60
        )
        assertTrue(r[0].explanation.contains("terminado"))
    }

    // ------------------------------------------------------------------
    // Duración desconocida y sin tiempo indicado (CA3.6)
    // ------------------------------------------------------------------

    @Test
    fun duracion_desconocida_se_recomienda_sin_prometer_tiempos() {
        val r = RecommendationEngine.recommend(listOf(cand("x")), 30)
        assertTrue(Reason.DURACION_DESCONOCIDA in r[0].reasons)
        assertTrue(r[0].explanation.contains("sin dato", ignoreCase = true))
    }

    @Test
    fun sin_tiempo_indicado_el_factor_tiempo_no_puntua_ni_promete() {
        val r = RecommendationEngine.recommend(
            listOf(cand("a", durationMin = 6000)), availableMinutes = 0
        )
        assertFalse(r[0].reasons.contains(Reason.CABE_HOY))
        assertFalse(r[0].explanation.contains("cabe"))
    }

    // ------------------------------------------------------------------
    // Factor géneros y ánimo (D3.1b/D3.1c)
    // ------------------------------------------------------------------

    @Test
    fun afinidad_de_generos_suma_y_explica() {
        val rpg = cand("rpg", genres = listOf("RPG", "Adventure"), durationMin = 120)
        val otro = cand("otro", genres = listOf("Sports"), durationMin = 120)
        val r = RecommendationEngine.recommend(listOf(otro, rpg), 120, favoriteGenres = setOf("RPG"))
        assertEquals("rpg", r.first().gameId)
        assertTrue(Reason.GENERO_AFIN in r.first().reasons)
        assertTrue(r.first().explanation.contains("rpg"))
    }

    @Test
    fun animo_algo_corto_excluye_lo_que_queda_largo_o_sin_dato() {
        val corto = cand("corto", durationMin = 240)   // quedan 4 h
        val largo = cand("largo", durationMin = 400 * 60)
        val sinDato = cand("sinDato")
        val r = RecommendationEngine.recommend(
            listOf(corto, largo, sinDato), 60, mood = Mood.SHORT
        )
        assertEquals(listOf("corto"), r.map { it.gameId })
    }

    @Test
    fun animo_historia_empuja_rpg_y_aventura() {
        val rpg = cand("rpg", genres = listOf("RPG"), durationMin = 120)
        val deporte = cand("deporte", genres = listOf("Sports"), durationMin = 120)
        val r = RecommendationEngine.recommend(
            listOf(deporte, rpg), 60, mood = Mood.STORY
        )
        assertEquals("rpg", r.first().gameId)
    }

    // ------------------------------------------------------------------
    // Nota histórica (D3.1d) y límites
    // ------------------------------------------------------------------

    @Test
    fun nota_alta_suma_y_explica() {
        val querido = cand("querido", rating = 4.5, durationMin = 120)
        val soso = cand("soso", rating = 2.0, durationMin = 120)
        val r = RecommendationEngine.recommend(listOf(soso, querido), 120)
        assertEquals("querido", r.first().gameId)
        assertTrue(Reason.NOTA_ALTA in r.first().reasons)
        assertTrue(r.first().explanation.contains("4.5"))
    }

    @Test
    fun respeta_el_maximo_de_resultados_y_orden_por_puntuacion() {
        val candidates = (1..5).map { cand("j$it", durationMin = 60) }
        val r = RecommendationEngine.recommend(candidates, 60, max = 3)
        assertEquals(3, r.size)
        assertTrue(r.zipWithNext().all { (a, b) -> a.score >= b.score })
    }
}
