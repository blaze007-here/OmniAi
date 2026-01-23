package com.omniai.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiService {

    //  Supabase Edge Function URL
    private const val SUPABASE_FUNCTION_URL =
        "https://lhiziddurpovduccvieb.supabase.co/functions/v1/gemini-chat"

    // (Supabase anon key)
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxoaXppZGR1cnBvdmR1Y2N2aWViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg2NTM1NjIsImV4cCI6MjA4NDIyOTU2Mn0.GLi1wegP8R1vBfcj_K5_-afdsbJHmDJYHVyENMc6Kik"

    suspend fun sendMessage(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(SUPABASE_FUNCTION_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")

                // ✅ REQUIRED AUTH HEADERS
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $SUPABASE_ANON_KEY"
                )
                connection.setRequestProperty(
                    "apikey",
                    SUPABASE_ANON_KEY
                )

                connection.doOutput = true
                connection.doInput = true

                val body = JSONObject().apply {
                    put("prompt", prompt)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(body.toString())
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
                throw Exception("Failed to communicate with AI: ${e.message}")
            }
        }
    }
}
