package com.vpk.sprachninja.di

import android.content.Context
import com.vpk.sprachninja.data.local.AppDatabase
import com.vpk.sprachninja.data.repository.SettingsRepositoryImpl
import com.vpk.sprachninja.data.repository.UserRepositoryImpl
import com.vpk.sprachninja.domain.repository.SettingsRepository
import com.vpk.sprachninja.domain.repository.UserRepository

/**
 * A manual dependency injection container that is created in the Application class.
 * It holds and provides instances of repositories, data sources, and use cases.
 */
class AppContainer(private val context: Context) {

    /**
     * A private lazy-initialized property for the Room database.
     * Being private, it ensures that other parts of the app access the database
     * only through the repository abstractions.
     * `lazy` ensures the database is created only when it's first needed.
     */
    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    /**
     * A public lazy-initialized property for the UserRepository.
     * ViewModels and UseCases will get this instance from the container.
     * It is typed as the interface (UserRepository) to respect the dependency inversion principle.
     */
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(appDatabase.userDao())
    }

    /**
     * A public lazy-initialized property for the SettingsRepository.
     * This provides access to securely stored application settings.
     */
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }
}