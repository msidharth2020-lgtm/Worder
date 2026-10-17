package com.example.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String
)

object TranslationService {

    val supportedLanguages: List<LanguageOption> = listOf(
        LanguageOption("es", "Spanish", "Español"),
        LanguageOption("fr", "French", "Français"),
        LanguageOption("de", "German", "Deutsch"),
        LanguageOption("it", "Italian", "Italiano"),
        LanguageOption("pt", "Portuguese", "Português"),
        LanguageOption("ja", "Japanese", "日本語"),
        LanguageOption("zh-CN", "Chinese (Simplified)", "简体中文"),
        LanguageOption("ko", "Korean", "한국어"),
        LanguageOption("hi", "Hindi", "हिन्दी"),
        LanguageOption("ar", "Arabic", "العربية"),
        LanguageOption("ru", "Russian", "Русский"),
        LanguageOption("tr", "Turkish", "Türkçe"),
        LanguageOption("vi", "Vietnamese", "Tiếng Việt"),
        LanguageOption("pl", "Polish", "Polski"),
        LanguageOption("nl", "Dutch", "Nederlands"),
        LanguageOption("uk", "Ukrainian", "Українська"),
        LanguageOption("sv", "Swedish", "Svenska"),
        LanguageOption("el", "Greek", "Ελληνικά"),
        LanguageOption("en", "English", "English")
    )

    suspend fun translateText(
        text: String,
        sourceLang: String = "auto",
        targetLang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success(text)
        if (sourceLang.equals(targetLang, ignoreCase = true)) return@withContext Result.success(text)

        // 1. Primary: Google Translate GTX API endpoint
        val googleRes = translateViaGoogleTranslateApi(text, sourceLang, targetLang)
        if (googleRes.isSuccess) {
            return@withContext googleRes
        }

        // 2. Secondary: Gemini 3.5 Flash if a valid API key is present
        val geminiKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("")
        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY" && geminiKey != "your_api_key_here") {
            val geminiRes = translateViaGemini(text, targetLang, geminiKey)
            if (geminiRes.isSuccess) {
                return@withContext geminiRes
            }
        }

        // 3. Fallback: MyMemory Neural Machine Translation REST API
        translateViaNeuralRest(text, sourceLang, targetLang)
    }

    private fun translateViaGoogleTranslateApi(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            val sl = if (sourceLang.isBlank()) "auto" else sourceLang
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$targetLang&dt=t&q=$encodedText"
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            }

            val status = conn.responseCode
            if (status in 200..299) {
                val responseStr = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                val rootArray = JSONArray(responseStr)
                val sentencesArray = rootArray.optJSONArray(0)
                if (sentencesArray != null) {
                    val sb = StringBuilder()
                    for (i in 0 until sentencesArray.length()) {
                        val segment = sentencesArray.optJSONArray(i)
                        val piece = segment?.optString(0)
                        if (!piece.isNullOrEmpty() && piece != "null") {
                            sb.append(piece)
                        }
                    }
                    val finalResult = sb.toString().trim()
                    if (finalResult.isNotEmpty()) {
                        return Result.success(finalResult)
                    }
                }
                Result.failure(Exception("Empty Google Translate response"))
            } else {
                Result.failure(Exception("Google Translate HTTP $status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun translateViaGemini(
        text: String,
        targetLangCode: String,
        apiKey: String
    ): Result<String> {
        return try {
            val targetName = supportedLanguages.find { it.code == targetLangCode }?.name ?: targetLangCode
            val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val prompt = "Translate the following text accurately into $targetName ($targetLangCode). Return ONLY the translated text without quotes or explanations:\n\n$text"
            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", prompt))
                        )
                    )
                )
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val status = conn.responseCode
            if (status in 200..299) {
                val responseStr = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                val json = JSONObject(responseStr)
                val translated = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                if (!translated.isNullOrBlank()) {
                    Result.success(translated)
                } else {
                    Result.failure(Exception("Empty Gemini translation response"))
                }
            } else {
                Result.failure(Exception("Gemini HTTP $status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun translateViaNeuralRest(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            val chunks = splitIntoChunks(text, 420)
            val translatedChunks = mutableListOf<String>()

            val src = if (sourceLang == "auto" || sourceLang.isBlank()) "en" else sourceLang
            for (chunk in chunks) {
                val encodedQuery = URLEncoder.encode(chunk, "UTF-8")
                val langPair = URLEncoder.encode("$src|$targetLang", "UTF-8")
                val url = URL("https://api.mymemory.translated.net/get?q=$encodedQuery&langpair=$langPair")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", "WordCraft-Android/1.0")
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val body = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                    val json = JSONObject(body)
                    val responseData = json.optJSONObject("responseData")
                    val translated = responseData?.optString("translatedText")
                    if (!translated.isNullOrBlank()) {
                        translatedChunks.add(decodeHtmlEntities(translated))
                    } else {
                        return Result.failure(Exception("Translation service returned empty result"))
                    }
                } else {
                    return Result.failure(Exception("Translation HTTP status $code"))
                }
            }
            Result.success(translatedChunks.joinToString(" "))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun splitIntoChunks(text: String, maxLen: Int): List<String> {
        if (text.length <= maxLen) return listOf(text)
        val result = mutableListOf<String>()
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        var current = StringBuilder()
        for (sentence in sentences) {
            if (current.length + sentence.length + 1 <= maxLen) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(sentence)
            } else {
                if (current.isNotEmpty()) {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                if (sentence.length <= maxLen) {
                    current.append(sentence)
                } else {
                    var idx = 0
                    while (idx < sentence.length) {
                        val end = (idx + maxLen).coerceAtMost(sentence.length)
                        result.add(sentence.substring(idx, end))
                        idx = end
                    }
                }
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    /**
     * Launches the Google Translate Android app (if installed) or opens Google Translate Web
     * pre-populated with the text and target language.
     */
    fun openInGoogleTranslate(context: Context, text: String, targetLangCode: String) {
        val webUri = Uri.parse(
            "https://translate.google.com/?sl=auto&tl=${Uri.encode(targetLangCode)}&text=${Uri.encode(text)}&op=translate"
        )
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
    }
}
