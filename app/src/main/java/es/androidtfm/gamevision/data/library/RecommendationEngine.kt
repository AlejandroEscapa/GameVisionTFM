package es.androidtfm.gamevision.data.library

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/*
 * Motor de recomendación "¿Qué juego ahora?" (F3/T3.1, decisiones D3.1-D3.2).
 *
 * Lógica PURA: sin Android, sin Firebase, sin RAWG — entra datos, salen
 * recomendaciones explicadas. Los pesos y factores son de D3.1: tiempo
 * disponible vs duración restante + afinidad con los géneros favoritos del
 * momento + estado de ánimo como filtro manual opcional. Toda recomendación
 * lleva SU explicación (D3.2: nunca una caja negra).
 *
 * Convenciones de datos:
 *  - `durationMin` = duración total del juego (playtimeManual si existe; si no,
 *    la del HLTB con mejor encaje). null = sin dato.
 *  - `minutesTotal` = lo ya jugado; lo que queda es durationMin - minutesTotal.
 */

object RecommendationEngine {

    /** Estado de ánimo (D3.1c): filtro manual opcional sobre la recomendación. */
    enum class Mood(val label: String) {
        ANY("Cualquiera"),
        SHORT("Algo corto"),
        STORY("Historia"),
        RELAX("Relajado");

        companion object {
            fun fromLabel(label: String?): Mood = entries.firstOrNull { it.label == label } ?: ANY
        }
    }

    /** Motivos por los que se recomienda (alimentan la explicación, D3.2). */
    enum class Reason {
        /** Lo que queda del juego cabe en el tiempo disponible. */
        CABE_HOY,
        /** Está jugándose y queda poco: oportunidad de cerrarlo. */
        CERCA_DEL_FINAL,
        /** Encaja con los géneros favoritos del momento. */
        GENERO_AFIN,
        /** El usuario le puso nota alta: le gustó. */
        NOTA_ALTA,
        /** Lo eligió antes con «Jugar»: sigue siendo buena opción (T3.5). */
        ELEGIDO_ANTES,
        /** Sin dato de duración: se recomienda sin prometer tiempos. */
        DURACION_DESCONOCIDA
    }

    data class Candidate(
        val gameId: String,
        val name: String,
        val status: LibraryStatus,
        val genres: List<String> = emptyList(),
        val rating: Double? = null,
        val minutesTotal: Int = 0,
        val durationMin: Int? = null
    )

    data class Recommendation(
        val gameId: String,
        val name: String,
        val score: Int,
        val minutesRemaining: Int?,
        val reasons: List<Reason>,
        val explanation: String
    )

    // Géneros por estado de ánimo (valores RAWG, en inglés como llegan del catálogo)
    private val MOOD_GENRES: Map<Mood, Set<String>> = mapOf(
        Mood.SHORT to setOf("Indie", "Puzzle", "Arcade", "Casual"),
        Mood.STORY to setOf("RPG", "Adventure", "Narrative", "Story-rich"),
        Mood.RELAX to setOf("Simulation", "Indie", "Puzzle", "Sandbox", "Casual")
    )
    private val MOOD_MAX_MINUTES = mapOf(Mood.SHORT to 300) // "algo corto" = ≤ 5 h restantes

