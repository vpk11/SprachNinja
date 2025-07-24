package com.vpk.sprachninja.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vpk.sprachninja.data.local.AppDatabase
import com.vpk.sprachninja.data.remote.GeminiApiService
import com.vpk.sprachninja.data.repository.GeminiRepositoryImpl
import com.vpk.sprachninja.data.repository.SettingsRepositoryImpl
import com.vpk.sprachninja.data.repository.UserRepositoryImpl
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.SettingsRepository
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

/**
 * A manual dependency injection container that is created in the Application class.
 * It holds and provides instances of repositories, data sources, and use cases.
 */
class AppContainer(private val context: Context) {

    // --- Database and Local Repositories ---

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(appDatabase.userDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    // --- Networking ---

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val geminiApiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    // --- Public Repositories that use Networking ---

    /**
     * A public lazy-initialized property for the GeminiRepository.
     * This provides a clean abstraction over the Gemini API calls.
     */
    val geminiRepository: GeminiRepository by lazy {
        GeminiRepositoryImpl(geminiApiService, settingsRepository)
    }
}