package com.vpk.sprachninja.domain.usecase

import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.domain.repository.SettingsRepository

/**
 * A use case that encapsulates the business logic for saving the application settings.
 *
 * @property settingsRepository The repository that provides settings data operations.
 */
class SaveSettingsUseCase(private val settingsRepository: SettingsRepository) {

    /**
     * Executes the use case to save the application settings.
     * The `operator` keyword allows this class to be invoked as a function.
     *
     * @param appSettings The settings object to be saved.
     */
    suspend operator fun invoke(appSettings: AppSettings) {
        // Here you could add validation logic if needed, e.g.,
        // if (appSettings.apiKey.isBlank()) throw SomeCustomException("API key cannot be empty")
        settingsRepository.saveSettings(appSettings)
    }
}