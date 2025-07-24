## Phase 22: Intelligent Translation Validation

### Step 22.1: Create the Validation Result Data Model
**Goal:** Define a structured data class to hold the result of the LLM's validation. This is more robust than a simple Boolean.
**Context:** The project has a `domain/model` package.
**Prompt:**
"In the `com.vpk.sprachninja.domain.model` package, create a new file `TranslationValidationResult.kt`. Inside, define a new `@Serializable` data class named `TranslationValidationResult`. It must have two properties:
*   `isCorrect`: A `Boolean` that is `true` if the user's translation is acceptable.
*   `feedback`: A `String` that provides a brief explanation for the validation (e.g., 'Correct!', 'Almost! A more natural phrasing would be...', or 'This translation is incorrect because...')."

---

### Step 22.2: Add a Validation Method to the Gemini Repository
**Goal:** Create a new function in the `GeminiRepository` specifically for validating a user's translation. This keeps the API logic clean and centralized.
**Context:** The `GeminiRepository` and `GeminiRepositoryImpl` files exist.
**Prompt:**
"This is a two-part task for the `GeminiRepository`.

1.  **Update the Interface:** In `domain/repository/GeminiRepository.kt`, add a new `suspend` function:
    ```kotlin
    suspend fun validateTranslation(
        originalQuestion: String,
        expectedAnswer: String,
        userAnswer: String
    ): Result<com.vpk.sprachninja.domain.model.TranslationValidationResult>
    ```

2.  **Update the Implementation:** In `data/repository/GeminiRepositoryImpl.kt`, implement the new `validateTranslation` method. The core of this function will be a new, carefully crafted prompt. Use the following prompt logic:
    *   **Task:** Ask the LLM to act as a language expert evaluating a student's answer.
    *   **Inputs:** Provide the original question (e.g., the English sentence), the expected answer (the one Gemini originally provided), and the student's actual answer.
    *   **Instruction:** Tell the LLM that minor variations, synonyms, or different but correct word orders are acceptable. The key is semantic equivalence and grammatical correctness.
    *   **Output:** Instruct the LLM to **return only a JSON object** that matches the `TranslationValidationResult` data class (`isCorrect` and `feedback`).
    *   After getting the response, parse the JSON into your `TranslationValidationResult` model and return it inside a `Result.success` wrapper."

---

### Step 22.3: Update the ViewModel to Use the Validation Method
**Goal:** Modify the `QuestionAnswerViewModel` to call the new validation method for translation questions, while keeping the old logic for fill-in-the-blank.
**Context:** The `QuestionAnswerViewModel.kt` file has a `checkAnswer()` function.
**Prompt:**
"Modify `QuestionAnswerViewModel.kt` with the following changes:

1.  Add a new state flow to hold the feedback text from the validation:
    ```kotlin
    private val _validationFeedback = MutableStateFlow<String?>(null)
    val validationFeedback: StateFlow<String?> = _validationFeedback.asStateFlow()
    ```
2.  Reset this new state flow to `null` inside the `loadNextQuestion()` function.
3.  Update the `checkAnswer()` function. It needs to handle the two question types differently:
    *   Check `currentState.question.questionType`.
    *   If the type is `TRANSLATE_EN_DE`:
        *   Call the new `geminiRepository.validateTranslation()` method, passing in the required strings.
        *   On success, update `_validationState` using the `isCorrect` boolean from the result.
        *   Update `_validationFeedback` with the `feedback` string from the result.
    *   If the type is `FILL_IN_THE_BLANK` (or in an `else` block):
        *   Keep the existing `userAnswer.value.trim().equals(...)` logic.
        *   If the answer is incorrect, set `_validationFeedback` to show the correct answer, e.g., `_validationFeedback.value = "Correct answer: ${currentState.question.correctAnswer}"`."

---

### Step 22.4: Update the UI to Display the Intelligent Feedback
**Goal:** Show the new, helpful feedback message to the user on the question screen.
**Context:** The `QuestionAnswerScreen.kt` file displays the UI based on the `validationState`.
**Prompt:**
"Modify the `SuccessState` composable inside `com.vpk.sprachninja.presentation.ui.view.QuestionAnswerScreen.kt`.

1.  The composable should now accept the new `validationFeedback: String?` state from the ViewModel.
2.  When the `validationState` is `INCORRECT` and `validationFeedback` is not null or blank:
    *   Instead of (or in addition to) showing a simple "Correct answer" string, display the `validationFeedback` text. This will show the user the helpful explanation or correction provided by the LLM. You can display this in a `Text` composable below the answer field."