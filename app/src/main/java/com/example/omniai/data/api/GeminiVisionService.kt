package com.omniai.app.data.api

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiVisionService {

    // Supabase Edge Function URL (VISION)
    private const val SUPABASE_FUNCTION_URL =
        "https://lhiziddurpovduccvieb.supabase.co/functions/v1/gemini-vision"

    // Supabase anon key (safe to keep in app)
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxoaXppZGR1cnBvdmR1Y2N2aWViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg2NTM1NjIsImV4cCI6MjA4NDIyOTU2Mn0.GLi1wegP8R1vBfcj_K5_-afdsbJHmDJYHVyENMc6Kik"

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
                val base64Image = bitmapToBase64(bitmap)
                val prompt = buildImagePrompt(subject, additionalContext, showSteps)

                val url = URL(SUPABASE_FUNCTION_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $SUPABASE_ANON_KEY"
                )
                connection.setRequestProperty("apikey", SUPABASE_ANON_KEY)
                connection.doOutput = true
                connection.doInput = true

                val requestBody = JSONObject().apply {
                    put("prompt", prompt)
                    put("imageBase64", base64Image)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    )
                    val response = reader.readText()
                    reader.close()

                    JSONObject(response).getString("text")
                } else {
                    val errorReader = BufferedReader(
                        InputStreamReader(connection.errorStream ?: connection.inputStream)
                    )
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

                val url = URL(SUPABASE_FUNCTION_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $SUPABASE_ANON_KEY"
                )
                connection.setRequestProperty("apikey", SUPABASE_ANON_KEY)
                connection.doOutput = true
                connection.doInput = true

                val requestBody = JSONObject().apply {
                    put("prompt", prompt)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    )
                    val response = reader.readText()
                    reader.close()

                    JSONObject(response).getString("text")
                } else {
                    val errorReader = BufferedReader(
                        InputStreamReader(connection.errorStream ?: connection.inputStream)
                    )
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

    private fun buildImagePrompt(
        subject: String,
        additionalContext: String,
        showSteps: Boolean
    ): String {
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

    private fun buildTextPrompt(
        question: String,
        subject: String,
        showSteps: Boolean
    ): String {
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
