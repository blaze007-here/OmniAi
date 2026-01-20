package com.omniai.app.data.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.omniai.app.ui.screens.art.ArtMood
import com.omniai.app.ui.screens.art.ArtStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ImageGenerationService {
    // Using Pollinations.ai - FREE image generation API (no key needed!)
    private const val API_URL = "https://image.pollinations.ai/prompt/"

    /**
     * Generate image using Pollinations.ai FREE API
     */
    suspend fun generateImage(
        prompt: String,
        style: ArtStyle,
        mood: ArtMood
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Build enhanced prompt
                val enhancedPrompt = buildEnhancedPrompt(prompt, style, mood)

                // Pollinations.ai accepts prompt in URL
                val encodedPrompt = java.net.URLEncoder.encode(enhancedPrompt, "UTF-8")
                val imageUrl = "$API_URL$encodedPrompt?width=512&height=512&nologo=true"

                // Download image
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                bitmap
            } catch (e: Exception) {
                throw Exception("Failed to generate image: ${e.message}")
            }
        }
    }

    private fun buildEnhancedPrompt(
        userPrompt: String,
        style: ArtStyle,
        mood: ArtMood
    ): String {
        val styleName = when (style) {
            ArtStyle.ANIME -> "anime style, manga illustration"
            ArtStyle.REALISTIC -> "photorealistic, highly detailed, 8k"
            ArtStyle.ABSTRACT -> "abstract art, geometric shapes, modern"
            ArtStyle.FANTASY -> "fantasy art, magical, ethereal"
            ArtStyle.CYBERPUNK -> "cyberpunk style, neon lights, futuristic"
            ArtStyle.WATERCOLOR -> "watercolor painting, soft colors"
            ArtStyle.OIL_PAINTING -> "oil painting, classical art style"
            ArtStyle.PIXEL_ART -> "pixel art, 16-bit, retro game style"
            ArtStyle.MINIMALIST -> "minimalist design, simple, clean"
            ArtStyle.SURREAL -> "surreal art, dreamlike, Salvador Dali style"
        }

        val moodName = when (mood) {
            ArtMood.VIBRANT -> "vibrant colors, bright, cheerful"
            ArtMood.DARK -> "dark atmosphere, moody lighting, dramatic"
            ArtMood.PEACEFUL -> "peaceful, serene, calm atmosphere"
            ArtMood.ENERGETIC -> "dynamic, energetic, action-packed"
            ArtMood.DREAMY -> "dreamy, soft lighting, magical atmosphere"
            ArtMood.MYSTERIOUS -> "mysterious, enigmatic, atmospheric"
        }

        return "$userPrompt, $styleName, $moodName, high quality, detailed"
    }
}