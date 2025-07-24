package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vpk.sprachninja.di.AppContainer
import com.vpk.sprachninja.domain.usecase.SaveUserUseCase

/**
 * A custom factory for creating ViewModel instances.
 * This is necessary because our ViewModels have constructors with dependencies
 * that need to be provided from our manual DI container (AppContainer).
 *
 * @property appContainer The application's dependency container.
 */
class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `ViewModel` class.
     *
     * @param modelClass A `Class` whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if the `modelClass` is unknown.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // Check if the requested ViewModel is OnboardingViewModel
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                // Construct OnboardingViewModel with its dependencies from the AppContainer.
                // We create the SaveUserUseCase here, injecting the repository from the container.
                OnboardingViewModel(
                    saveUserUseCase = SaveUserUseCase(appContainer.userRepository)
                ) as T
            }
            // Add other ViewModel creation blocks here in the future, for example:
            // modelClass.isAssignableFrom(HomeViewModel::class.java) -> { ... }

            else -> {
                // If the ViewModel class is not recognized, throw an exception.
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}