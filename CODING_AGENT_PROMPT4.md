**Core Context for all prompts:**
*   **Project Name:** SprachNinja
*   **Package Name:** `com.vpk.sprachninja`
*   **Current State:** The app has a stable onboarding flow, a home screen, an interactive settings screen (API Key & Level Change), and a fill-in-the-blank practice session with answer checking and repetition prevention.
*   **Architecture:** Clean Architecture with MVVM, Jetpack Compose, and a manual DI container.

---

## Phase 18: Keyboard-Aware UI in Practice Session

### Step 18.1: Fix Keyboard Occlusion in Q&A Screen
**Goal:** Prevent the on-screen keyboard from hiding the "Check Answer" button.
**Context:** `QuestionAnswerScreen.kt` has a `Column` that doesn't scroll, causing the UI to be pushed off-screen by the keyboard.
**Prompt:**
"Modify the `SuccessState` composable in `com.vpk.sprachninja.presentation.ui.view.QuestionAnswerScreen.kt`.
1.  Add a `Modifier.verticalScroll(rememberScrollState())` to the main `Column`.
2.  To ensure the question text stays visible, change the `verticalArrangement` of the `Column` from `Arrangement.Center` to `Arrangement.Top` and add a `Spacer(Modifier.weight(1f))` between the question `Text` and the `OutlinedTextField`.
3.  Add another `Spacer(Modifier.weight(1f))` between the `Row` of buttons and the `OutlinedTextField` to push the content towards the center when the keyboard is not visible."

---

## Phase 19: Statistics & Profile Page - Data Layer

### Step 19.1: Create `LevelStats` Entity and DAO
**Goal:** Create the Room database components to store user statistics per level.
**Context:** The project has a functional Room setup.
**Prompt:**
"1.  In `com.vpk.sprachninja.data.local`, create a new `@Entity` data class `LevelStats`. It must have `germanLevel: String` as its `@PrimaryKey`, `correctCount: Int`, and `wrongCount: Int`.
2.  In the same package, create a `@Dao` interface `LevelStatsDao`. It should have three `suspend` functions:
    *   `@Query("SELECT * FROM levelstats WHERE germanLevel = :level") fun getStatsForLevel(level: String): LevelStats?`
    *   `@Upsert suspend fun upsertStats(stats: LevelStats)`
    *   `@Query("UPDATE levelstats SET correctCount = correctCount + 1 WHERE germanLevel = :level") suspend fun incrementCorrectCount(level: String)`
    *   `@Query("UPDATE levelstats SET wrongCount = wrongCount + 1 WHERE germanLevel = :level") suspend fun incrementWrongCount(level: String)`"

### Step 19.2: Update `AppDatabase` and Create `LevelStatsRepository`
**Goal:** Integrate the new entity into the database and create its repository.
**Context:** `AppDatabase.kt` and the repository pattern are established.
**Prompt:**
"1.  In `AppDatabase.kt`, add `LevelStats::class` to the `entities` array, increment the database `version` to 3, and add an abstract function to get the `LevelStatsDao`.
2.  In `com.vpk.sprachninja.domain.repository`, create a `LevelStatsRepository` interface with methods that mirror the DAO.
3.  In `com.vpk.sprachninja.data.repository`, create `LevelStatsRepositoryImpl` to implement the interface. In the increment/decrement methods, it should first check if stats for a level exist. If not, it should create a new `LevelStats` object before incrementing."

### Step 19.3: Wire `LevelStatsRepository` in `AppContainer`
**Goal:** Make the new repository available for dependency injection.
**Context:** The `AppContainer` class is defined.
**Prompt:**
"Update the `AppContainer` class in `com.vpk.sprachninja.di`. Add a new public `lazy` property for the `LevelStatsRepository` interface, initializing it with `LevelStatsRepositoryImpl` and passing it the `levelStatsDao` from the `appDatabase` instance."

### Step 19.4: Update ViewModel to Track Stats
**Goal:** Record correct and incorrect answers after the user checks their answer.
**Context:** `QuestionAnswerViewModel.kt` has a `checkAnswer` function.
**Prompt:**
"Modify `QuestionAnswerViewModel.kt`:
1.  Add `LevelStatsRepository` to its constructor and update the `ViewModelFactory` accordingly.
2.  In the `checkAnswer` function, after determining if the answer is correct or incorrect, get the current user's level.
3.  Call the appropriate method on the `levelStatsRepository` (`incrementCorrectCount` or `incrementWrongCount`) with the user's level."

---

## Phase 20: Statistics & Profile Page - Presentation Layer

