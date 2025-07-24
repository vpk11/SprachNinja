package com.vpk.sprachninja.domain.model

/**
 * A data class representing the application's user-configurable settings.
 *
 * @property apiKey The Gemini API key provided by the user.
 * @property modelName The specific Gemini model the user wishes to use.
 */
data class AppSettings(
    val apiKey: String,
    val modelName: String
)