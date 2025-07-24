package com.vpk.sprachninja.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    companion object {
        private const val PREF_FILE_NAME = "sprachninja_secure_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL_NAME = "model_name"
        private const val DEFAULT_MODEL_NAME = "gemini-1.5-flash"
        private const val KEY_DAILY_TIP_DATE = "daily_tip_date"
        private const val KEY_DAILY_TIP_CONTENT = "daily_tip_content"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        PREF_FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveSettings(appSettings: AppSettings) {
        sharedPreferences.edit {
            putString(KEY_API_KEY, appSettings.apiKey)
            putString(KEY_MODEL_NAME, appSettings.modelName)
        }
    }

    override fun getSettings(): Flow<AppSettings> = callbackFlow {
        fun getCurrentSettings() = AppSettings(
            apiKey = sharedPreferences.getString(KEY_API_KEY, "") ?: "",
            modelName = sharedPreferences.getString(KEY_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        )

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getCurrentSettings())
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getCurrentSettings())

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun getDailyTip(dateString: String): String? {
        val savedDate = sharedPreferences.getString(KEY_DAILY_TIP_DATE, null)
        if (savedDate == dateString) {
            return sharedPreferences.getString(KEY_DAILY_TIP_CONTENT, null)
        }
        return null
    }

    override suspend fun saveDailyTip(dateString: String, tip: String) {
        sharedPreferences.edit {
            putString(KEY_DAILY_TIP_DATE, dateString)
            putString(KEY_DAILY_TIP_CONTENT, tip)
        }
    }
}