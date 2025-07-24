package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.usecase.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Onboarding screen.
 * It manages the UI state and handles user interactions for the onboarding process.
 *
 * @property saveUserUseCase The use case responsible for saving the user profile.
 */
class OnboardingViewModel(
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    /**
     * Holds the current value of the username input field.
     */
    val username = MutableStateFlow("")

    /**
     * Holds the current value of the German level input field.
     */
    val germanLevel = MutableStateFlow("")

    /**

     * A private StateFlow to manage the completion state internally.
     */
    private val _onboardingComplete = MutableStateFlow(false)

    /**
     * A public, read-only StateFlow that the UI can observe to know when onboarding is finished.
     */
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()

    /**
     * Saves the user profile based on the current state of the input fields.
     * This function is called when the user clicks the "Get Started" button.
     */
    fun saveUser() {
        // Ensure username is not empty before proceeding
        if (username.value.isBlank()) {
            return
        }

        viewModelScope.launch {
            val userToSave = User(
                username = username.value.trim(),
                germanLevel = germanLevel.value.trim().ifBlank { "A1.1" } // Default level if empty
            )
            saveUserUseCase(userToSave)
            // Signal that the process is complete
            _onboardingComplete.value = true
        }
    }
}