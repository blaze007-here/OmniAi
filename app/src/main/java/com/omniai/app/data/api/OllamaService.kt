package com.omniai.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.omniai.app.AppConfig

class OllamaService(
    private val hostIp: String = AppConfig.OLLAMA_HOST_IP,
    private var modelName: String = AppConfig.TEXT_MODEL
) {

    suspend fun generateResponse(
        userPrompt: String,
        imagesBase64: List<String>? = null,
        systemPrompt: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$hostIp:11434/api/chat")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000 // 15 seconds
            conn.readTimeout = 90000    // 90 seconds

            // Determine which model to use
            val activeModel = if (!imagesBase64.isNullOrEmpty()) {
                AppConfig.VISION_MODEL 
            } else {
                modelName
            }

            // Build Ollama Chat API JSON payload
            val jsonBody = JSONObject().apply {
                put("model", activeModel)
                put("stream", false)
                val messages = JSONArray().apply {
                    if (systemPrompt != null) {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                        if (!imagesBase64.isNullOrEmpty()) {
                            put("images", JSONArray(imagesBase64))
                        }
                    })
                }
                put("messages", messages)
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)
                return@withContext responseJson
                    .getJSONObject("message")
                    .getString("content")
            } else {
                val errorStream = conn.errorStream
                val errorDetails = if (errorStream != null) {
                    errorStream.bufferedReader().use { it.readText() }
                } else {
                    "No error details"
                }
                return@withContext "Ollama Error (HTTP $responseCode): $errorDetails"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Connection failed: ${e.message}\n\nIP: $hostIp\nModel: $modelName"
        }
    }
}
