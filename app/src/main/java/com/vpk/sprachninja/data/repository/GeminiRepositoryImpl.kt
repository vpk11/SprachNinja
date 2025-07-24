package com.vpk.sprachninja.data.repository

import android.util.Log
import com.vpk.sprachninja.data.remote.GeminiApiService
import com.vpk.sprachninja.data.remote.dto.Content
import com.vpk.sprachninja.data.remote.dto.GeminiRequest
import com.vpk.sprachninja.data.remote.dto.Part
import com.vpk.sprachninja.domain.model.PracticeQuestion
import com.vpk.sprachninja.domain.model.TranslationValidationResult
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

    override suspend fun validateTranslation(
        originalQuestion: String,
        expectedAnswer: String,
        userAnswer: String
    ): Result<TranslationValidationResult> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName

            if (apiKey.isBlank()) {
                throw IllegalStateException("Gemini API key is not set.")
            }

            val validationPrompt = """
                You are a German language teaching assistant. Your task is to evaluate a student's translation from English to German.

                CONTEXT:
                - Original English Sentence: "$originalQuestion"
                - A sample correct German translation: "$expectedAnswer"
                - The student's submitted German translation: "$userAnswer"

                EVALUATION CRITERIA:
                1.  Determine if the student's translation is grammatically correct and semantically equivalent to the original English sentence.
                2.  Minor differences in word choice (synonyms) or word order are acceptable if the meaning is the same and the sentence is natural-sounding.
                3.  For example, if the original is "I'm going to the cinema" and the student writes "Ich gehe ins Kino", "Ich gehe zum Kino", or "Ich fahre ins Kino", all are correct.

                OUTPUT:
                You MUST return ONLY a single, raw JSON object with no extra text or markdown.
                The JSON object must have the following keys:
                - "isCorrect": A boolean (`true` or `false`).
                - "feedback": A brief, helpful string for the student. If correct, say "Correct!" or "Great job!". If incorrect, briefly explain the mistake (e.g., "Good try, but the verb should be at the end of the sentence.").
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = validationPrompt))))
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
                    val result = json.decodeFromString<TranslationValidationResult>(cleanedJson)
                    Result.success(result)
                } catch (e: Exception) {
                    Log.e("GeminiRepo", "JSON Parsing failed for validation: ${e.message}. Cleaned text: $cleanedJson", e)
                    Result.failure(Exception("Failed to parse the validation from the API response."))
                }
            } else {
                Result.failure(Exception("The API did not return any validation content."))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Error validating translation via Gemini API", e)
            Result.failure(e)
        }
    }

    override suspend fun getDailyTip(userLevel: String): Result<String> {
        return try {
            val settings = settingsRepository.getSettings().first()
            val apiKey = settings.apiKey
            val modelName = settings.modelName

            if (apiKey.isBlank()) {
                return Result.failure(Exception("Gemini API key is not set."))
            }

            val tipPrompt = """
                You are an encouraging German language expert.
                Generate a single, short, and interesting tip about the German language, grammar, or culture that would be helpful for a student at the $userLevel level.
                The tip should be no more than two sentences.
                
                CRITICAL INSTRUCTIONS:
                - Do not use markdown.
                - Do not use JSON.
                - Return only the raw text of the tip.
                
                EXAMPLE:
                Did you know? In German, all nouns are capitalized, no matter where they appear in a sentence. This can make them easier to spot!
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = tipPrompt))))
            )

            val response = geminiApiService.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawText != null) {
                Result.success(rawText.trim())
            } else {
                Result.failure(Exception("The API did not return a tip."))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Error fetching daily tip from Gemini API", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        userLevel: String,
        topic: String,
        questionType: String,
        recentQuestions: List<String>
    ): String {
        val recentQuestionsString = recentQuestions.joinToString(separator = "\n - ") { it }

        val baseInstructions = """
            IMPORTANT:
            Do not generate any of the following questions again:
             - $recentQuestionsString

            OUTPUT:
            You MUST return ONLY a single, raw JSON object with no extra text or markdown formatting.
            The JSON object must have the following keys: "questionText", "correctAnswer", "questionType", and optionally "options".
        """.trimIndent()

        return when (questionType) {
            "MULTIPLE_CHOICE_WORD" -> """
                You are a German language teacher creating a multiple-choice vocabulary quiz.
                Your task is to generate a question for a student at the $userLevel level from the topic "$topic".

                CRITICAL INSTRUCTIONS:
                1. Select a single, common German noun (including its article, e.g., "der Tisch"), verb, or adjective appropriate for the $userLevel.
                2. Provide the single correct English translation for this word.
                3. Provide two plausible but incorrect English translations. These distractors should be related in theme or sound.
                4. The three English options (1 correct, 2 incorrect) must be shuffled randomly.

                $baseInstructions
                The JSON "questionType" key MUST have the exact value "MULTIPLE_CHOICE_WORD".
                The "questionText" key should be the German word.
                The "correctAnswer" key should be the correct English translation.
                The "options" key must be an array of three strings: the correct answer and the two incorrect distractors in a random order.
            """.trimIndent()
            "TRANSLATE_EN_DE" -> """
                You are an expert German teacher creating a translation exercise.
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
                You are an expert German teacher. Your task is to generate one single, high-quality, unambiguous fill-in-the-blank question in German.
                Your task is to generate a question for a student at the $userLevel level.
                The topic is "$topic".

                CRITICAL INSTRUCTIONS:
                1. Choose exactly one grammar point appropriate for this level.
                2. Introduce exactly one new vocabulary word suitable for this level.
                3. Write one complete German sentence (8–15 words) containing a single "___" placeholder.
                4. The sentence context must make the correct answer the only possible choice.
                5. Use the new vocabulary in its correct form (case, gender, number) and don’t test any other rules.
                6. Do not include any extra text, explanation or formatting—return only a raw JSON object.
                7. BAD EXAMPLE: "Das ist meine ___." (This is bad because many nouns can fit).

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