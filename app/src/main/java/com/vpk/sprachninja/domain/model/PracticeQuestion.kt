package com.vpk.sprachninja.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a single, structured practice question generated by the Gemini API.
 * This model allows the app to understand the question, its answer, and its type,
 * enabling more complex interactions like answer checking.
 *
 * @property questionText The main text of the question presented to the user.
 *                        e.g., "Ich gehe ___ Kino."
 * @property correctAnswer The exact, correct answer for the question.
 *                         e.g., "ins"
 * @property questionType A string identifier for the type of question, which can be used
 *                        by the UI to render different input methods in the future.
 *                        e.g., "FILL_IN_THE_BLANK", "MULTIPLE_CHOICE"
 * @property options A list of choices for multiple-choice questions. This will be null
 *                   for other question types.
 */
@Serializable
data class PracticeQuestion(
    val questionText: String,
    val correctAnswer: String,
    val questionType: String,
    val options: List<String>? = null // Added this line
)