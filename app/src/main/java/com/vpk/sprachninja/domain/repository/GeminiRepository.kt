package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.domain.model.PracticeQuestion

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
}