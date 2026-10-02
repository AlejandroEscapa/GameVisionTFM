package es.androidtfm.gamevision.data.library

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/*
 * Lógica pura del recap anual (F3 — Bloque B, T3.6).
 *
 * Calcula el «GameVision Rewind» a partir de la biblioteca y el diario, sin
 * Android ni Firebase: 100 % testeable en JVM (CA3.6).
 *
 * Decisiones que aplica:
 *  - D3.3: se calcula en el cliente, con los datos que ya están en el dispositivo.
 *  - D3.6: **celebrar siempre, nunca culpabilizar**. Un año con poca actividad
 *    produce un recap positivo y nunca vacío (CA3.5).
 *
 * Sobre las fechas: aquí se usa `java.time` en lugar de `Calendar` (que es lo que
 * usan DiaryUtils y StatisticsUtils) porque la racha necesita el **ordinal de día**
 * (`LocalDate.toEpochDay()`), que es correcto en cambios de mes, de año y de hora
 * de verano. `Calendar` no da ese ordinal sin aritmética frágil. minSdk 33 lo
 * permite sin desugaring.
 */

/** Un juego dentro del recap, con los minutos que le corresponden en el año. */
data class RewindGame(
    val gameId: String,
    val name: String,
    val coverUrl: String?,
    val minutes: Int,
    val rating: Double?
)

/** Mes más activo del año, con su etiqueta en español. */
data class RewindMonth(val index: Int, val label: String, val minutes: Int)

/**
 * La historia del usuario con **un** juego (F3/T3.10): el mini-recap que multiplica
 * las ocasiones de compartir sin depender del cierre del año (D3.5).
 */
data class GameStory(
    val gameId: String,
    val name: String,
    val coverUrl: String?,
    val status: LibraryStatus,
    val minutesTotal: Int,
    val sessionsCount: Int,
    val firstPlayedAt: Long?,
    val lastPlayedAt: Long?,
    val longestStreakDays: Int,
    val rating: Double?,
    /** Líneas de la historia. **Nunca vacía** (mismo suelo que el recap anual). */
    val highlights: List<String>
)

/**
 * El recap anual completo. `hasActivity` distingue el caso normal del caso «poca
 * actividad» (CA3.5), que **no** es un error: se pinta igual, con otro texto.
 */
data class RewindData(
    val year: Int,
    /** Minutos del año (o el acumulado de las fichas si no hay sesiones apuntadas). */
    val minutesTotal: Int,
    val gamesPlayed: Int,
    val gamesCompleted: Int,
    /** El juego del año: el que más minutos se llevó. */
    val topGame: RewindGame?,
    /** Los `topN` juegos del año, de más a menos minutos (para la tarjeta: 4–6). */
    val topGames: List<RewindGame>,
    /** Géneros dominantes del año: (género → nº de juegos), descendente. */
    val topGenres: List<Pair<String, Int>>,
    /** Nota media de los juegos del año (null si no hay ninguna nota). */
    val ratingAverage: Double?,
    val ratedCount: Int,
    /** Racha más larga **dentro del año**: días naturales consecutivos jugados. */
    val longestStreakDays: Int,
    val busiestMonth: RewindMonth?,
    val mainPlatform: String?,
    /** false = año sin actividad registrada; el recap se pinta igual (CA3.5). */
    val hasActivity: Boolean,
    /** Titular del recap, ya en tono celebratorio (D3.6). */
    val headline: String,
    /** Líneas de datos para la pantalla. **Nunca vacía.** */
    val highlights: List<String>
)

object RewindUtils {

    /** Etiquetas de mes en español (0-11), para el mes más activo. */
    private val MESES_ES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    private val ESTADOS_TERMINADOS = setOf(LibraryStatus.COMPLETED, LibraryStatus.COLLECTED)

    /** Año natural de un instante (null si no hay fecha válida). */
    fun yearOf(epochMillis: Long?): Int? {
        if (epochMillis == null || epochMillis <= 0) return null
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().year
    }

    /**
     * Ordinal de día (días desde 1970-01-01). Dos días consecutivos difieren en 1,
     * incluso entre meses, entre años y con cambio de hora.
     */
    fun dayOrdinal(epochMillis: Long): Long =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

