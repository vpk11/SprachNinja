package com.vpk.sprachninja.presentation.viewmodel

import com.vpk.sprachninja.data.local.User

/**
 * Represents the different states the HomeActivity UI can be in.
 * Using a sealed interface ensures that we handle all possible states in our UI.
 */
sealed interface HomeUiState {
    /** The state while waiting for the initial data from the database. */
    object Loading : HomeUiState

    /** The state when a user profile has been successfully loaded. */
    data class Success(val user: User) : HomeUiState

    /** The state when loading is complete and no user profile was found. */
    object NoUser : HomeUiState
}