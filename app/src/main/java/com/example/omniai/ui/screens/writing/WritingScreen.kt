package com.omniai.app.ui.screens.writing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omniai.app.data.api.GeminiService
import kotlinx.coroutines.launch

enum class WritingMode {
    REWRITE, SUMMARIZE, EXPAND, GRAMMAR, TONE
}

enum class ToneType {
    FORMAL, CASUAL, PROFESSIONAL, FRIENDLY, ACADEMIC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(onNavigateBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(WritingMode.REWRITE) }
    var selectedTone by remember { mutableStateOf(ToneType.FORMAL) }
    var isLoading by remember { mutableStateOf(false) }
    var showToneSelector by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Writing Assistant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            inputText = ""
                            outputText = ""
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
            // Mode Selection
            Text(
                text = "Choose Mode",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    label = "Rewrite",
                    icon = Icons.Default.Edit,
                    selected = selectedMode == WritingMode.REWRITE,
                    onClick = { selectedMode = WritingMode.REWRITE }
                )
                ModeChip(
                    label = "Summarize",
                    icon = Icons.Default.ShortText,
                    selected = selectedMode == WritingMode.SUMMARIZE,
                    onClick = { selectedMode = WritingMode.SUMMARIZE }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    label = "Expand",
                    icon = Icons.Default.UnfoldMore,
                    selected = selectedMode == WritingMode.EXPAND,
                    onClick = { selectedMode = WritingMode.EXPAND }
                )
                ModeChip(
                    label = "Grammar",
                    icon = Icons.Default.Check,
                    selected = selectedMode == WritingMode.GRAMMAR,
                    onClick = { selectedMode = WritingMode.GRAMMAR }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    label = "Change Tone",
                    icon = Icons.Default.Mood,
                    selected = selectedMode == WritingMode.TONE,
                    onClick = {
                        selectedMode = WritingMode.TONE
                        showToneSelector = true
                    }
                )
            }

            // Tone Selector (only shown for Tone mode)
            if (selectedMode == WritingMode.TONE) {
                Text(
                    text = "Select Tone",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToneChip("Formal", selectedTone == ToneType.FORMAL) {
                        selectedTone = ToneType.FORMAL
                    }
                    ToneChip("Casual", selectedTone == ToneType.CASUAL) {
                        selectedTone = ToneType.CASUAL
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToneChip("Professional", selectedTone == ToneType.PROFESSIONAL) {
                        selectedTone = ToneType.PROFESSIONAL
                    }
                    ToneChip("Friendly", selectedTone == ToneType.FRIENDLY) {
                        selectedTone = ToneType.FRIENDLY
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToneChip("Academic", selectedTone == ToneType.ACADEMIC) {
                        selectedTone = ToneType.ACADEMIC
                    }
                }
            }

            // Input Text
            Text(
                text = "Your Text",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Paste or type your text here...") },
                enabled = !isLoading
            )

            // Process Button
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            try {
                                val prompt = buildPrompt(selectedMode, selectedTone, inputText)
                                val response = GeminiService.sendMessage(
                                    listOf(mapOf("role" to "user", "content" to prompt))
                                )
                                outputText = response
                            } catch (e: Exception) {
                                outputText = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Processing..." else "✨ ${getModeLabel(selectedMode)}")
            }

            // Output Text
            if (outputText.isNotBlank()) {
                Text(
                    text = "Result",
                    style = MaterialTheme.typography.titleMedium
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = outputText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    // Copy to clipboard functionality
                                    // You can add clipboard manager here
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ModeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun RowScope.ToneChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.weight(1f)
    )
}

fun buildPrompt(mode: WritingMode, tone: ToneType, text: String): String {
    return when (mode) {
        WritingMode.REWRITE -> "Rewrite the following text to make it better while keeping the same meaning:\n\n$text"
        WritingMode.SUMMARIZE -> "Summarize the following text concisely:\n\n$text"
        WritingMode.EXPAND -> "Expand the following text with more details and explanations:\n\n$text"
        WritingMode.GRAMMAR -> "Fix all grammar, spelling, and punctuation errors in the following text. Only return the corrected text:\n\n$text"
        WritingMode.TONE -> "Rewrite the following text in a ${tone.name.lowercase()} tone:\n\n$text"
    }
}

fun getModeLabel(mode: WritingMode): String {
    return when (mode) {
        WritingMode.REWRITE -> "Rewrite"
        WritingMode.SUMMARIZE -> "Summarize"
        WritingMode.EXPAND -> "Expand"
        WritingMode.GRAMMAR -> "Fix Grammar"
        WritingMode.TONE -> "Change Tone"
    }
}