    /**
     * Años con algo que recapitulear, de más reciente a más antiguo. Lo usa la UI
     * para ofrecer el Rewind del año en curso y los anteriores con datos.
     */
    fun availableYears(
        entries: List<LibraryEntry>,
        sessions: List<PlaySession>,
        now: Long
    ): List<Int> {
        val years = buildSet {
            sessions.forEach { yearOf(it.date)?.let(::add) }
            entries.forEach { e ->
                yearOf(e.finishedAt)?.let(::add)
                yearOf(e.startedAt)?.let(::add)
                yearOf(e.addedAt)?.let(::add)
            }
            yearOf(now)?.let(::add)
        }
        return years.sortedDescending()
    }

    /**
     * Racha más larga: el mayor número de días naturales consecutivos con al menos
     * una sesión, **dentro del año**. Con sesiones en días sueltos devuelve 1; sin
     * sesiones, 0.
     */
    fun longestStreakDays(sessions: List<PlaySession>, year: Int): Int =
        rachaMasLarga(sessions.filter { yearOf(it.date) == year }.map { dayOrdinal(it.date) })

    /**
     * La historia del usuario con [entry]: horas, sesiones, recorrido temporal, racha
     * y nota. Pura y testeable, como el recap anual.
     *
     * Si no hay diario para ese juego, cae al contador de la ficha (las «horas de
     * bolsillo»), igual que hace el recap anual.
     */
    fun computeForGame(entry: LibraryEntry, sessions: List<PlaySession>): GameStory {
        val propias = sessions
            .filter { it.gameId == entry.gameId && it.minutes > 0 }
            .sortedBy { it.date }
        val minutos = if (propias.isEmpty()) {
            entry.minutesTotal.coerceAtLeast(0)
        } else {
            propias.sumOf { it.minutes }
        }
        val primero = propias.firstOrNull()?.date
        val ultimo = propias.lastOrNull()?.date
        val racha = rachaMasLarga(propias.map { dayOrdinal(it.date) })

        val highlights = buildList {
            if (minutos > 0) add("${formatHoursTotal(minutos)} en total")
            if (propias.isNotEmpty()) {
                add(if (propias.size == 1) "1 sesión apuntada" else "${propias.size} sesiones apuntadas")
            }
            if (primero != null && ultimo != null && primero != ultimo) {
                add("De ${fechaCorta(primero)} a ${fechaCorta(ultimo)}")
            }
            if (racha > 1) add("Racha más larga: $racha días seguidos")
            entry.rating?.let { add("Tu nota: ${formatNota(it)}") }
            // Suelo: una ficha sin partidas tampoco deja la historia en blanco.
            if (isEmpty()) add("Todavía sin partidas apuntadas")
        }

        return GameStory(
            gameId = entry.gameId,
            name = entry.name.ifBlank { entry.gameId },
            coverUrl = entry.coverUrl,
            status = entry.status,
            minutesTotal = minutos,
            sessionsCount = propias.size,
            firstPlayedAt = primero,
            lastPlayedAt = ultimo,
            longestStreakDays = racha,
            rating = entry.rating,
            highlights = highlights
        )
    }

    /** Nota en formato español: 4,5. Vivía duplicada en la pantalla; aquí es testeable. */
    fun formatNota(value: Double): String =
        String.format(Locale("es", "ES"), "%.1f", value)

    /** Día y mes en español, para el recorrido de una historia: «3 de marzo». */
    internal fun fechaCorta(epochMillis: Long): String {
        val fecha = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return "${fecha.dayOfMonth} de ${MESES_ES[fecha.monthValue - 1]}"
    }

    /** Mayor número de días consecutivos en una lista de ordinales de día. */
    private fun rachaMasLarga(dias: List<Long>): Int {
        val orden = dias.distinct().sorted()
        if (orden.isEmpty()) return 0
        var mejor = 1
        var actual = 1
        for (i in 1 until orden.size) {
            actual = if (orden[i] == orden[i - 1] + 1L) actual + 1 else 1
            if (actual > mejor) mejor = actual
        }
        return mejor
    }

    /** Horas totales en formato compacto para el recap: «312 h», «1 h», «0 h». */
    fun formatHoursTotal(minutes: Int): String {
        if (minutes <= 0) return "0 h"
        val hours = minutes / 60
        return if (hours <= 0) "menos de 1 h" else "$hours h"
    }

