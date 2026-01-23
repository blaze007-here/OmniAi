package com.omniai.app.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseService {
    private const val SUPABASE_URL = "https://lhiziddurpovduccvieb.supabase.co"
    private const val SUPABASE_KEY = "API"// supabase api key here or annon keey

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    fun initialize() {
        // Client is initialized lazily
    }

    /**
     * Sign up a new user with email and password
     */
    suspend fun signUp(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign in an existing user
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signOut()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return client.auth.currentUserOrNull() != null
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return client.auth.currentUserOrNull()?.email
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }
}
