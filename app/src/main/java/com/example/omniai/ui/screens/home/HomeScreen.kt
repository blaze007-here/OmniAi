package com.omniai.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omniai.app.data.supabase.SupabaseService
import kotlinx.coroutines.launch

data class AIFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit, onNavigateToChat: () -> Unit, onNavigateToWriting: () -> Unit) {
    val scope = rememberCoroutineScope()
    val userEmail = SupabaseService.getCurrentUserEmail()

    val features = listOf(
        AIFeature(
            title = "Homework Helper",
            description = "Get step-by-step solutions",
            icon = Icons.Default.School,
            route = "homework"
        ),
        AIFeature(
            title = "Writing Assistant",
            description = "Improve your writing",
            icon = Icons.Default.Edit,
            route = "writing"
        ),
        AIFeature(
            title = "Chat Assistant",
            description = "Your productivity companion",
            icon = Icons.Default.Chat,
            route = "chat"
        ),
        AIFeature(
            title = "Art Generator",
            description = "Create stunning visuals",
            icon = Icons.Default.Palette,
            route = "art"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OmniAI") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                SupabaseService.signOut()
                                onLogout()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineSmall
            )

            userEmail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(features) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = {
                            when (feature.route) {
                                "chat" -> onNavigateToChat()
                                "writing" -> onNavigateToWriting()
                                else -> { /* Other features coming soon */ }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(feature: AIFeature, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}