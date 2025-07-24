package com.vpk.sprachninja.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vpk.sprachninja.domain.model.AppSettings
import com.vpk.sprachninja.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * The concrete implementation of the SettingsRepository interface.
 * This class is responsible for securely storing and retrieving app settings
 * using EncryptedSharedPreferences.
 *
 * @param context The application context, required to create SharedPreferences.
 */
class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    // Define constants for keys and file names to avoid magic strings.
    companion object {
        private const val PREF_FILE_NAME = "sprachninja_secure_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL_NAME = "model_name"
        private const val DEFAULT_MODEL_NAME = "gemini-1.5-flash"
    }

    // Create the master key for encryption. This key is stored securely by the Android system.
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Initialize the EncryptedSharedPreferences instance.
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Saves the settings to encrypted storage.
     * The ktx 'edit' extension function handles applying the changes automatically.
     */
    override suspend fun saveSettings(appSettings: AppSettings) {
        sharedPreferences.edit {
            putString(KEY_API_KEY, appSettings.apiKey)
            putString(KEY_MODEL_NAME, appSettings.modelName)
        }
    }

    /**
     * Retrieves the settings as a Flow.
     * `callbackFlow` is used to convert the callback-based OnSharedPreferenceChangeListener
     * into a modern, reactive Flow.
     */
    override fun getSettings(): Flow<AppSettings> = callbackFlow {
        // A helper function to read the current state from SharedPreferences.
        fun getCurrentSettings() = AppSettings(
            apiKey = sharedPreferences.getString(KEY_API_KEY, "") ?: "",
            modelName = sharedPreferences.getString(KEY_MODEL_NAME, DEFAULT_MODEL_NAME) ?: DEFAULT_MODEL_NAME
        )

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            // When a preference changes, send the new settings to the Flow.
            trySend(getCurrentSettings())
        }

        // Register the listener to be notified of changes.
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // Immediately send the current value when the Flow is first collected.
        trySend(getCurrentSettings())

        // The 'awaitClose' block is crucial for cleanup. It is executed when the
        // Flow's consumer is cancelled.
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}