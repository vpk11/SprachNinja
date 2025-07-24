package com.vpk.sprachninja.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpk.sprachninja.R
import com.vpk.sprachninja.data.local.RecentQuestion
import com.vpk.sprachninja.data.model.Curriculum
import com.vpk.sprachninja.domain.repository.GeminiRepository
import com.vpk.sprachninja.domain.repository.LevelStatsRepository
import com.vpk.sprachninja.domain.repository.RecentQuestionRepository
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class ValidationState {
    UNCHECKED, CORRECT, INCORRECT
}

class QuestionAnswerViewModel(
    private val geminiRepository: GeminiRepository,
    private val userRepository: UserRepository,
    private val recentQuestionRepository: RecentQuestionRepository,
    private val levelStatsRepository: LevelStatsRepository,
    private val context: Context,
    private val questionType: String // Add the new parameter
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    val userAnswer = MutableStateFlow("")

    private val _validationState = MutableStateFlow(ValidationState.UNCHECKED)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            userAnswer.value = ""
            _validationState.value = ValidationState.UNCHECKED

            try {
                val user = userRepository.getUser().first()
                if (user == null) {
                    _uiState.value = QuestionUiState.Error("User profile not found.")
                    return@launch
                }

                // Get recent questions to avoid repetition
                val recentQuestions = recentQuestionRepository.getRecentQuestionsForLevel(user.germanLevel)
                val exclusionList = recentQuestions.map { it.questionText }

                val topics = getTopicsForUserLevel(user.germanLevel)
                if (topics.isEmpty()) {
                    _uiState.value = QuestionUiState.Error("Could not find topics for level ${user.germanLevel}.")
                    return@launch
                }
                val randomTopic = topics.random()

                // 3. Call the refactored repository method with all the required parameters
                val result = geminiRepository.generateQuestion(
                    userLevel = user.germanLevel,
                    topic = randomTopic,
                    questionType = questionType,
                    recentQuestions = exclusionList
                )

                result.onSuccess { practiceQuestion ->
                    _uiState.value = QuestionUiState.Success(practiceQuestion)
                    // Save the new question to the recent list
                    val newRecentQuestion = RecentQuestion(
                        questionText = practiceQuestion.questionText,
                        userLevel = user.germanLevel
                    )
                    recentQuestionRepository.insert(newRecentQuestion)
                }.onFailure { error ->
                    _uiState.value = QuestionUiState.Error(error.message ?: "An unknown error occurred.")
                }

            } catch (e: Exception) {
                Log.e("QAVM", "Error loading question", e)
                _uiState.value = QuestionUiState.Error(e.message ?: "A critical error occurred.")
            }
        }
    }

    fun checkAnswer() {
        val currentState = _uiState.value
        if (currentState is QuestionUiState.Success) {
            val isCorrect = userAnswer.value.trim().equals(
                currentState.question.correctAnswer,
                ignoreCase = true
            )
            _validationState.value = if (isCorrect) ValidationState.CORRECT else ValidationState.INCORRECT

            viewModelScope.launch {
                val user = userRepository.getUser().first()
                if (user != null) {
                    if (isCorrect) {
                        levelStatsRepository.incrementCorrectCount(user.germanLevel)
                    } else {
                        levelStatsRepository.incrementWrongCount(user.germanLevel)
                    }
                }
            }
        }
    }

    private fun getTopicsForUserLevel(userLevel: String): List<String> {
        return try {
            val jsonString = context.resources.openRawResource(R.raw.german_levels_structure)
                .bufferedReader().use { it.readText() }
            val curriculum = jsonParser.decodeFromString<Curriculum>(jsonString)
            val majorLevel = userLevel.take(2)
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