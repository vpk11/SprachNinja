package com.vpk.sprachninja.data.repository

import android.util.Log
import com.vpk.sprachninja.data.remote.GeminiApiService
import com.vpk.sprachninja.data.remote.dto.Content
import com.vpk.sprachninja.data.remote.dto.GeminiRequest
import com.vpk.sprachninja.data.remote.dto.Part
import com.vpk.sprachninja.domain.model.PracticeQuestion
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class GeminiRepositoryImpl(
    private val geminiApiService: GeminiApiService,
    private val settingsRepository: SettingsRepository
) : GeminiRepository {

    // Initialize the JSON parser, making it lenient to unknown keys from the API.
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateQuestion(prompt: String): Result<PracticeQuestion> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName

            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key is not set. Please set it in the settings."))
            }

            val fullPrompt = """
                $prompt
                
                Based on the topic above, generate a single practice question.
                The question must be a fill-in-the-blank style, focusing on a single grammar rule or vocabulary word.
                
                You MUST return ONLY a single, raw JSON object with no extra text or markdown formatting.
                The JSON object must have the following keys:
                - "questionText": A string containing the question with "___" as the blank.
                - "correctAnswer": A string containing ONLY the word or phrase that fits in the blank.
                - "questionType": A string with the exact value "FILL_IN_THE_BLANK".
                
                Example: {"questionText":"Ich gehe ___ Kino.","correctAnswer":"ins","questionType":"FILL_IN_THE_BLANK"}
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = fullPrompt))))
            )

            val response = geminiApiService.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (generatedText != null) {
                try {
                    val practiceQuestion = json.decodeFromString<PracticeQuestion>(generatedText)
                    Result.success(practiceQuestion)
                } catch (e: Exception) {
                    Log.e("GeminiRepo", "JSON Parsing failed: ${e.message}. Raw text: $generatedText", e)
                    Result.failure(Exception("Failed to parse the question from the API response."))
                }
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