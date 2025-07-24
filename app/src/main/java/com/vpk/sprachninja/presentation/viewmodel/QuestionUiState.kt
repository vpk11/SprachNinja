package com.vpk.sprachninja.presentation.viewmodel

import com.vpk.sprachninja.domain.model.PracticeQuestion

/**
 * Represents the different states the Question & Answer screen UI can be in.
 */
sealed interface QuestionUiState {
    /** The state while waiting for a question to be generated. */
    object Loading : QuestionUiState

    /** The state when a question has been successfully generated. */
    data class Success(val question: PracticeQuestion) : QuestionUiState

    /** The state when an error occurs during question generation. */
    data class Error(val message: String) : QuestionUiState
}