package com.omniai.app.ui.screens.art

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.omniai.app.data.api.ImageGenerationService
import kotlinx.coroutines.launch

enum class ArtStyle {
    ANIME, REALISTIC, ABSTRACT, FANTASY, CYBERPUNK,
    WATERCOLOR, OIL_PAINTING, PIXEL_ART, MINIMALIST, SURREAL
}

enum class ArtMood {
    VIBRANT, DARK, PEACEFUL, ENERGETIC, DREAMY, MYSTERIOUS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtScreen(onNavigateBack: () -> Unit) {
    var userIdea by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(ArtStyle.ANIME) }
    var selectedMood by remember { mutableStateOf(ArtMood.VIBRANT) }
    var generatedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generationHistory by remember { mutableStateOf(listOf<Pair<String, Bitmap>>()) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Art Generator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userIdea = ""
                            generatedImage = null
                            errorMessage = null
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create stunning AI-generated images for free!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Your Idea Input
            Text(
                text = "Describe your image",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = userIdea,
                onValueChange = { userIdea = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text("Example: A magical forest with glowing mushrooms and fireflies")
                },
                enabled = !isLoading
            )

            // Style Selection
            Text(
                text = "Choose Art Style",
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip("Anime", selectedStyle == ArtStyle.ANIME) {
                        selectedStyle = ArtStyle.ANIME
                    }
                    StyleChip("Realistic", selectedStyle == ArtStyle.REALISTIC) {
                        selectedStyle = ArtStyle.REALISTIC
                    }
                    StyleChip("Abstract", selectedStyle == ArtStyle.ABSTRACT) {
                        selectedStyle = ArtStyle.ABSTRACT
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip("Fantasy", selectedStyle == ArtStyle.FANTASY) {
                        selectedStyle = ArtStyle.FANTASY
                    }
                    StyleChip("Cyberpunk", selectedStyle == ArtStyle.CYBERPUNK) {
                        selectedStyle = ArtStyle.CYBERPUNK
                    }
                    StyleChip("Watercolor", selectedStyle == ArtStyle.WATERCOLOR) {
                        selectedStyle = ArtStyle.WATERCOLOR
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip("Oil Paint", selectedStyle == ArtStyle.OIL_PAINTING) {
                        selectedStyle = ArtStyle.OIL_PAINTING
                    }
                    StyleChip("Pixel Art", selectedStyle == ArtStyle.PIXEL_ART) {
                        selectedStyle = ArtStyle.PIXEL_ART
                    }
                    StyleChip("Minimal", selectedStyle == ArtStyle.MINIMALIST) {
                        selectedStyle = ArtStyle.MINIMALIST
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip("Surreal", selectedStyle == ArtStyle.SURREAL) {
                        selectedStyle = ArtStyle.SURREAL
                    }
                }
            }

            // Mood Selection
            Text(
                text = "Choose Mood",
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodChip("🌈 Vibrant", selectedMood == ArtMood.VIBRANT) {
                        selectedMood = ArtMood.VIBRANT
                    }
                    MoodChip("🌑 Dark", selectedMood == ArtMood.DARK) {
                        selectedMood = ArtMood.DARK
                    }
                    MoodChip("☮️ Peaceful", selectedMood == ArtMood.PEACEFUL) {
                        selectedMood = ArtMood.PEACEFUL
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodChip("⚡ Energetic", selectedMood == ArtMood.ENERGETIC) {
                        selectedMood = ArtMood.ENERGETIC
                    }
                    MoodChip("✨ Dreamy", selectedMood == ArtMood.DREAMY) {
                        selectedMood = ArtMood.DREAMY
                    }
                    MoodChip("🔮 Mysterious", selectedMood == ArtMood.MYSTERIOUS) {
                        selectedMood = ArtMood.MYSTERIOUS
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val bitmap = ImageGenerationService.generateImage(
                                prompt = userIdea,
                                style = selectedStyle,
                                mood = selectedMood
                            )
                            generatedImage = bitmap

                            // Add to history
                            if (bitmap != null) {
                                generationHistory = listOf(Pair(userIdea, bitmap)) + generationHistory.take(4)
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to generate image"
                            generatedImage = null
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && userIdea.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating art...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎨 Generate Image")
                }
            }

            // Error Message
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Generated Image Display
            generatedImage?.let { bitmap ->
                Text(
                    text = "Your AI Art",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Download functionality
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = {
                            // Share functionality
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            }

            // Generation History
            if (generationHistory.isNotEmpty()) {
                Text(
                    text = "Recent Creations",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    generationHistory.forEach { (prompt, bitmap) ->
                        HistoryCard(prompt, bitmap)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.StyleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun RowScope.MoodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun HistoryCard(prompt: String, bitmap: Bitmap) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Previous generation",
                modifier = Modifier
                    .size(60.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = prompt.take(50) + if (prompt.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}