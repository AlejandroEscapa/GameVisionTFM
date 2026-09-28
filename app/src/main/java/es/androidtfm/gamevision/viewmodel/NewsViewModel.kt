package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.BuildConfig
import es.androidtfm.gamevision.retrofit.Article
import es.androidtfm.gamevision.retrofit.NewsApiService
import es.androidtfm.gamevision.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

/**
 * ViewModel para la búsqueda de noticias
 *
 * Contiene todos los metodos con los que se interactua con la API de noticias.
 */

@HiltViewModel
class NewsViewModel @Inject constructor(
    // Inyectable para pruebas; Hilt provee la instancia Retrofit de la aplicación
    private val newsApi: NewsApiService
) : ViewModel() {

    private companion object {
        const val TAG = "NewsViewModel"
    }

    /**
     * Constructor sin argumentos para previews y usos manuales (Hilt usa el primario).
     */
    constructor() : this(RetrofitInstance.newsApi)

    // Clave de la API para acceder al servicio de noticias (definida en local.properties, fuera del control de versiones)
    private val apiKey = BuildConfig.NEWS_API_KEY

    /**
     * Indica si la última carga falló (p. ej. sin conexión). La UI lo usa para
     * mostrar un aviso en lugar de quedarse cargando para siempre (F0/modo degradado).
     */
    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed

    /**
     * Obtiene noticias filtradas según una consulta.
     * @param query: Término de búsqueda para filtrar las noticias.
     * @return Lista de artículos filtrados y ordenados. Lista vacía si falla la red.
     */
    suspend fun fetchFilteredNews(query: String): List<Article> =
        runCatching { newsApi.getEverything(query, apiKey) }
            .fold(
                onSuccess = { response ->
                    _loadFailed.value = false
                    // Filtra las noticias sin imagen o marcadas como "[Removed]"
                    response.articles.filter {
                        !it.title.contains("[Removed]", ignoreCase = true) &&
                            !it.urlToImage.isNullOrEmpty()
                    }.sortedByDescending { it.publishedAt } // Ordena por fecha descendente
                        .take(25) // Limita el resultado a 25 artículos
                },
                onFailure = { error ->
                    // Modo degradado: sin red no se cae la app; se marca el fallo y
                    // se devuelve lista vacía para que la UI avise al usuario.
                    Log.w(TAG, "No se pudieron cargar las noticias: ${error.message}")
                    _loadFailed.value = true
                    emptyList()
                }
            )

    /**
     * Formatea la fecha de publicación de un artículo.
     * @param dateString: Cadena de fecha en formato ISO 8601 (ej. "2023-10-05T14:30:00Z").
     * @return Cadena de fecha formateada (ej. "14:30 05-10-2023").
     */
    fun formatPublishedAt(dateString: String): String {
        // Define el formato de entrada (ISO 8601)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        // Define el formato de salida (HH:mm dd-MM-yyyy)
        val outputFormat = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())

        return try {
            // Convierte la cadena de fecha a un objeto Date
            val date: Date = inputFormat.parse(dateString) ?: return ""
            // Formatea la fecha al nuevo formato y devuelve la cadena
            outputFormat.format(date)
        } catch (e: Exception) {
            // Maneja la excepción si la cadena no es válida
            ""
        }
    }
}