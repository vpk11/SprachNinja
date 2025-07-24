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

---

## Phase 23: "Learn Word" Multiple-Choice Feature

### Step 23.1: Evolve the Data Model to Support Options
**Goal:** Modify the `PracticeQuestion` data model to hold a list of choices for multiple-choice questions.
**Context:** The `PracticeQuestion` model currently only supports a single question and answer.
**Prompt:**
"Modify the `com.vpk.sprachninja.domain.model.PracticeQuestion` data class.
1.  Add a new property to the data class: `options: List<String>? = null`.
2.  Make the new property **nullable** and give it a **default value of `null`**. This is critical to ensure that our existing `FILL_IN_THE_BLANK` and `TRANSLATE_EN_DE` question types, which don't use this field, continue to work without modification."

---

### Step 23.2: Enhance the Gemini Repository for Multiple-Choice Questions
**Goal:** Teach the `GeminiRepository` how to generate a multiple-choice vocabulary question.
**Context:** The `buildPrompt` function in `GeminiRepositoryImpl.kt` uses a `when` block to create different prompts.
**Prompt:**
"Modify the `buildPrompt` function in `com.vpk.sprachninja.data.repository.GeminiRepositoryImpl.kt`.
1.  Add a new case to the `when` block for a new question type: `"MULTIPLE_CHOICE_WORD"`.
2.  The prompt for this new case must instruct the LLM to do the following:
    *   Act as a German teacher creating a vocabulary quiz for a specific level.
    *   Select **one** German noun, verb, or adjective appropriate for the user's level.
    *   Provide its single correct English translation.
    *   Provide **two plausible but incorrect** English translations (distractors).
    *   Return a JSON object matching the `PracticeQuestion` structure.
    *   The `questionText` should be the German word.
    *   The `correctAnswer` should be the correct English translation.
    *   The `questionType` must be `"MULTIPLE_CHOICE_WORD"`.
    *   The `options` field must be an array of three strings containing the correct answer and the two distractors, **shuffled randomly**."

---

