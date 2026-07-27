package com.omniai.app.ui.screens.homework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omniai.app.data.api.OllamaService
import com.omniai.app.AppConfig
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

enum class Subject {
    MATH, PHYSICS, CHEMISTRY, BIOLOGY, CODING, OTHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(onNavigateBack: () -> Unit) {
    var selectedSubject by remember { mutableStateOf(Subject.MATH) }
    var questionText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var solution by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSteps by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            selectedBitmap = uriToBitmap(context, it)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedBitmap = it
            selectedImageUri = null // Clear URI when using camera
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Homework Helper") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            questionText = ""
                            selectedImageUri = null
                            selectedBitmap = null
                            solution = ""
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
            // Subject Selection
            Text(
                text = "Select Subject",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectChip("Math", Icons.Default.Calculate, selectedSubject == Subject.MATH) {
                    selectedSubject = Subject.MATH
                }
                SubjectChip("Physics", Icons.Default.Science, selectedSubject == Subject.PHYSICS) {
                    selectedSubject = Subject.PHYSICS
                }
                SubjectChip("Chemistry", Icons.Default.Biotech, selectedSubject == Subject.CHEMISTRY) {
                    selectedSubject = Subject.CHEMISTRY
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectChip("Biology", Icons.Default.LocalFlorist, selectedSubject == Subject.BIOLOGY) {
                    selectedSubject = Subject.BIOLOGY
                }
                SubjectChip("Coding", Icons.Default.Code, selectedSubject == Subject.CODING) {
                    selectedSubject = Subject.CODING
                }
                SubjectChip("Other", Icons.Default.MoreHoriz, selectedSubject == Subject.OTHER) {
                    selectedSubject = Subject.OTHER
                }
            }

            // Image Input Section
            Text(
                text = "Add Problem (Photo or Text)",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }

                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }

            // Display selected image
            selectedBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected problem",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Text Input (optional, for typed questions)
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Or type your question") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading,
                placeholder = { Text("Example: Solve for x: 2x + 5 = 15") }
            )

            // Step-by-step toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showSteps,
                    onCheckedChange = { showSteps = it }
                )
                Text("Show step-by-step solution")
            }

            // Solve Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            if (selectedBitmap != null) {
                                solution = "Note: Using '${AppConfig.VISION_MODEL}' for image analysis. Attempting to solve..."
                            }

                            val ollamaService = OllamaService()

                            val systemPrompt = buildSystemPrompt(selectedSubject, showSteps)
                            val userPrompt = if (selectedBitmap != null && questionText.isNotBlank()) {
                                "Analyze the problem in the image. Additional context: $questionText"
                            } else if (selectedBitmap != null) {
                                "Analyze and solve the problem in the image."
                            } else {
                                questionText
                            }

                            if (userPrompt.isBlank() && selectedBitmap == null) {
                                solution = "Please provide a question (image or text)"
                            } else {
                                val images = selectedBitmap?.let { listOf(bitmapToBase64(it)) }
                                val response = ollamaService.generateResponse(
                                    userPrompt = userPrompt,
                                    imagesBase64 = images,
                                    systemPrompt = systemPrompt
                                )
                                
                                solution = if (selectedBitmap != null && response.contains("Error: Ollama server returned HTTP 404")) {
                                    "Error: Model '${AppConfig.VISION_MODEL}' not found. Please run 'ollama pull ${AppConfig.VISION_MODEL}"
                                } else {
                                    response
                                }
                            }
                        } catch (e: Exception) {
                            solution = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && (selectedBitmap != null || questionText.isNotBlank())
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solving locally...")
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎯 Get Solution")
                }
            }

            // Solution Display
            if (solution.isNotBlank()) {
                Text(
                    text = "Solution",
                    style = MaterialTheme.typography.titleMedium
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = solution,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { /* Copy functionality */ }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
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
fun RowScope.SubjectChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.weight(1f)
    )
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

fun buildSystemPrompt(subject: Subject, showSteps: Boolean): String {
    val stepsInstruction = if (showSteps) {
        "Provide a detailed step-by-step solution. Explain each step clearly."
    } else {
        "Provide the final answer with a brief explanation."
    }

    return """
        You are a helpful homework tutor specializing in ${subject.name}.
        
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
