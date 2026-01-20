package com.omniai.app.data.api

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiVisionService {
    private const val API_KEY = "API" // Same as GeminiService
    // Use the stable 2.5 Flash model on the v1beta endpoint
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    /**
     * Solve homework problem from an image
     */
    suspend fun solveFromImage(
        bitmap: Bitmap,
        subject: String,
        additionalContext: String = "",
        showSteps: Boolean = true
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // Convert bitmap to base64
                val base64Image = bitmapToBase64(bitmap)

                val url = URL("$API_URL?key=$API_KEY")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.doInput = true

                // Build prompt
                val prompt = buildImagePrompt(subject, additionalContext, showSteps)

                // Build request with image
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                // Add text prompt
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                                // Add image
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.4)
                        put("maxOutputTokens", 2048)
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

                    // Parse response
                    val jsonResponse = JSONObject(response)
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0).getJSONObject("content")
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
                throw Exception("Failed to solve problem: ${e.message}")
            }
        }
    }

    /**
     * Solve homework problem from text
     */
    suspend fun solveFromText(
        question: String,
        subject: String,
        showSteps: Boolean = true
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildTextPrompt(question, subject, showSteps)

                val url = URL("$API_URL?key=$API_KEY")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.doInput = true

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
                        put("temperature", 0.4)
                        put("maxOutputTokens", 2048)
                    })
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val jsonResponse = JSONObject(response)
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0).getJSONObject("content")
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
                throw Exception("Failed to solve problem: ${e.message}")
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun buildImagePrompt(subject: String, additionalContext: String, showSteps: Boolean): String {
        val stepsInstruction = if (showSteps) {
            "Provide a detailed step-by-step solution. Explain each step clearly."
        } else {
            "Provide the final answer with a brief explanation."
        }

        val contextPart = if (additionalContext.isNotBlank()) {
            "\n\nAdditional context: $additionalContext"
        } else {
            ""
        }

        return """
            You are a helpful homework tutor specializing in $subject.
            
            Analyze the problem shown in the image and solve it.
            
            $stepsInstruction
            
            If it's a math problem, show all calculations.
            If it's a coding problem, explain the logic and provide code if needed.
            If it's a science problem, explain the concepts involved.
            $contextPart
            
            Format your response clearly with:
            1. Problem Understanding
            2. Solution Steps (if requested)
            3. Final Answer
            4. Key Concepts (if applicable)
        """.trimIndent()
    }

    private fun buildTextPrompt(question: String, subject: String, showSteps: Boolean): String {
        val stepsInstruction = if (showSteps) {
            "Provide a detailed step-by-step solution. Explain each step clearly."
        } else {
            "Provide the final answer with a brief explanation."
        }

        return """
            You are a helpful homework tutor specializing in $subject.
            
            Question: $question
            
            $stepsInstruction
            
            If it's a math problem, show all calculations.
            If it's a coding problem, explain the logic and provide code if needed.
            If it's a science problem, explain the concepts involved.
            
            Format your response clearly with:
            1. Problem Understanding
            2. Solution Steps (if requested)
            3. Final Answer
            4. Key Concepts (if applicable)
        """.trimIndent()
    }
}