    /**
     * Resumen de juegos en una línea: «1 juego, 1 terminado», «12 juegos, 3 terminados».
     *
     * Vive aquí y no en la pantalla porque el singular/plural es una regla de texto, no
     * de pintado: escrito a mano en la UI salió «1 juegos, 1 terminados» (visto en el
     * emulador el 02/10/2026) y el copy en español es parte del contrato (§8).
     */
    fun resumenDeJuegos(gamesPlayed: Int, gamesCompleted: Int): String {
        val juegos = if (gamesPlayed == 1) "1 juego" else "$gamesPlayed juegos"
        val terminados = if (gamesCompleted == 1) "1 terminado" else "$gamesCompleted terminados"
        return "$juegos, $terminados"
    }

    /**
     * Calcula el recap de [year]. Puro: mismos `entries` + `sessions` + `year` →
     * mismo resultado (salvo el orden de los empates, que se rompe por nombre).
     *
     * @param topN cuántos juegos y géneros destacar (la tarjeta compartible debe
     *   quedarse en 4–6 para no tardar en generar la imagen — riesgo anotado en F3).
     */
    fun compute(
        entries: List<LibraryEntry>,
        sessions: List<PlaySession> = emptyList(),
        year: Int,
        topN: Int = 5
    ): RewindData {
        val sessionsOfYear = sessions.filter { yearOf(it.date) == year && it.minutes > 0 }
        val minutesFromSessions = sessionsOfYear.sumOf { it.minutes }

        // Juegos del año: si hay sesiones, los que se jugaron; si no, los que tienen
        // actividad registrada en el año (fichas sin diario: «horas de bolsillo»).
        val entriesOfYear = if (sessionsOfYear.isNotEmpty()) {
            val ids = sessionsOfYear.map { it.gameId }.toSet()
            entries.filter { it.gameId in ids }
        } else {
            entries.filter { e ->
                yearOf(e.finishedAt) == year || yearOf(e.startedAt) == year ||
                    yearOf(e.addedAt) == year
            }.ifEmpty { entries }
        }

        // Minutos por juego. Con diario manda el diario; sin diario, el contador de
        // la ficha (es la única fuente que existe en ese caso).
        val minutesByGame: Map<String, Int> = if (sessionsOfYear.isNotEmpty()) {
            sessionsOfYear.groupBy { it.gameId }.mapValues { (_, s) -> s.sumOf { it.minutes } }
        } else {
            entriesOfYear.associate { it.gameId to it.minutesTotal.coerceAtLeast(0) }
        }

        val minutesTotal = if (sessionsOfYear.isNotEmpty()) {
            minutesFromSessions
        } else {
            entriesOfYear.sumOf { it.minutesTotal.coerceAtLeast(0) }
        }

        val entriesById = entries.associateBy { it.gameId }
        val topGames = minutesByGame
            .filter { it.value > 0 }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN)
            .mapNotNull { (gameId, minutes) ->
                val entry = entriesById[gameId] ?: return@mapNotNull null
                RewindGame(
                    gameId = gameId,
                    name = entry.name.ifBlank { gameId },
                    coverUrl = entry.coverUrl,
                    minutes = minutes,
                    rating = entry.rating
                )
            }

        val gamesPlayed = if (sessionsOfYear.isNotEmpty()) {
            sessionsOfYear.map { it.gameId }.distinct().size
        } else {
            entriesOfYear.count { it.minutesTotal > 0 || ESTADOS_TERMINADOS.contains(it.status) }
        }

        val gamesCompleted = entriesOfYear.count { e ->
            yearOf(e.finishedAt) == year ||
                (e.finishedAt == null && ESTADOS_TERMINADOS.contains(e.status))
        }

