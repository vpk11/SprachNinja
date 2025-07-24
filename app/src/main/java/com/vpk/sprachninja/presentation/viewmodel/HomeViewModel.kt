package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Home screen.
 * Its primary responsibility is to determine if a user profile exists to guide navigation.
 *
 * @param getUserUseCase The use case responsible for retrieving the user profile.
 */
class HomeViewModel(
    getUserUseCase: GetUserUseCase
) : ViewModel() {

    /**
     * A cold Flow from the use case is converted into a hot StateFlow.
     * This makes the last known user value available to new collectors and survives
     * configuration changes. It starts sharing when the UI is visible and stops after 5 seconds
     * of inactivity.
     */
    val user: StateFlow<User?> = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}