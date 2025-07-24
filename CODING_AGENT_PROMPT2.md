# LLM_PROMPT_DETAILED2.md

This document provides the next granular, iterative sequence of prompts to continue building the **SprachNinja** application. It assumes all steps from previous prompt files have been successfully implemented.

**Core Context for all prompts:**
*   **Project Name:** SprachNinja
*   **Package Name:** `com.vpk.sprachninja`
*   **Current State:** The app has a stable onboarding flow, a home screen with a non-functional "Start Learning" button, and a non-interactive settings screen. All activities have a `TopAppBar`.
*   **Architecture:** Clean Architecture with MVVM, Jetpack Compose, and a manual DI container.

---

## Phase 11: Interactive Settings Dialog

### Step 11.1: Create a Reusable Dialog Composable
**Goal:** Build a generic `AlertDialog` for settings input.
**Context:** The project has a `presentation/ui/view` package.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create a new file `SettingsDialog.kt`.
1.  Define a new `@Composable` function `SettingsDialog`.
2.  It should accept parameters: `onDismissRequest: () -> Unit`, `onConfirmation: (apiKey: String, modelName: String) -> Unit`, `initialApiKey: String`, `initialModelName: String`.
3.  The composable should contain an `AlertDialog`.
4.  Inside the `AlertDialog`, use `remember` to manage the state of two `TextField`s, one for the API Key and one for the Model Name, pre-filled with the initial values.
5.  Provide "Cancel" and "Save" `TextButton`s. "Cancel" should call `onDismissRequest`. "Save" should call `onConfirmation` with the current text field values, then call `onDismissRequest`."

### Step 11.2: Integrate the Dialog into the Settings Screen
**Goal:** Use the new dialog to make the "Gemini API Key" setting interactive.
**Context:** `SettingsScreen.kt` and `SettingsDialog.kt` exist.
**Prompt:**
"Modify `SettingsScreen.kt`.
1.  Inside the `SettingsScreen` composable, create a mutable state to control the dialog's visibility: `var showDialog by remember { mutableStateOf(false) }`.
2.  In the `onClick` for the "Gemini API Key" `SettingsItem`, set `showDialog = true`.
3.  Add an `if (showDialog)` block. Inside it, call your `SettingsDialog` composable.
4.  For the `onDismissRequest` lambda, set `showDialog = false`.
5.  For the `onConfirmation` lambda, call `viewModel.saveSettings(apiKey, modelName)`."

---

## Phase 12: Static Content Screens (Legal)

### Step 12.1: Add Legal Text to `strings.xml`
**Goal:** Add placeholder legal text to the app's string resources.
**Context:** The `res/values/strings.xml` file exists.
**Prompt:**
"Open `app/src/main/res/values/strings.xml` and add three new string resources:
1.  `terms_and_conditions_content` (with placeholder text about local storage).
2.  `privacy_policy_content` (with placeholder text about no data collection).
3.  `data_protection_content` (with placeholder text about on-device storage)."

### Step 12.2: Create a Reusable `LegalInfoScreen` Composable
**Goal:** Build a generic screen to display text content.
**Context:** The `presentation/ui/view` package exists.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create a new file `LegalInfoScreen.kt`.
1.  Define a `@Composable` function `LegalInfoScreen`.
2.  It should accept `@StringRes titleResId: Int` and `@StringRes contentResId: Int`, and a lambda `onNavigateUp: () -> Unit`.
3.  The UI should use a `Scaffold` with a `TopAppBar` that displays the `titleResId` and has a back navigation icon that calls `onNavigateUp`.
4.  The content should be a `LazyColumn` showing the text from `contentResId`."

### Step 12.3: Create and Declare `LegalActivity`
**Goal:** Create an Activity to host the `LegalInfoScreen` and declare it in the manifest.
**Context:** `LegalInfoScreen.kt` exists.
**Prompt:**
1. In `com.vpk.sprachninja.presentation.ui.view`, create `LegalActivity.kt`. It should read `Intent` extras for a title resource ID and a content resource ID. In `setContent`, call `LegalInfoScreen`, passing the retrieved IDs and a lambda to `finish()` the activity.
2. Declare `LegalActivity` in `AndroidManifest.xml` with `android:exported="false"`.

### Step 12.4: Navigate to `LegalActivity` from Settings
**Goal:** Make the legal links in the settings screen functional.
**Context:** `SettingsScreen.kt` has `TODOs` for navigation.
**Prompt:**
"In `SettingsScreen.kt`, update the `onClick` lambdas for the three legal `SettingsItem`s. Each `onClick` should create an `Intent` for `LegalActivity`, use `putExtra` to add the correct title string resource (e.g., `R.string.terms_and_conditions_title`) and content string resource ID (`R.string.terms_and_conditions_content`), and then launch the intent."

---

## Phase 13: Networking and Gemini API Setup

### Step 13.1: Add Networking Dependencies
**Goal:** Add Retrofit and Kotlinx Serialization libraries to the project.
**Context:** The `build.gradle.kts` and `libs.versions.toml` files exist.
**Prompt:**
"Update your Gradle files:
1. In `libs.versions.toml`, add versions and library aliases for `retrofit-core`, `retrofit-converter-kotlinx-serialization`, and `kotlinx-serialization-json`.
2. In the app's `build.gradle.kts`, apply the `kotlinx-serialization` plugin and implement the new libraries."

