package es.androidtfm.gamevision.data.catalog.local

import androidx.room.TypeConverter

/*
 * Conversores de tipo para Room (F0/T0.4).
 *
 * Room no sabe guardar List<String> / List<Int> en SQLite, así que se serializan
 * a una única cadena usando un separador de control (US, 0x1F) que no aparece en
 * nombres de juegos, géneros ni plataformas.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(SEP)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEP)

    @TypeConverter
    fun fromIntList(value: List<Int>): String =
        value.joinToString(SEP) { it.toString() }

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        if (value.isEmpty()) emptyList() else value.split(SEP).mapNotNull { it.toIntOrNull() }

    private companion object {
        /** Unidad de separación: no colisiona con texto normal. */
        const val SEP = "\u001F"
    }
}
