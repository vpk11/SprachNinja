package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.data.local.LevelStats
import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.repository.LevelStatsRepository
import com.vpk.sprachninja.domain.repository.UserRepository
import com.vpk.sprachninja.domain.usecase.UpdateUserLevelUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val levelStatsRepository: LevelStatsRepository,
    private val updateUserLevelUseCase: UpdateUserLevelUseCase // Added
) : ViewModel() {

    /**
     * Exposes a state flow of the current user, automatically updating when the user data changes.
     */
    val user: StateFlow<User?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Exposes a state flow of the statistics for the user's current level.
     * It automatically re-fetches stats if the user's level changes.
     */
    val levelStats: StateFlow<LevelStats?> = user.flatMapLatest { currentUser ->
        if (currentUser != null) {
            levelStatsRepository.getStatsForLevel(currentUser.germanLevel)
        } else {
            flowOf(null) // Emit null if there is no user
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Updates the user's German proficiency level.
     * @param newLevel The new level to be saved.
     */
    fun updateUserLevel(newLevel: String) {
        viewModelScope.launch {
            updateUserLevelUseCase(newLevel)
        }
    }
}