### Step 13.2: Create Gemini API Data Models
**Goal:** Define the data classes for the Gemini API request and response.
**Context:** Kotlinx Serialization is now a dependency.
**Prompt:**
"In a new package `com.vpk.sprachninja.data.remote.dto`, create a file `GeminiDto.kt`.
1. Define a `@Serializable` data class `GeminiRequest` containing a list of `Content` objects.
2. Define a `@Serializable` data class `Content` containing a list of `Part` objects.
3. Define a `@Serializable` data class `Part` containing a `String` property named `text`.
4. Define `@Serializable` data classes for the `GeminiResponse`, mirroring the expected JSON structure (candidates -> content -> parts -> text)."

### Step 13.3: Create the `GeminiApiService` Interface
**Goal:** Define the Retrofit interface for the Gemini API.
**Context:** Retrofit dependencies and API data models exist.
**Prompt:**
"In `com.vpk.sprachninja.data.remote`, create a Retrofit interface `GeminiApiService`.
1. Define a `suspend` function `generateContent`.
2. It should `POST` to the endpoint `v1beta/models/{model}:generateContent`.
3. It must accept a `@Path("model") model: String`, a `@Query("key") apiKey: String`, and a `@Body request: GeminiRequest`.
4. The function should return a `GeminiResponse`."

### Step 13.4: Add Retrofit to `AppContainer`
**Goal:** Configure and provide the Retrofit and `GeminiApiService` instances via DI.
**Context:** `AppContainer` exists, `GeminiApiService` is defined.
**Prompt:**
"Update `AppContainer.kt`:
1. Add a `lazy` property to create and configure a `Retrofit` instance with the base URL `https://generativelanguage.googleapis.com/` and the `KotlinxSerializationConverterFactory`.
2. Add a `lazy` property to create the `GeminiApiService` using `retrofit.create()`."

### Step 13.5: Create the `GeminiRepository`
**Goal:** Create the repository to abstract away the Gemini API call.
**Context:** `GeminiApiService` and `SettingsRepository` are available in `AppContainer`.
**Prompt:**
1. In `com.vpk.sprachninja.domain.repository`, create an interface `GeminiRepository` with a single method: `suspend fun generateQuestion(prompt: String): Result<String>`.
2. In `com.vpk.sprachninja.data.repository`, create `GeminiRepositoryImpl` which implements the interface. Its constructor should take `GeminiApiService` and `SettingsRepository`.
3. In `generateQuestion`, get the API key and model name from `SettingsRepository`. If the key is blank, return `Result.failure`. Otherwise, build the request, call the service, parse the response, and return the result wrapped in a `Result` object.

### Step 13.6: Wire `GeminiRepository` in `AppContainer`
**Goal:** Provide the `GeminiRepository` via DI.
**Context:** `AppContainer` provides the necessary dependencies (`GeminiApiService`, `SettingsRepository`).
**Prompt:**
"Update `AppContainer.kt`. Add a public `lazy` property for the `GeminiRepository` interface, initializing it with `GeminiRepositoryImpl` and passing its required dependencies from the container."

---

## Phase 14: Q&A Feature - Final Implementation

### Step 14.1: Create `QuestionUiState` and `QuestionAnswerViewModel`
**Goal:** Create the ViewModel and its state representation for the Q&A screen.
**Context:** `GeminiRepository` and `UserRepository` are available.
**Prompt:**
1. In `com.vpk.sprachninja.presentation.viewmodel`, create a sealed interface `QuestionUiState` with `Loading`, `Success(question: String)`, and `Error(message: String)` states.
2. In the same package, create `QuestionAnswerViewModel`. It should take `GeminiRepository`, `UserRepository`, and `Context` in its constructor.
3. Expose a `StateFlow<QuestionUiState>`.
4. Create a function `loadNextQuestion()` that reads the user's level, picks a topic from the `german_levels_structure.json`, constructs a prompt, calls `GeminiRepository`, and updates the `QuestionUiState`.

### Step 14.2: Update `ViewModelFactory` for `QuestionAnswerViewModel`
**Goal:** Teach the factory how to create the new ViewModel.
**Context:** `ViewModelFactory` and `QuestionAnswerViewModel` exist.
**Prompt:**
"Update `ViewModelFactory.kt`. Add a new `when` branch for `QuestionAnswerViewModel::class.java`, constructing it with its required dependencies from the `AppContainer`."

### Step 14.3: Create `QuestionAnswerActivity` and `Screen`
**Goal:** Build the final UI and Activity for the Q&A flow.
**Context:** The `QuestionAnswerViewModel` is ready.
**Prompt:**
"1. In `com.vpk.sprachninja.presentation.ui.view`, create `QuestionAnswerActivity.kt`. It should instantiate the `QuestionAnswerViewModel` and host the `QuestionAnswerScreen`.
2. In the same package, create `QuestionAnswerScreen.kt`. It should use a `Scaffold`, observe the `QuestionUiState`, and display the UI for each state (Loading, Error, Success). The success state should include a `Text` for the question, an `OutlinedTextField` for the answer, and a `Button` to submit.
3. Declare `QuestionAnswerActivity` in `AndroidManifest.xml`."

### Step 14.4: Finalize Navigation
**Goal:** Connect the "Start Learning" button to the new Q&A feature.
**Context:** The `WelcomeScreen` has a `TODO` on its "Start Learning" button.
**Prompt:**
"Modify the `WelcomeScreen` composable in `HomeActivity.kt`. Find the 'Start Learning' `Button` and update its `onClick` lambda to launch `QuestionAnswerActivity`."