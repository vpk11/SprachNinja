package com.vpk.sprachninja.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.R
import com.vpk.sprachninja.data.model.Curriculum
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class QuestionAnswerViewModel(
    private val geminiRepository: GeminiRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            try {
                val user = userRepository.getUser().first()
                if (user == null) {
                    _uiState.value = QuestionUiState.Error("User profile not found.")
                    return@launch
                }

                val topics = getTopicsForUserLevel(user.germanLevel)
                if (topics.isEmpty()) {
                    _uiState.value = QuestionUiState.Error("Could not find topics for level ${user.germanLevel}.")
                    return@launch
                }

                val randomTopic = topics.random()
                val prompt = "Generate a single, simple German language practice question about the topic '$randomTopic' for a learner at level ${user.germanLevel}. The question should be challenging but appropriate for the level. Do not include the answer in the response."

                val result = geminiRepository.generateQuestion(prompt)

                result.onSuccess { question ->
                    _uiState.value = QuestionUiState.Success(question)
                }.onFailure { error ->
                    _uiState.value = QuestionUiState.Error(error.message ?: "An unknown error occurred.")
                }

            } catch (e: Exception) {
                Log.e("QAVM", "Error loading question", e)
                _uiState.value = QuestionUiState.Error(e.message ?: "A critical error occurred.")
            }
        }
    }

    private fun getTopicsForUserLevel(userLevel: String): List<String> {
        return try {
            val jsonString = context.resources.openRawResource(R.raw.german_levels_structure)
                .bufferedReader().use { it.readText() }
            val curriculum = jsonParser.decodeFromString<Curriculum>(jsonString)

            val majorLevel = userLevel.take(2) // e.g., "A1" from "A1.1"

            curriculum.levels
                .find { it.level.equals(majorLevel, ignoreCase = true) }
                ?.subLevels
                ?.find { it.subLevelName.equals(userLevel, ignoreCase = true) }
                ?.topics ?: emptyList()
        } catch (e: Exception) {
            Log.e("QAVM", "Error parsing curriculum JSON", e)
            emptyList()
        }
    }
}