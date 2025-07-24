package com.vpk.sprachninja.domain.repository

/**
 * An interface that defines the contract for generating content via the Gemini API.
 * This decouples the domain layer from the specifics of the remote data source.
 */
interface GeminiRepository {

    /**
     * Generates a question based on a given prompt.
     *
     * @param prompt The detailed prompt to send to the Gemini API.
     * @return A [Result] wrapper containing the generated question text on success,
     *         or an exception on failure.
     */
    suspend fun generateQuestion(prompt: String): Result<String>
}