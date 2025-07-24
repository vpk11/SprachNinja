package com.vpk.sprachninja.domain.usecase

import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case that encapsulates the business logic for retrieving the application settings.
 *
 * @property settingsRepository The repository that provides settings data operations.
 */
class GetSettingsUseCase(private val settingsRepository: SettingsRepository) {

    /**
     * Executes the use case to get the application settings as a reactive stream.
     * The `operator` keyword allows this class to be invoked as a function, e.g., `getSettingsUseCase()`.
     *
     * @return A Flow that emits the current AppSettings.
     */
    operator fun invoke(): Flow<AppSettings> {
        return settingsRepository.getSettings()
    }
}