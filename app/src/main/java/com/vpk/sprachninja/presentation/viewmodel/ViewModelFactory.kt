package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vpk.sprachninja.di.AppContainer
import com.vpk.sprachninja.domain.usecase.GetSettingsUseCase
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import com.vpk.sprachninja.domain.usecase.SaveSettingsUseCase
import com.vpk.sprachninja.domain.usecase.SaveUserUseCase

/**
 * A custom factory for creating ViewModel instances.
 * This is necessary because our ViewModels have constructors with dependencies
 * that need to be provided from our manual DI container (AppContainer).
 *
 * @property appContainer The application's dependency container.
 */
class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(
                    saveUserUseCase = SaveUserUseCase(appContainer.userRepository)
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    getUserUseCase = GetUserUseCase(appContainer.userRepository)
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    getSettingsUseCase = GetSettingsUseCase(appContainer.settingsRepository),
                    saveSettingsUseCase = SaveSettingsUseCase(appContainer.settingsRepository)
                ) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}