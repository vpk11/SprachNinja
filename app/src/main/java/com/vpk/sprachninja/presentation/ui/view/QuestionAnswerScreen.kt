package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.domain.model.PracticeQuestion
import com.vpk.sprachninja.presentation.viewmodel.QuestionAnswerViewModel
import com.vpk.sprachninja.presentation.viewmodel.QuestionUiState
import com.vpk.sprachninja.presentation.viewmodel.ValidationState
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionAnswerScreen(
    viewModel: QuestionAnswerViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userAnswer by viewModel.userAnswer.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val validationFeedback by viewModel.validationFeedback.collectAsState()

    SprachNinjaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Practice Session") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    }
                )
            },
            modifier = Modifier.imePadding()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is QuestionUiState.Loading -> CircularProgressIndicator()
                    is QuestionUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadNextQuestion() })
                    is QuestionUiState.Success -> {
                        SuccessState(
                            question = state.question,
                            userAnswer = userAnswer,
                            onAnswerChange = { viewModel.userAnswer.value = it },
                            validationState = validationState,
                            validationFeedback = validationFeedback,
                            onCheckAnswer = { viewModel.checkAnswer() },
                            onNext = { viewModel.loadNextQuestion() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessState(
    question: PracticeQuestion,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    validationState: ValidationState,
    validationFeedback: String?,
    onCheckAnswer: () -> Unit,
    onNext: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (question.questionType) {
            "MULTIPLE_CHOICE_WORD" -> MultipleChoiceInput(
                options = question.options,
                validationState = validationState,
                onAnswerSelected = { selectedOption ->
                    onAnswerChange(selectedOption)
                    onCheckAnswer()
                }
            )
            else -> {
                TextInput(
                    userAnswer = userAnswer,
                    onAnswerChange = onAnswerChange,
                    validationState = validationState,
                    validationFeedback = validationFeedback,
                    onCheckAnswer = onCheckAnswer,
                    focusManager = focusManager
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (validationState != ValidationState.UNCHECKED && !validationFeedback.isNullOrBlank()) {
            Text(
                text = validationFeedback,
                color = if (validationState == ValidationState.CORRECT) Color.Green else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (validationState != ValidationState.UNCHECKED) {
            TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Next Question")
            }
        }
    }
}

@Composable
private fun TextInput(
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    validationState: ValidationState,
    validationFeedback: String?,
    onCheckAnswer: () -> Unit,
    focusManager: FocusManager
) {
    OutlinedTextField(
        value = userAnswer,
        onValueChange = onAnswerChange,
        label = { Text("Your answer") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            when (validationState) {
                ValidationState.CORRECT -> Icon(Icons.Filled.Check, "Correct", tint = Color.Green)
                ValidationState.INCORRECT -> Icon(Icons.Filled.Close, "Incorrect", tint = MaterialTheme.colorScheme.error)
                ValidationState.UNCHECKED -> Unit
            }
        },
        readOnly = validationState != ValidationState.UNCHECKED,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (validationState == ValidationState.UNCHECKED) {
                onCheckAnswer()
                focusManager.clearFocus()
            }
        })
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = {
            onCheckAnswer()
            focusManager.clearFocus()
        },
        enabled = validationState == ValidationState.UNCHECKED && userAnswer.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (validationFeedback == "Checking...") {
            CircularProgressIndicator(modifier = Modifier.height(24.dp))
        } else {
            Text("Check Answer")
        }
    }
}

@Composable
private fun MultipleChoiceInput(
    options: List<String>?,
    validationState: ValidationState,
    onAnswerSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options?.forEach { option ->
            Button(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                enabled = validationState == ValidationState.UNCHECKED
            ) {
                Text(option)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}