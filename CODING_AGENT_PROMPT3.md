This document provides the next granular, iterative sequence of prompts to continue building the **SprachNinja** application. It assumes all steps from `LLM_PROMPT_DETAILED.md` and `LLM_PROMPT_DETAILED1.md` have been successfully implemented and the application is stable.

**Core Context for all prompts:**
*   **Project Name:** SprachNinja
*   **Package Name:** `com.vpk.sprachninja`
*   **Current State:** The app has a stable onboarding flow, a home screen, an interactive settings screen for the API key, and a basic (but flawed) question generation flow.
*   **Architecture:** Clean Architecture with MVVM, Jetpack Compose, and a manual DI container.

---

## Phase 15: Enhanced Question Generation & Answer-Checking

### Step 15.1: Create a Structured `PracticeQuestion` Model
**Goal:** Define a data class to hold a structured question, moving away from a simple string.
**Context:** We need to know the question, the answer, and the type of question.
**Prompt:**
"In the `com.vpk.sprachninja.domain.model` package, create a new data class `PracticeQuestion`.
1.  Make it `@Serializable` so it can be parsed from a JSON string.
2.  It should have three properties:
    *   `questionText: String` (e.g., "Ich gehe ___ Kino.")
    *   `correctAnswer: String` (e.g., "ins")
    *   `questionType: String` (e.g., "FILL_IN_THE_BLANK")"

### Step 15.2: Refine the Gemini Repository and Prompt
**Goal:** Update the repository to fetch a structured `PracticeQuestion` and improve the prompt sent to Gemini.
**Context:** `GeminiRepository.kt` currently returns `Result<String>`.
**Prompt:**
"This is a two-part task for `GeminiRepositoryImpl.kt`:
1.  **Update the Interface:** In `domain/repository/GeminiRepository.kt`, change the `generateQuestion` function signature to return `Result<com.vpk.sprachninja.domain.model.PracticeQuestion>`.
2.  **Update the Implementation:** In `data/repository/GeminiRepositoryImpl.kt`, modify the `generateQuestion` method:
    *   Construct a much more detailed prompt. The new prompt must ask Gemini to focus on grammar or vocabulary and to return its response as a JSON object matching the `PracticeQuestion` data class structure. Example prompt snippet: `'...return ONLY a JSON object with keys "questionText", "correctAnswer", and "questionType".'`
    *   After receiving the text response from Gemini, use `Json.decodeFromString<PracticeQuestion>(generatedText)` to parse it.
    *   Return `Result.success(parsedQuestion)` or `Result.failure` if parsing fails."

### Step 15.3: Update ViewModel for Answer Checking
**Goal:** Modify the `QuestionAnswerViewModel` to handle the new `PracticeQuestion` model and add state for answer validation.
**Context:** `QuestionAnswerViewModel.kt` uses the old `QuestionUiState`.
**Prompt:**
"Modify the Q&A presentation layer:
1.  **Update UI State:** In `QuestionUiState.kt`, change the `Success` state to hold a `PracticeQuestion` object: `data class Success(val question: PracticeQuestion) : QuestionUiState`.
2.  **Update ViewModel:** In `QuestionAnswerViewModel.kt`:
    *   Add a new `MutableStateFlow<String>` for the `userAnswer`.
    *   Add a new `enum class ValidationState { UNCHECKED, CORRECT, INCORRECT }` and expose a `StateFlow` for it, defaulting to `UNCHECKED`.
    *   Create a new public function `checkAnswer()`. It should compare `userAnswer.value` (case-insensitively) with the `correctAnswer` from the current `PracticeQuestion` and update the `validationState` flow accordingly.
    *   When loading a new question, reset `userAnswer` to `""` and `validationState` to `UNCHECKED`."

### Step 15.4: Update the Q&A Screen UI
**Goal:** Modify the UI to display validation results and the correct answer if wrong.
**Context:** `QuestionAnswerScreen.kt` needs to reflect the new validation state.
**Prompt:**
"Modify `QuestionAnswerScreen.kt`:
1.  Update the `SuccessState` composable to accept the `validationState` and the `userAnswer` state from the ViewModel, plus the `onCheckAnswer` lambda.
2.  Change the `Button` text to "Check Answer" and have its `onClick` call `onCheckAnswer`. It should be disabled when the answer is not `UNCHECKED`.
3.  Use the `validationState` to change the `OutlinedTextField`'s border color or show an icon (`Icons.Filled.Check` or `Icons.Filled.Close`).
4.  If the state is `INCORRECT`, display a `Text` composable below the text field showing "Correct answer: [correct answer]".
5.  Add a "Next Question" `TextButton` that is only visible when the answer state is `CORRECT` or `INCORRECT`. Its `onClick` should call `viewModel.loadNextQuestion()`."

