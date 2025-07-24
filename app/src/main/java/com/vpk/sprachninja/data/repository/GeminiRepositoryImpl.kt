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

    override suspend fun generateQuestion(
        userLevel: String,
        topic: String,
        questionType: String,
        recentQuestions: List<String>
    ): Result<PracticeQuestion> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName

            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key is not set. Please set it in the settings."))
            }

            // Build the prompt based on the requested question type
            val fullPrompt = buildPrompt(userLevel, topic, questionType, recentQuestions)

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

    /**
     * Constructs the full prompt to be sent to the Gemini API based on the question type.
     */
    private fun buildPrompt(
        userLevel: String,
        topic: String,
        questionType: String,
        recentQuestions: List<String>
    ): String {
        val recentQuestionsString = recentQuestions.joinToString(separator = "\n - ") { it }

        val baseInstructions = """
            CRITICAL: Do not generate any of the following questions again:
             - $recentQuestionsString

            OUTPUT:
            You MUST return ONLY a single, raw JSON object with no extra text or markdown formatting.
            The JSON object must have the following keys: "questionText", "correctAnswer", and "questionType".
        """.trimIndent()

        return when (questionType) {
            "TRANSLATE_EN_DE" -> """
                You are a German language teacher creating a translation exercise.
                Your task is to generate a simple English sentence for a German student at the $userLevel level to translate.
                The topic is "$topic".

                CRITICAL INSTRUCTIONS:
                1. The English sentence must be simple, common, and appropriate for the $userLevel level.
                2. The correct German translation should be a standard, natural-sounding sentence.

                $baseInstructions
                The JSON "questionType" key MUST have the exact value "TRANSLATE_EN_DE".
                The "questionText" key should be the English sentence to translate.
                The "correctAnswer" key should be the correct full German translation.
            """.trimIndent()

            "FILL_IN_THE_BLANK" -> """
                You are a German language teacher creating a fill-in-the-blank grammar exercise.
                Your task is to generate a question for a student at the $userLevel level.
                The topic is "$topic".

                CRITICAL INSTRUCTIONS:
                1. The question must test a SINGLE, SPECIFIC grammar rule or vocabulary word related to the topic.
                2. The sentence context MUST make the correct answer clear and unambiguous. There should be only one logical answer.
                3. BAD EXAMPLE: "Das ist meine ___." (This is bad because many nouns can fit).
                4. GOOD EXAMPLE for topic 'Accusative Prepositions': "Der Vogel fliegt ___ den Baum." (The answer 'durch' is the only logical choice).
                5. GOOD EXAMPLE for topic 'Family': "Die Mutter meines Vaters ist meine ___." (The answer 'Oma' is the only logical choice).

                $baseInstructions
                The JSON "questionType" key MUST have the exact value "FILL_IN_THE_BLANK".
                The "questionText" key should be the German sentence with "___" as the blank.
                The "correctAnswer" key should contain ONLY the word or phrase that fits in the blank.
            """.trimIndent()

            else -> throw IllegalArgumentException("Unsupported question type: $questionType")
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