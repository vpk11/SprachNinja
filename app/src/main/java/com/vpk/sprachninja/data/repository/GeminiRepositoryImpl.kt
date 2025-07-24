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

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateQuestion(prompt: String): Result<PracticeQuestion> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName

            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key is not set. Please set it in the settings."))
            }

            // --- THIS IS THE NEW, REFINED PROMPT ---
            val fullPrompt = """
                $prompt

                CRITICAL INSTRUCTIONS:
                1.  The question must test a SINGLE, SPECIFIC grammar rule or vocabulary word related to the topic.
                2.  The sentence context MUST make the correct answer clear and unambiguous. There should be only one logical answer.
                3.  BAD EXAMPLE: "Das ist meine ___." (This is bad because many nouns can fit).
                4.  GOOD EXAMPLE for topic 'Accusative Prepositions': "Der Vogel fliegt ___ den Baum." (The answer 'durch' is the only logical choice).
                5.  GOOD EXAMPLE for topic 'Family': "Die Mutter meines Vaters ist meine ___." (The answer 'Oma' is the only logical choice).
                
                OUTPUT:
                You MUST return ONLY a single, raw JSON object with no extra text or markdown formatting.
                The JSON object must have the following keys:
                - "questionText": A string containing the question with "___" as the blank.
                - "correctAnswer": A string containing ONLY the word or phrase that fits in the blank.
                - "questionType": A string with the exact value "FILL_IN_THE_BLANK".
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = fullPrompt))))
            )

            val response = geminiApiService.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawText != null) {
                val cleanedJson = cleanApiResponse(rawText)
                try {
                    val practiceQuestion = json.decodeFromString<PracticeQuestion>(cleanedJson)
                    Result.success(practiceQuestion)
                } catch (e: Exception) {
                    Log.e("GeminiRepo", "JSON Parsing failed: ${e.message}. Cleaned text: $cleanedJson", e)
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

    private fun cleanApiResponse(rawText: String): String {
        val startIndex = rawText.indexOf('{')
        val endIndex = rawText.lastIndexOf('}')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            rawText.substring(startIndex, endIndex + 1)
        } else {
            rawText
        }
    }
}