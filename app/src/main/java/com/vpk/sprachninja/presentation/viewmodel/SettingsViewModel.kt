package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.domain.usecase.GetSettingsUseCase
import com.vpk.sprachninja.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * It manages the UI state for settings and handles saving them.
 *
 * @param getSettingsUseCase The use case for retrieving application settings.
 * @param saveSettingsUseCase The use case for saving application settings.
 */
class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    // Define a default/initial state for the UI before the first value from the Flow is emitted.
    private val initialSettings = AppSettings(apiKey = "", modelName = "gemini-1.5-flash")

    /**
     * Exposes the current application settings as a StateFlow.
     * The UI layer will collect this flow to reactively display the latest settings.
     * The `stateIn` operator converts the cold Flow from the use case into a hot StateFlow,
     * making it suitable for UI consumption.
     */
    val settings: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialSettings
        )

    /**
     * Saves the application settings.
     * This function is called from the UI and manages its own coroutine scope.
     *
     * @param apiKey The user-provided Gemini API key.
     * @param modelName The user-selected model name.
     */
    fun saveSettings(apiKey: String, modelName: String) {
        // Launch a coroutine within the viewModelScope to perform the save operation
        // on a background thread without blocking the UI.
        viewModelScope.launch {
            val finalModelName = modelName.trim().ifBlank { initialSettings.modelName }
            val newSettings = AppSettings(
                apiKey = apiKey.trim(),
                modelName = finalModelName
            )
            saveSettingsUseCase(newSettings)
        }
    }
}