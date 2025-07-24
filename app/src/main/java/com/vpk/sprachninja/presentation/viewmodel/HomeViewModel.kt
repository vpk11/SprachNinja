package com.vpk.sprachninja.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.SettingsRepository
import com.vpk.sprachninja.domain.usecase.GetUserUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    getUserUseCase: GetUserUseCase,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _dailyTip = MutableStateFlow<String?>("Loading tip...")
    val dailyTip: StateFlow<String?> = _dailyTip.asStateFlow()

    init {
        getUserUseCase()
            .onEach { user ->
                if (user != null) {
                    _uiState.value = HomeUiState.Success(user)
                    // 5. Call the new function once we have the user's level
                    fetchDailyTip(user.germanLevel)
                } else {
                    _uiState.value = HomeUiState.NoUser
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchDailyTip(userLevel: String) {
        viewModelScope.launch {
            val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cachedTip = settingsRepository.getDailyTip(todayString)

            if (cachedTip != null) {
                _dailyTip.value = cachedTip
            } else {
                val result = geminiRepository.getDailyTip(userLevel)
                result.onSuccess { newTip ->
                    settingsRepository.saveDailyTip(todayString, newTip)
                    _dailyTip.value = newTip
                }.onFailure { error ->
                    _dailyTip.value = "Could not load tip: ${error.message}"
                }
            }
        }
    }
}