package es.androidtfm.gamevision.retrofit

import kotlinx.serialization.Serializable

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: Modelos de la API de NewsAPI.
 *
 * Todos los campos tienen valor por defecto para que el parseo sea tolerante
 * con respuestas que omiten claves (comportamiento equivalente al de Gson).
 */

@Serializable
data class New(
    val status: String = "",
    val totalResults: Long = 0,
    val articles: List<Article> = emptyList() // Lista de artículos
)

@Serializable
data class Article(
    val source: Source = Source(),
    val author: String? = null,
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val urlToImage: String? = null,
    val publishedAt: String = "",
    val content: String = ""
)

@Serializable
data class Source(
    val id: String? = null,
    val name: String = ""
)