        // Géneros dominantes: los de los juegos que cuentan en el año.
        val gamesForGenres = entriesOfYear.filter { it.gameId in minutesByGame.keys || minutesByGame.isEmpty() }
        val topGenres = gamesForGenres
            .flatMap { e -> e.genres.map { it.trim() } }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN)
            .map { it.key to it.value }

        // Nota media del año; si en el año no hay ninguna nota, la de toda la biblioteca
        // (mejor un dato que un hueco, y sigue siendo celebración).
        val ratedOfYear = gamesForGenres.mapNotNull { it.rating }
        val rated = ratedOfYear.ifEmpty { entries.mapNotNull { it.rating } }
        val ratingAverage = RatingUtils.average(rated.sum(), rated.size)

        val busiestMonth = sessionsOfYear
            .groupBy { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().monthValue - 1 }
            .mapValues { (_, s) -> s.sumOf { it.minutes } }
            .entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
            ?.let { (index, minutes) -> RewindMonth(index, mesLabel(index), minutes) }

        // Plataforma principal: la que acumula más minutos del año (los minutos de cada
        // juego se atribuyen a la plataforma de su ficha).
        val mainPlatform = minutesByGame.entries
            .mapNotNull { (gameId, minutes) ->
                entriesById[gameId]?.lastPlatform?.takeIf { p -> p.isNotBlank() }?.let { it to minutes }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, m) -> m.sum() }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()?.key
            ?: entriesOfYear.mapNotNull { it.lastPlatform?.takeIf { p -> p.isNotBlank() } }
                .groupingBy { it }.eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .firstOrNull()?.key

        val streak = longestStreakDays(sessions, year)
        val hasActivity = minutesTotal > 0 || gamesPlayed > 0 || gamesCompleted > 0

        val highlights = buildHighlights(
            minutesTotal = minutesTotal,
            gamesPlayed = gamesPlayed,
            gamesCompleted = gamesCompleted,
            topGame = topGames.firstOrNull(),
            topGenre = topGenres.firstOrNull(),
            streak = streak,
            busiestMonth = busiestMonth,
            mainPlatform = mainPlatform,
            hasActivity = hasActivity
        )

        return RewindData(
            year = year,
            minutesTotal = minutesTotal,
            gamesPlayed = gamesPlayed,
            gamesCompleted = gamesCompleted,
            topGame = topGames.firstOrNull(),
            topGames = topGames,
            topGenres = topGenres,
            ratingAverage = ratingAverage,
            ratedCount = rated.size,
            longestStreakDays = streak,
            busiestMonth = busiestMonth,
            mainPlatform = mainPlatform,
            hasActivity = hasActivity,
            headline = headline(year, hasActivity),
            highlights = highlights
        )
    }

    /** Titular del recap. Nunca culpabiliza: un año flojo tiene su propia frase. */
    internal fun headline(year: Int, hasActivity: Boolean): String =
        if (hasActivity) "Tu $year en juegos" else "Tu $year, a tu ritmo"

    /**
     * Líneas del recap. **Siempre al menos una**: es la garantía de CA3.5 (el caso
     * de poca actividad no puede quedar en una pantalla vacía).
     */
    internal fun buildHighlights(
        minutesTotal: Int,
        gamesPlayed: Int,
        gamesCompleted: Int,
        topGame: RewindGame?,
        topGenre: Pair<String, Int>?,
        streak: Int,
        busiestMonth: RewindMonth?,
        mainPlatform: String?,
        hasActivity: Boolean
    ): List<String> = buildList {
        if (minutesTotal > 0) add("${formatHoursTotal(minutesTotal)} jugadas")
        if (gamesPlayed > 0) {
            add(if (gamesPlayed == 1) "1 juego en marcha" else "$gamesPlayed juegos en marcha")
        }
        if (gamesCompleted > 0) {
            add(if (gamesCompleted == 1) "1 juego terminado" else "$gamesCompleted juegos terminados")
        }
        topGame?.let { add("Tu juego del año: ${it.name} (${formatHoursTotal(it.minutes)})") }
        topGenre?.let { add("Tu género dominante: ${it.first}") }
        if (streak > 1) add("Racha más larga: $streak días seguidos")
        busiestMonth?.let { add("Tu mes más activo: ${it.label}") }
        mainPlatform?.let { add("Plataforma principal: $it") }
        // Suelo del recap: si no hay ningún dato, sigue habiendo algo que decir (D3.6).
        if (isEmpty()) add("Tu biblioteca te está esperando")
    }

    private fun mesLabel(index: Int): String =
        MESES_ES.getOrElse(index) { "" }.replaceFirstChar { it.uppercase() }
}
