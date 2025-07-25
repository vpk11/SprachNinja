package com.vpk.sprachninja.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vpk.sprachninja.data.local.AppDatabase
import com.vpk.sprachninja.data.remote.GeminiApiService
import com.vpk.sprachninja.data.repository.GeminiRepositoryImpl
import com.vpk.sprachninja.data.repository.LevelStatsRepositoryImpl
import com.vpk.sprachninja.data.repository.RecentQuestionRepositoryImpl
import com.vpk.sprachninja.data.repository.SettingsRepositoryImpl
import com.vpk.sprachninja.data.repository.UserRepositoryImpl
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.LevelStatsRepository
import com.vpk.sprachninja.domain.repository.RecentQuestionRepository
import com.vpk.sprachninja.domain.repository.SettingsRepository
import com.vpk.sprachninja.domain.repository.UserRepository
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

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

    val recentQuestionRepository: RecentQuestionRepository by lazy {
        RecentQuestionRepositoryImpl(appDatabase.recentQuestionDao())
    }

    val levelStatsRepository: LevelStatsRepository by lazy {
        LevelStatsRepositoryImpl(appDatabase.levelStatsDao())
    }

    // --- Networking ---

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val geminiApiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    // --- Public Repositories that use Networking ---

    val geminiRepository: GeminiRepository by lazy {
        GeminiRepositoryImpl(geminiApiService, settingsRepository)
    }
}