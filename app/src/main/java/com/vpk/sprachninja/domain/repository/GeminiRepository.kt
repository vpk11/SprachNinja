package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.domain.model.PracticeQuestion
import com.vpk.sprachninja.domain.model.TranslationValidationResult

/**
 * An interface that defines the contract for generating content via the Gemini API.
 * This decouples the domain layer from the specifics of the remote data source.
 */
interface GeminiRepository {

    /**
     * Generates a structured practice question based on a given prompt.
     *
     * @param userLevel The user's current German level (e.g., "A2.1").
     * @param topic The specific topic for the question (e.g., "Dative Prepositions").
     * @param questionType The type of question to generate (e.g., "FILL_IN_THE_BLANK").
     * @param recentQuestions A list of recent question texts to avoid repetition.
     * @return A [Result] wrapper containing the structured [PracticeQuestion] on success,
     *         or an exception on failure.
     */
    suspend fun generateQuestion(
        userLevel: String,
        topic: String,
        questionType: String,
        recentQuestions: List<String>
    ): Result<PracticeQuestion>

    /**
     * Validates a user's translation against an expected answer using the Gemini API.
     *
     * @param originalQuestion The original sentence the user was asked to translate (e.g., in English).
     * @param expectedAnswer The sample correct answer provided by the API initially.
     * @param userAnswer The translation provided by the user.
     * @return A [Result] wrapper containing the [TranslationValidationResult] on success,
     *         or an exception on failure.
     */
    suspend fun validateTranslation(
        originalQuestion: String,
        expectedAnswer: String,
        userAnswer: String
    ): Result<TranslationValidationResult>
}