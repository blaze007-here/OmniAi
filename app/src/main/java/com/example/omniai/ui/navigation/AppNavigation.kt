package com.omniai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.omniai.app.data.supabase.SupabaseService
import com.omniai.app.ui.screens.auth.LoginScreen
import com.omniai.app.ui.screens.auth.SignUpScreen
import com.omniai.app.ui.screens.auth.LoginScreen
import com.omniai.app.ui.screens.auth.SignUpScreen
import com.omniai.app.ui.screens.home.HomeScreen
import com.omniai.app.ui.screens.chat.ChatScreen
import com.omniai.app.ui.screens.writing.WritingScreen
import com.omniai.app.ui.screens.homework.HomeworkScreen
import com.omniai.app.ui.screens.art.ArtScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Writing : Screen("writing")
    object Homework : Screen("homework")
    object Art : Screen("art")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = if (SupabaseService.isAuthenticated()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToWriting = {
                    navController.navigate(Screen.Writing.route)
                },
                onNavigateToHomework = {
                    navController.navigate(Screen.Homework.route)
                },
                onNavigateToArt = {
                    navController.navigate(Screen.Art.route)
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Writing.route) {
            WritingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Homework.route) {
            HomeworkScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Art.route) {
            ArtScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}