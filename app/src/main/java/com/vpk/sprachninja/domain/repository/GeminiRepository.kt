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
     * @param prompt The detailed prompt to send to the Gemini API.
     * @return A [Result] wrapper containing the structured [PracticeQuestion] on success,
     *         or an exception on failure.
     */
    suspend fun generateQuestion(prompt: String): Result<PracticeQuestion>
}