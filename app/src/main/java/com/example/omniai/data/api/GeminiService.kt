package com.omniai.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiService {
    private const val API_KEY = "AIzaSyAghuA4o5So1QD7nVxMQ9g2Nx4N_OCKyB8" // Replace with your Gemini API key
    // Try the 2.5 version which is now the stable standard in the beta channel
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    suspend fun sendMessage(messages: List<Map<String, String>>): String {
        return withContext(Dispatchers.IO) {
            try {
                // Build conversation context for Gemini
                val conversationContext = messages.joinToString("\n\n") { msg ->
                    if (msg["role"] == "user") {
                        "User: ${msg["content"]}"
                    } else {
                        "Assistant: ${msg["content"]}"
                    }
                }

                val url = URL("$API_URL?key=$API_KEY")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.doInput = true

                // Get only the last user message
                val lastUserMessage = messages.lastOrNull { it["role"] == "user" }?.get("content") ?: ""

                // Build context-aware prompt
                val prompt = if (messages.size > 1) {
                    "Previous conversation:\n$conversationContext\n\nPlease respond to the latest message naturally, considering the conversation history."
                } else {
                    lastUserMessage
                }

                // Build request body for Gemini API
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1024)
                    })
                }

                // Send request
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody.toString())
                writer.flush()
                writer.close()

                // Read response
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Parse Gemini response
                    val jsonResponse = JSONObject(response)
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0)
                        .getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val text = parts.getJSONObject(0).getString("text")

                    text
                } else {
                    val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                    val errorResponse = errorReader.readText()
                    errorReader.close()

                    throw Exception("API Error ($responseCode): $errorResponse")
                }
            } catch (e: Exception) {
                throw Exception("Failed to communicate with AI: ${e.message}")
            }
        }
    }
}