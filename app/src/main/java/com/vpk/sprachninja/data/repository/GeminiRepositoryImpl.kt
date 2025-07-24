package com.vpk.sprachninja.data.repository

import android.util.Log
import com.vpk.sprachninja.data.remote.GeminiApiService
import com.vpk.sprachninja.data.remote.dto.Content
import com.vpk.sprachninja.data.remote.dto.GeminiRequest
import com.vpk.sprachninja.data.remote.dto.Part
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

/**
 * The concrete implementation of the GeminiRepository interface.
 * This class handles fetching settings, building the request, calling the API,
 * and processing the response.
 *
 * @param geminiApiService The Retrofit service for the Gemini API.
 * @param settingsRepository The repository for accessing app settings like the API key.
 */
class GeminiRepositoryImpl(
    private val geminiApiService: GeminiApiService,
    private val settingsRepository: SettingsRepository
) : GeminiRepository {

    override suspend fun generateQuestion(prompt: String): Result<String> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key is not set. Please set it in the settings."))
            }

            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )

            val response = geminiApiService.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (generatedText != null) {
                Result.success(generatedText)
            } else {
                val feedback = response.promptFeedback?.safetyRatings?.firstOrNull()
                val errorMessage = "Failed to generate content. Finish reason: ${feedback?.category}, Probability: ${feedback?.probability}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Error generating question from Gemini API", e)
            Result.failure(e)
        }
    }
}