package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun saveSettings(appSettings: AppSettings)

    fun getSettings(): Flow<AppSettings>

    /**
     * Gets the cached "Tip of the Day" for a specific date.
     * @param dateString The date in "YYYY-MM-DD" format.
     * @return The cached tip, or null if not found.
     */
    suspend fun getDailyTip(dateString: String): String?

    /**
     * Saves the "Tip of the Day" for a specific date.
     * @param dateString The date in "YYYY-MM-DD" format.
     * @param tip The tip to be cached.
     */
    suspend fun saveDailyTip(dateString: String, tip: String)
}