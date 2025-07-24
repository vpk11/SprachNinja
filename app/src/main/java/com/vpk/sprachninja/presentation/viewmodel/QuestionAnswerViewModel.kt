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
    private val questionType: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    val userAnswer = MutableStateFlow("")

    private val _validationState = MutableStateFlow(ValidationState.UNCHECKED)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    private val _validationFeedback = MutableStateFlow<String?>(null)
    val validationFeedback: StateFlow<String?> = _validationFeedback.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            userAnswer.value = ""
            _validationState.value = ValidationState.UNCHECKED
            _validationFeedback.value = null

            try {
                val user = userRepository.getUser().first()
                if (user == null) {
                    _uiState.value = QuestionUiState.Error("User profile not found.")
                    return@launch
                }

                val recentQuestions = recentQuestionRepository.getRecentQuestionsForLevel(user.germanLevel)
                val exclusionList = recentQuestions.map { it.questionText }

                val topics = getTopicsForUserLevel(user.germanLevel)
                if (topics.isEmpty()) {
                    _uiState.value = QuestionUiState.Error("Could not find topics for level ${user.germanLevel}.")
                    return@launch
                }
                val randomTopic = topics.random()

                val result = geminiRepository.generateQuestion(
                    userLevel = user.germanLevel,
                    topic = randomTopic,
                    questionType = questionType,
                    recentQuestions = exclusionList
                )

                result.onSuccess { practiceQuestion ->
                    _uiState.value = QuestionUiState.Success(practiceQuestion)
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
            val currentQuestion = currentState.question
            val currentAnswer = userAnswer.value.trim()

            if (currentAnswer.isBlank()) return

            viewModelScope.launch {
                when (currentQuestion.questionType) {
                    "TRANSLATE_EN_DE" -> checkTranslationWithLLM(currentQuestion, currentAnswer)
                    // Add the new case here
                    "MULTIPLE_CHOICE_WORD", "FILL_IN_THE_BLANK" -> checkFillInTheBlank(currentQuestion, currentAnswer)
                    else -> { // Default fallback
                        checkFillInTheBlank(currentQuestion, currentAnswer)
                    }
                }
            }
        }
    }

    private suspend fun checkTranslationWithLLM(question: com.vpk.sprachninja.domain.model.PracticeQuestion, answer: String) {
        _validationFeedback.value = "Checking..."
        val result = geminiRepository.validateTranslation(
            originalQuestion = question.questionText,
            expectedAnswer = question.correctAnswer,
            userAnswer = answer
        )

        result.onSuccess { validationResult ->
            _validationFeedback.value = validationResult.feedback
            val isCorrect = validationResult.isCorrect
            _validationState.value = if (isCorrect) ValidationState.CORRECT else ValidationState.INCORRECT
            updateStats(isCorrect)
        }.onFailure {
            _validationFeedback.value = "Error checking answer. Please try again."
            _validationState.value = ValidationState.INCORRECT
        }
    }

    private fun checkFillInTheBlank(question: com.vpk.sprachninja.domain.model.PracticeQuestion, answer: String) {
        val isCorrect = answer.equals(question.correctAnswer, ignoreCase = true)
        if (isCorrect) {
            _validationFeedback.value = "Correct!"
        } else {
            _validationFeedback.value = "Correct answer: ${question.correctAnswer}"
        }
        _validationState.value = if (isCorrect) ValidationState.CORRECT else ValidationState.INCORRECT
        viewModelScope.launch {
            updateStats(isCorrect)
        }
    }

    private suspend fun updateStats(isCorrect: Boolean) {
        val user = userRepository.getUser().first()
        if (user != null) {
            if (isCorrect) {
                levelStatsRepository.incrementCorrectCount(user.germanLevel)
            } else {
                levelStatsRepository.incrementWrongCount(user.germanLevel)
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