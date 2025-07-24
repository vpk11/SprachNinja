package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for saving and retrieving application settings.
 * This decouples the domain layer from the specific data storage implementation.
 */
interface SettingsRepository {

    /**
     * Saves the user's application settings.
     *
     * @param appSettings The settings object to be saved.
     */
    suspend fun saveSettings(appSettings: AppSettings)

    /**
     * Retrieves the application settings as a reactive stream.
     *
     * @return A Flow that emits the current AppSettings whenever they change.
     */
    fun getSettings(): Flow<AppSettings>
}