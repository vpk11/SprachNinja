package com.vpk.sprachninja.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the structured result from the LLM after validating a user's translation.
 * This allows for more nuanced feedback than a simple true/false check.
 *
 * @property isCorrect True if the user's translation is semantically and grammatically correct,
 *                     even if it differs slightly from the expected answer.
 * @property feedback A helpful message for the user, explaining why their answer was correct
 *                    or providing a correction if it was wrong.
 */
@Serializable
data class TranslationValidationResult(
    val isCorrect: Boolean,
    val feedback: String
)