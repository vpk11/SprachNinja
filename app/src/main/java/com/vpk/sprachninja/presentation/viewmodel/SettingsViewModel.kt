package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.usecase.GetSettingsUseCase
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import com.vpk.sprachninja.domain.usecase.SaveSettingsUseCase
import com.vpk.sprachninja.domain.usecase.UpdateUserLevelUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserLevelUseCase: UpdateUserLevelUseCase
) : ViewModel() {

    private val initialSettings = AppSettings(apiKey = "", modelName = "gemini-1.5-flash")

    val settings: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialSettings
        )

    val user: StateFlow<User?> = getUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    fun saveSettings(apiKey: String, modelName: String) {
        viewModelScope.launch {
            val finalModelName = modelName.trim().ifBlank { initialSettings.modelName }
            val newSettings = AppSettings(
                apiKey = apiKey.trim(),
                modelName = finalModelName
            )
            saveSettingsUseCase(newSettings)
        }
    }

    /**
     * Updates the user's current German proficiency level.
     * @param newLevel The new level selected by the user.
     */
    fun updateUserLevel(newLevel: String) {
        viewModelScope.launch {
            updateUserLevelUseCase(newLevel)
        }
    }
}