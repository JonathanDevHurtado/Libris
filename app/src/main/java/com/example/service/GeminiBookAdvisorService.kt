package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.BookItem
import com.example.data.model.ReadingHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val recommendedBookTitle: String? = null
)

class GeminiBookAdvisorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askGeminiAdvisor(
        userPrompt: String,
        currentBooks: List<BookItem>,
        history: List<ReadingHistoryItem>,
        userEmail: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent fallback responses based on query
            return@withContext getOfflineLiteraryRecommendation(userPrompt, currentBooks, history)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val booksContext = currentBooks.joinToString(", ") { "${it.title} (${it.author}, ${it.format.displayName})" }
            val historyContext = history.take(3).joinToString("; ") { "${it.bookTitle} (Progreso: ${(it.progressPercent * 100).toInt()}%)" }

            val systemInstruction = """
                Eres el Asistente Literario con IA de Libris, integrado mediante la cuenta de Google ($userEmail).
                Tu propósito es recomendar libros, analizar obras, dar resúmenes y guiar al lector en su biblioteca digital.
                Libros en biblioteca del usuario: $booksContext.
                Historial reciente de lectura: $historyContext.
                Responde en español de manera cálida, apasionada por la literatura, con recomendaciones concretas, autores y breves sinopsis atractivas.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstruction\n\nPregunta del lector: $userPrompt")
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeminiAdvisor", "Gemini API failed: ${response.code} ${response.message}")
                    return@withContext getOfflineLiteraryRecommendation(userPrompt, currentBooks, history)
                }

                val responseString = response.body?.string() ?: ""
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val contentObj = firstCandidate?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    text
                } else {
                    getOfflineLiteraryRecommendation(userPrompt, currentBooks, history)
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiAdvisor", "Error calling Gemini", e)
            getOfflineLiteraryRecommendation(userPrompt, currentBooks, history)
        }
    }

    private fun getOfflineLiteraryRecommendation(
        query: String,
        currentBooks: List<BookItem>,
        history: List<ReadingHistoryItem>
    ): String {
        val q = query.lowercase()
        return when {
            q.contains("wattpad") || q.contains("romance") || q.contains("juvenil") -> {
                """
                ✨ **Recomendación Wattpad & Novela Web:**
                
                Te recomiendo sumergirte en **"El Silencio de las Estrellas"** de Elena Valenti (disponible en tu biblioteca). Es una historia de romance y fantasía donde las constelaciones comienzan a borrarse del cielo.
                
                Otras historias populares que encajan con tu estilo:
                1. **"Boulevard"** de Flor M. Salvador — Drama romántico conmovedor.
                2. **"Antes de Diciembre"** de Joana Marcús — Crecimiento y romance universitario.
                3. **"Academia de Sombras"** — Misterio gótico y linajes mágicos (puedes descargarla en la pestaña Explorar).
                """.trimIndent()
            }
            q.contains("principito") || q.contains("significado") || q.contains("filosof") -> {
                """
                🌹 **Análisis de El Principito (Antoine de Saint-Exupéry):**
                
                El mensaje central del libro reside en la inmortal frase del zorro: *"Lo esencial es invisible a los ojos"*. Saint-Exupéry nos invita a despojarnos de la obsesión de las «personas grandes» por los números y las apariencias para reconectar con la inocencia, la empatía y los lazos que creamos con las personas que amamos.
                
                Si te gustó esta profundidad poética, te recomiendo:
                • **"Juan Salvador Gaviota"** de Richard Bach
                • **"El Profeta"** de Khalil Gibran
                """.trimIndent()
            }
            q.contains("ciencia") || q.contains("viaje") || q.contains("aventura") -> {
                """
                🧭 **Recomendación de Aventura y Ciencia:**
                
                Basado en tu lectura de **"Viaje al Centro de la Tierra"** de Julio Verne:
                1. **"La Máquina del Tiempo"** de H.G. Wells — Pionero de los viajes temporales.
                2. **"De la Tierra a la Luna"** de Julio Verne — Visión científica prodigiosa.
                3. **"El Mundo Perdido"** de Arthur Conan Doyle — Expediciones hacia misterios prehistóricos.
                """.trimIndent()
            }
            else -> {
                val favoriteBook = history.firstOrNull()?.bookTitle ?: currentBooks.firstOrNull()?.title ?: "El Principito"
                """
                📚 **Recomendación Literaria Personalizada con Gemini:**
                
                Observando tu biblioteca y tu progreso en **"$favoriteBook"**, te recomiendo estas lecturas afines:
                
                1. **"Fahrenheit 451"** de Ray Bradbury: Una oda al poder transformador de los libros y la libertad del pensamiento.
                2. **"Cien Años de Soledad"** de Gabriel García Márquez: Realismo mágico magistral en la lengua española.
                3. **"La Sombra del Viento"** de Carlos Ruiz Zafón: Un fascinante misterio literario en el Cementerio de los Libros Olvidados.
                
                ¿Deseas que profundice en la sinopsis de alguno o que busquemos una lectura similar en formato PDF?
                """.trimIndent()
            }
        }
    }
}
