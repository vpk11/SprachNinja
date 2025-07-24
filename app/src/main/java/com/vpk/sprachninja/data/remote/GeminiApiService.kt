package com.vpk.sprachninja.data.remote

import com.vpk.sprachninja.data.remote.dto.GeminiRequest
import com.vpk.sprachninja.data.remote.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * A Retrofit service interface for interacting with the Google Gemini API.
 */
interface GeminiApiService {

    /**
     * Sends a prompt to the specified Gemini model and requests content generation.
     *
     * @param model The ID of the model to use (e.g., "gemini-1.5-flash"). This is part of the URL path.
     * @param apiKey The user's API key, sent as a URL query parameter.
     * @param request The request body containing the content to be processed by the model.
     * @return A [GeminiResponse] object containing the model's generated content.
     */
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}