### Step 23.3: Add the New Mode to the Selection Dialog
**Goal:** Allow the user to select the new "Learn Word" practice mode from the home screen.
**Context:** The `PracticeModeDialog.kt` composable displays the list of available practice modes.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view.PracticeModeDialog.kt`, add a new `PracticeModeItem` to the `Column` inside the dialog.
1.  The title should be `"Learn Words"`.
2.  The description should be `"Multiple choice vocabulary."`
3.  The `onClick` lambda should call `onModeSelected` with the new question type string: `"MULTIPLE_CHOICE_WORD"`."

---

### Step 23.4: Implement the Multiple-Choice UI
**Goal:** Render clickable buttons for multiple-choice questions instead of a text field.
**Context:** `QuestionAnswerScreen.kt` currently uses an `OutlinedTextField` for all question types.
**Prompt:**
"Modify the `SuccessState` composable in `com.vpk.sprachninja.presentation.ui.view.QuestionAnswerScreen.kt`.
1.  Inside the `SuccessState` composable, use a `when (question.questionType)` block to display different input controls.
2.  For the `"FILL_IN_THE_BLANK"` and `"TRANSLATE_EN_DE"` cases, keep the existing `OutlinedTextField`.
3.  Add a new case for `"MULTIPLE_CHOICE_WORD"`:
    *   Inside this case, iterate through the `question.options` list (if it's not null).
    *   For each `option` string in the list, create a `Button` that fills the width.
    *   The `Button`'s `onClick` should do two things: first call `onAnswerChange(option)` to register the choice, and then immediately call `onCheckAnswer()` to validate it.
    *   All buttons in the list should be `enabled` only when `validationState` is `UNCHECKED`."

---

### Step 23.5: Finalize the ViewModel Logic
**Goal:** Ensure the ViewModel correctly handles checking the answer for the new question type.
**Context:** The `checkAnswer` function in `QuestionAnswerViewModel.kt` currently has logic for two question types.
**Prompt:**
"Modify the `checkAnswer` function in `com.vpk.sprachninja.presentation.viewmodel.QuestionAnswerViewModel.kt`.
1. Find the `when (currentQuestion.questionType)` block you are about to create in the previous step (or create it if you haven't).
2.  Add a new branch for the `"MULTIPLE_CHOICE_WORD"` case.
3.  The logic inside this new branch can be identical to the `FILL_IN_THE_BLANK` logic: it should call your `checkFillInTheBlank` helper function, which performs a simple string comparison between `userAnswer.value` and `question.correctAnswer`. This works because the UI will have already set `userAnswer.value` to the text of the clicked button."

---

## Phase 24: Modern Settings Screen UI

### Step 24.1: Create a Reusable Section Header Composable
**Goal:** Create a simple, styled text composable to act as a title for each settings group.
**Context:** This will be a new helper composable, likely placed within `SettingsScreen.kt` itself.
**Prompt:**
"In the `com.vpk.sprachninja.presentation.ui.view.SettingsScreen.kt` file, create a new private `@Composable` function named `SettingsHeader`.
1.  It should accept a single `text: String` parameter.
2.  The composable should consist of a `Text` element.
3.  Apply `MaterialTheme.typography.titleMedium` for the style and `MaterialTheme.colorScheme.primary` for the color.
4.  Add a `Modifier` with `padding(top = 24.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)` to give it appropriate spacing within the list."

---

### Step 24.2: Create a Reusable `SettingsCardItem` Composable
**Goal:** Build a versatile, clickable `Card` that will be the primary UI for each individual setting. This replaces the old simple row.
**Context:** This is a new, core component for the settings screen.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view.SettingsScreen.kt`, create a new private `@Composable` function named `SettingsCardItem`.
1.  It should accept the following parameters: `title: String`, `subtitle: String`, `icon: ImageVector`, and `onClick: () -> Unit`.
2.  The root composable must be a `Card` with `modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick)`.
3.  Inside the `Card`, use a `Row` with `verticalAlignment = Alignment.CenterVertically` and a `padding` of `16.dp`.
4.  The `Row` should contain:
    *   An `Icon` for the `icon` parameter.
    *   A `Spacer` of `16.dp`.
    *   A `Column` with `modifier = Modifier.weight(1f)`. This column will contain two `Text` composables: one for the `title` (using `MaterialTheme.typography.bodyLarge`) and one for the `subtitle` (using `MaterialTheme.typography.bodySmall` and `MaterialTheme.colorScheme.onSurfaceVariant`).
    *   An `Icon` for `Icons.AutoMirrored.Filled.KeyboardArrowRight` to indicate it's clickable."

---

### Step 24.3: Rebuild the Settings Screen with the New Components
**Goal:** Replace the old `LazyColumn` content with a new layout structured with headers and cards.
**Context:** The `SettingsScreen.kt` file contains the main UI. The `SettingsViewModel` already provides the necessary user and settings data.
**Prompt:**
"Modify the main `SettingsScreen` composable in `com.vpk.sprachninja.presentation.ui.view.SettingsScreen.kt`.
1.  Inside the `Scaffold`, replace the existing `LazyColumn` content entirely.
2.  Rebuild the `LazyColumn` content using the new components:
    *   Call `SettingsHeader(text = "Account")`.
    *   Call `SettingsCardItem` for the "My Level" setting. Use `Icons.Default.Person` for the icon, the user's name for the title, and their `germanLevel` for the subtitle. Wire the `onClick` to the existing dialog logic.
    *   Call `SettingsHeader(text = "API Configuration")`.
    *   Call `SettingsCardItem` for the "Gemini API Key" setting. Use `Icons.Default.Key` for the icon and a masked version of the API key for the subtitle. Wire its `onClick` to the API key dialog.
    *   Call `SettingsHeader(text = "Legal")`.
    *   Call `SettingsCardItem` for "Terms & Conditions", "Privacy Policy", and "Data Protection", using appropriate icons (`Description`, `Shield`, `Policy`) and wiring their `onClick` handlers to navigate to the `LegalActivity`."