### Step 20.1: Create `ProfileViewModel` and Update Factory
**Goal:** Create a ViewModel to provide data for the new profile screen.
**Context:** `UserRepository` and `LevelStatsRepository` are now available.
**Prompt:**
"1.  In `com.vpk.sprachninja.presentation.viewmodel`, create `ProfileViewModel`. Its constructor should take `UserRepository` and `LevelStatsRepository`.
2.  Inside the `ProfileViewModel`, expose a `StateFlow<User?>` by collecting from the user repository.
3.  Expose another `StateFlow<LevelStats?>`. Use the user flow with `flatMapLatest` to fetch the stats for the user's current level whenever the user object changes.
4.  Update `ViewModelFactory.kt` to be able to create `ProfileViewModel` with its dependencies."

### Step 20.2: Create `ProfileScreen` UI
**Goal:** Build the UI to display user information and statistics.
**Context:** `ProfileViewModel` is ready. The `LevelSelectorDialog` composable already exists.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create `ProfileScreen.kt`.
1.  The composable should accept a `ProfileViewModel` and an `onNavigateUp` lambda.
2.  Use a `Scaffold` with a `TopAppBar` titled "My Profile" and a back navigation icon.
3.  The content should be a `Column` that displays:
    *   The user's name.
    *   A clickable `SettingsItem` for "My Level" which shows the current level and opens the `LevelSelectorDialog` on click (you will need to add the dialog logic).
    *   A "Statistics for this level" section that shows `Correct Answers: [count]` and `Wrong Answers: [count]`."

### Step 20.3: Create and Navigate to `ProfileActivity`
**Goal:** Create an Activity to host the profile screen and add an entry point from the home screen.
**Context:** `ProfileScreen` is ready.
**Prompt:**
"1.  In `com.vpk.sprachninja.presentation.ui.view`, create `ProfileActivity.kt` to host the `ProfileScreen` and its `ViewModel`.
2.  Declare `ProfileActivity` in `AndroidManifest.xml` with `android:exported="false"`.
3.  In `HomeActivity.kt`, add a new `IconButton` to the `TopAppBar`'s `actions` using `Icons.Filled.AccountCircle`. Its `onClick` should launch `ProfileActivity`."

---

## Phase 21: Translation Question Type

### Step 21.1: Enhance Gemini Prompt for Question Variety
**Goal:** Update the repository to be able to request different types of questions.
**Context:** `GeminiRepositoryImpl.kt` has a hardcoded prompt for fill-in-the-blank questions.
**Prompt:**
"Refactor `GeminiRepositoryImpl.kt`:
1.  Modify the `generateQuestion` function in both the interface and implementation to accept a new parameter: `questionType: String` (e.g., "FILL_IN_THE_BLANK", "TRANSLATE_EN_DE").
2.  Inside the implementation, use a `when (questionType)` block to build a different `fullPrompt` for each type.
3.  For "TRANSLATE_EN_DE", the prompt should ask Gemini for a simple English sentence to translate to German and instruct it to return the JSON with `questionType` set to "TRANSLATE_EN_DE"."

### Step 21.2: Create a Practice Mode Selection Dialog
**Goal:** Allow the user to choose what kind of practice they want before starting a session.
**Context:** The "Start Learning" button in `HomeActivity` navigates directly.
**Prompt:**
"1.  In `com.vpk.sprachninja.presentation.ui.view`, create a new composable `PracticeModeDialog`. It should display two clickable options: "Grammar & Vocabulary (Fill-in-the-blank)" and "Translation (English to German)".
2.  Modify the `WelcomeScreen` in `HomeActivity.kt`. The "Start Learning" button should now open this new dialog.
3.  When a mode is selected in the dialog, it should launch `QuestionAnswerActivity`, passing the chosen question type (e.g., "FILL_IN_THE_BLANK") as an `Intent` extra."

### Step 21.3: Update Q&A ViewModel to Handle Practice Mode
**Goal:** Make the `QuestionAnswerViewModel` aware of the selected practice mode.
**Context:** The ViewModel currently only generates one type of question.
**Prompt:**
"1.  Modify `QuestionAnswerActivity` to read the `questionType` string from its `Intent` extras.
2.  Update `QuestionAnswerViewModel`'s constructor to accept this `questionType` string. Update `ViewModelFactory` to handle passing this parameter from the Activity. (This will require a custom factory instance created in the Activity).
3.  In `QuestionAnswerViewModel`'s `loadNextQuestion` function, pass the `questionType` to the `geminiRepository.generateQuestion` method."