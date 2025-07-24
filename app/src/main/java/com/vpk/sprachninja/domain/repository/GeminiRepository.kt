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
     */
    suspend fun generateQuestion(
        userLevel: String,
        topic: String,
        questionType: String,
        recentQuestions: List<String>
    ): Result<PracticeQuestion>

    /**
     * Validates a user's translation against an expected answer using the Gemini API.
     */
    suspend fun validateTranslation(
        originalQuestion: String,
        expectedAnswer: String,
        userAnswer: String
    ): Result<TranslationValidationResult>

    /**
     * Fetches a short, interesting tip about the German language or culture.
     */
    suspend fun getDailyTip(userLevel: String): Result<String>
}