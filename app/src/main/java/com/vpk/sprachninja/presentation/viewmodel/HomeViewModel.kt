package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    getUserUseCase: GetUserUseCase
) : ViewModel() {

    /**
     * A StateFlow that represents the complete UI state of the home screen.
     * It starts in a 'Loading' state and then transitions to either 'Success' or 'NoUser'
     * based on the data received from the database.
     */
    val uiState: StateFlow<HomeUiState> = getUserUseCase()
        .map { user ->
            // Transform the raw User? object into a specific UI state
            if (user != null) {
                HomeUiState.Success(user)
            } else {
                HomeUiState.NoUser
            }
        }
        .stateIn(
            scope = viewModelScope,
            // The flow starts emitting only when the UI is subscribed and remains active
            // for 5 seconds afterward to survive configuration changes.
            started = SharingStarted.WhileSubscribed(5000),
            // The crucial initial state is explicitly Loading.
            initialValue = HomeUiState.Loading
        )
}