    /**
     * Recomienda hasta [max] juegos de [candidates] para [availableMinutes] libres.
     *
     * @param favoriteGenres géneros favoritos del momento (de StatisticsUtils).
     * @param availableMinutes tiempo libre; **<= 0 = sin límite indicado**: el
     *   factor tiempo no puntúa y la explicación no promete ventanas.
     * @param descartados juegos que el usuario descartó (T3.5): no vuelven a salir.
     * @param aceptados juegos que el usuario eligió con «Jugar» (T3.5): suben.
     */
    fun recommend(
        candidates: List<Candidate>,
        availableMinutes: Int,
        favoriteGenres: Set<String> = emptySet(),
        mood: Mood = Mood.ANY,
        max: Int = 3,
        descartados: Set<String> = emptySet(),
        aceptados: Set<String> = emptySet()
    ): List<Recommendation> {
        val elegibles = candidates.filter {
            (it.status == LibraryStatus.PLAYING || it.status == LibraryStatus.PAUSED) &&
                it.gameId !in descartados
        }
        if (elegibles.isEmpty()) return emptyList()

        val limiteMood = MOOD_MAX_MINUTES[mood]
        val generosMood = MOOD_GENRES[mood].orEmpty()

        val scored = elegibles.mapNotNull { c ->
            val restante = c.durationMin?.let { d -> max(0, d - c.minutesTotal) }

            // Filtro duro del ánimo "Algo corto": sin dato o demasiado largo, fuera.
            if (limiteMood != null && (restante == null || restante > limiteMood)) return@mapNotNull null

            var score = 0
            val motivos = mutableListOf<Reason>()
            var muyLargoParaHoy = false

            // Factor 1 — tiempo disponible vs duración restante (peso mayor, D3.1b)
            if (availableMinutes > 0 && restante != null) {
                when {
                    restante == 0 -> { score += 60; motivos += Reason.CABE_HOY }
                    restante <= availableMinutes -> { score += 60; motivos += Reason.CABE_HOY }
                    restante <= availableMinutes * 2 -> { score += 30; motivos += Reason.CERCA_DEL_FINAL }
                    else -> { score += 10; muyLargoParaHoy = true }
                }
                // Cerca del final y jugándose: oportunidad de cerrarlo.
                if (c.status == LibraryStatus.PLAYING && restante in 1..availableMinutes * 2) {
                    if (Reason.CERCA_DEL_FINAL !in motivos) motivos += Reason.CERCA_DEL_FINAL
                }
            } else if (restante == null) {
                score += 20
                motivos += Reason.DURACION_DESCONOCIDA
            } else if (availableMinutes <= 0) {
                // Sin límite indicado: el tiempo no puntúa ni explica.
            }

            // Factor 2 — afinidad con los géneros favoritos del momento (D3.1b)
            val afinidad = c.genres.count { it in favoriteGenres }
            if (afinidad > 0) {
                score += min(25, 10 * afinidad)
                motivos += Reason.GENERO_AFIN
            }
            // El ánimo también empuja (filter manual elegido por el usuario)
            if (mood != Mood.ANY && c.genres.any { it in generosMood }) score += 15

            // Factor 3 — nota histórica alta (D3.1d, matiz): le gustó, es buen candidato
            if ((c.rating ?: 0.0) >= 4.0) {
                score += 10
                motivos += Reason.NOTA_ALTA
            }

            // Factor 4 — aprendizaje ligero (T3.5): lo eligió antes, sigue arriba.
            if (c.gameId in aceptados) {
                score += 15
                motivos += Reason.ELEGIDO_ANTES
            }

            Recommendation(
                gameId = c.gameId,
                name = c.name.ifBlank { c.gameId },
                score = score,
                minutesRemaining = restante,
                reasons = motivos,
                explanation = explica(c, restante, availableMinutes, favoriteGenres, motivos)
            ) to muyLargoParaHoy
        }

        var resultado = scored.map { it.first }

        // CA3.1: si algo cabe (o casi) en el tiempo de hoy, los juegos "muy largos
        // para hoy" se excluyen de la respuesta — la recomendación no empuja al
        // backlog. Si NADA cabe, se dejan todos: mostrar algo con explicación
        // honesta es mejor que una pantalla vacía (tono anti-culpa, D-C10).
        if (availableMinutes > 0) {
            val hayAlgoViable = resultado.any {
                Reason.CABE_HOY in it.reasons || Reason.CERCA_DEL_FINAL in it.reasons
            }
            if (hayAlgoViable) {
                resultado = resultado.filterIndexed { i, _ -> !scored[i].second }
            }
        }

        return resultado
            .sortedWith(compareByDescending<Recommendation> { it.score }.thenBy { it.minutesRemaining ?: Int.MAX_VALUE })
            .take(max)
    }

    /** Explicación en lenguaje natural (D3.2: siempre el porqué, nunca caja negra). */
    private fun explica(
        c: Candidate,
        restante: Int?,
        disponible: Int,
        favoritos: Set<String>,
        motivos: List<Reason>
    ): String {
        val nombre = c.name.ifBlank { "este juego" }
        val partes = mutableListOf<String>()

        if (disponible > 0 && restante != null) {
            when {
                restante == 0 ->
                    partes += "Ya lo has terminado: solo falta marcarlo."
                restante <= disponible -> {
                    val quedan = if (restante >= 60) "≈${(restante / 60.0).roundToInt()} h" else "$restante min"
                    val tienes = if (disponible >= 60) "≈${(disponible / 60.0).roundToInt()} h" else "$disponible min"
                    partes += "Te quedan $quedan y hoy tienes $tienes: cabe."
                }
                else -> {
                    val quedan = if (restante >= 60) "≈${(restante / 60.0).roundToInt()} h" else "$restante min"
                    partes += "Te quedan $quedan: hoy toca avanzar, no cerrar."
                }
            }
        }
        if (Reason.DURACION_DESCONOCIDA in motivos) {
            partes += "Sin dato de duración: para jugar sin mirar el reloj."
        }
        val generosAfin = c.genres.filter { it in favoritos }
        if (generosAfin.isNotEmpty()) {
            partes += "Es de tus géneros de ahora (${generosAfin.lower()})."
        }
        if (Reason.NOTA_ALTA in motivos && c.rating != null) {
            partes += "Le pusiste ${c.rating}."
        }
        if (Reason.ELEGIDO_ANTES in motivos) {
            partes += "Lo marcaste para jugar."
        }
        if (partes.isEmpty()) partes += "Está en tu biblioteca esperando."
        return partes.joinToString(" ")
    }

    private fun List<String>.lower(): String =
        joinToString(", ") { it.lowercase() }
}
