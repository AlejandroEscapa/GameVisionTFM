package es.androidtfm.gamevision.data.library

/*
 * Plataformas jugadas (F1/T1.6).
 *
 * Lista corta y ordenada por relevancia (lo más común primero) para el selector
 * de la partida. Es un dato de usuario, no del catálogo.
 */
object Platforms {
    val ALL: List<String> = listOf(
        "PC",
        "PlayStation 5",
        "PlayStation 4",
        "Xbox Series X|S",
        "Xbox One",
        "Nintendo Switch",
        "Nintendo Switch 2",
        "Steam Deck",
        "Android",
        "iOS",
        "Retro / Emulador",
        "Otra"
    )
}
