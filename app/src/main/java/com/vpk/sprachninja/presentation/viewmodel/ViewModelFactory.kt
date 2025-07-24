package com.vpk.sprachninja.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vpk.sprachninja.di.AppContainer
import com.vpk.sprachninja.domain.usecase.GetSettingsUseCase
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import com.vpk.sprachninja.domain.usecase.SaveSettingsUseCase
import com.vpk.sprachninja.domain.usecase.SaveUserUseCase
import com.vpk.sprachninja.domain.usecase.UpdateUserLevelUseCase

class ViewModelFactory(
    private val appContainer: AppContainer,
    private val context: Context,
    private val questionType: String? = null
) : ViewModelProvider.Factory {

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
                    getUserUseCase = GetUserUseCase(appContainer.userRepository),
                    geminiRepository = appContainer.geminiRepository,
                    settingsRepository = appContainer.settingsRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    getSettingsUseCase = GetSettingsUseCase(appContainer.settingsRepository),
                    saveSettingsUseCase = SaveSettingsUseCase(appContainer.settingsRepository),
                    getUserUseCase = GetUserUseCase(appContainer.userRepository),
                    updateUserLevelUseCase = UpdateUserLevelUseCase(appContainer.userRepository)
                ) as T
            }
            modelClass.isAssignableFrom(QuestionAnswerViewModel::class.java) -> {
                requireNotNull(questionType) { "QuestionType must be provided for QuestionAnswerViewModel" }
                QuestionAnswerViewModel(
                    geminiRepository = appContainer.geminiRepository,
                    userRepository = appContainer.userRepository,
                    recentQuestionRepository = appContainer.recentQuestionRepository,
                    levelStatsRepository = appContainer.levelStatsRepository,
                    context = context,
                    questionType = questionType
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    userRepository = appContainer.userRepository,
                    levelStatsRepository = appContainer.levelStatsRepository,
                    updateUserLevelUseCase = UpdateUserLevelUseCase(appContainer.userRepository)
                ) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}