---

## Phase 16: Prevent Question Repetition

### Step 16.1: Create `RecentQuestion` Entity and DAO
**Goal:** Create the Room database components to store recently asked questions.
**Context:** The project has a fully functional Room setup for the `User` entity.
**Prompt:**
"1.  In `com.vpk.sprachninja.data.local`, create a new `@Entity` data class `RecentQuestion` with properties `id: Int` (PrimaryKey), `questionText: String`, and `userLevel: String`.
2.  In the same package, create a `@Dao` interface `RecentQuestionDao` with two methods: `suspend fun insert(question: RecentQuestion)` and `@Query("SELECT * FROM recentquestion WHERE userLevel = :level ORDER BY id DESC LIMIT 20") fun getRecentQuestionsForLevel(level: String): List<RecentQuestion>`."

### Step 16.2: Update `AppDatabase` and Create Repository
**Goal:** Integrate the new entity into the database and create its repository.
**Context:** `AppDatabase.kt` and the repository pattern are established.
**Prompt:**
"1.  In `AppDatabase.kt`, add `RecentQuestion::class` to the `entities` array in the `@Database` annotation and increment the database `version` number. Add `.fallbackToDestructiveMigration()` in the `Room.databaseBuilder` for now. Add an abstract function to get the `RecentQuestionDao`.
2.  Create a `RecentQuestionRepository` interface and its `RecentQuestionRepositoryImpl` class, following the existing pattern.
3.  In `AppContainer.kt`, wire up the `RecentQuestionRepository` so it can be injected."

### Step 16.3: Update ViewModel to Avoid Repetition
**Goal:** Use the new repository to prevent asking the same questions.
**Context:** `QuestionAnswerViewModel.kt` generates prompts.
**Prompt:**
"Modify `QuestionAnswerViewModel.kt`:
1.  Add `RecentQuestionRepository` to its constructor and update the `ViewModelFactory` accordingly.
2.  In the `loadNextQuestion` function, before building the prompt, fetch the list of recent questions using the new repository.
3.  Modify the Gemini prompt to include an instruction like: `'CRITICAL: Do not generate any of the following questions again: [list of recent question texts]'`.
4.  After successfully receiving and parsing a new question from Gemini, call `recentQuestionRepository.insert()` to save it to the database."

---

## Phase 17: User Level Management

### Step 17.1: Add `updateUserLevel` to `UserRepository`
**Goal:** Create the data access logic to change a user's level.
**Context:** `UserDao` and `UserRepositoryImpl` exist.
**Prompt:**
"1.  In `UserDao.kt`, add a new `suspend` function: `@Query("UPDATE user SET germanLevel = :newLevel") suspend fun updateUserLevel(newLevel: String)`.
2.  In the `UserRepository` interface, add a corresponding `suspend fun updateUserLevel(newLevel: String)` method.
3.  Implement this new method in `UserRepositoryImpl` by calling the DAO function."

### Step 17.2: Create `UpdateUserLevelUseCase`
**Goal:** Encapsulate the business logic for changing the user's level.
**Context:** The use case pattern is established.
**Prompt:**
"In `com.vpk.sprachninja.domain.usecase`, create a new class `UpdateUserLevelUseCase`. It should take a `UserRepository` in its constructor and have a single `suspend operator fun invoke(newLevel: String)` method that calls the repository's `updateUserLevel` function."

### Step 17.3: Add Level Selector UI to Settings
**Goal:** Allow users to see and change their level from the settings screen.
**Context:** `SettingsScreen.kt` exists.
**Prompt:**
"Modify `SettingsScreen.kt`:
1.  Add a new `SettingsItem` to the `LazyColumn`, titled "My Level". Its subtitle should display the current user's level. (You will need to fetch the `User` object in `SettingsViewModel` for this).
2.  When this item is clicked, show a dialog. The dialog should present a list of all available German levels (e.g., A1.1, A1.2, ... B2.2). You can hardcode this list for now.
3.  When a level is selected from the dialog, call a new function in the `SettingsViewModel` to update it."

### Step 17.4: Implement Level Change in `SettingsViewModel`
**Goal:** Add the logic to the ViewModel to handle the level update.
**Context:** `SettingsViewModel.kt` exists.
**Prompt:**
"Modify `SettingsViewModel.kt`:
1.  Add `UserRepository` and `UpdateUserLevelUseCase` to its constructor and update the `ViewModelFactory`.
2.  Expose a `StateFlow<User?>` by collecting from `userRepository.getUser()`.
3.  Create a new `suspend` function `updateUserLevel(newLevel: String)` that calls the `updateUserLevelUseCase`."