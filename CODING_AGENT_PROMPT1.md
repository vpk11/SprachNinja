This document provides the next granular, iterative sequence of prompts to continue building the **SprachNinja** application. It assumes all steps from `LLM_PROMPT_DETAILED.md` have been successfully implemented and the application is in a stable state.

**Core Context for all prompts:**
*   **Project Name:** SprachNinja
*   **Package Name:** `com.vpk.sprachninja`
*   **Current State:** The app has a functional theme, DI container, and a complete onboarding flow. `HomeActivity` correctly routes new users to `OnboardingActivity` and welcomes existing users.
*   **Architecture:** Clean Architecture with MVVM, Jetpack Compose, and a manual DI container.

---

## Phase 7: Home Screen Enhancement

### Step 7.1: Enhance the Welcome Screen
**Goal:** Update the `HomeActivity`'s `WelcomeScreen` to include a "Start Learning" button and a settings icon, as per the implementation plan.
**Context:** `HomeActivity.kt` currently shows a simple welcome text.
**Prompt:**
"Modify the `WelcomeScreen` composable inside `HomeActivity.kt`.
1.  Wrap the existing `Text` in a `Column` with `verticalArrangement = Arrangement.Center` and `horizontalAlignment = Alignment.CenterHorizontally`.
2.  Below the `Text`, add a `Spacer` of `32.dp`.
3.  Add a `Button` with the text "Start Learning". For now, its `onClick` can be empty or contain a `// TODO: Navigate to QuestionAnswerActivity`.
4.  In the `Box` that contains the `Column`, add an `IconButton` at the `TopEnd` alignment. Use the built-in `Icons.Filled.Settings` icon. Its `onClick` can also be a `// TODO: Navigate to SettingsActivity`."

---

## Phase 8: Settings Feature - Data and Domain Layer

### Step 8.1: Define Settings Data Model and Repository Interface
**Goal:** Create the data class for app settings and the domain-layer contract for accessing them.
**Context:** The project has a `domain/repository` package.
**Prompt:**
"1. In a new package `com.vpk.sprachninja.domain.model`, create a data class `AppSettings` with two `String` properties: `apiKey` and `modelName`.
2. In the `com.vpk.sprachninja.domain.repository` package, create an interface `SettingsRepository`. It should define two methods:
    *   `suspend fun saveSettings(appSettings: AppSettings)`
    *   `fun getSettings(): Flow<AppSettings>`"

### Step 8.2: Implement the Settings Repository
**Goal:** Create the data-layer implementation for `SettingsRepository` using `EncryptedSharedPreferences`.
**Context:** The `SettingsRepository` interface exists. The `security-crypto` dependency is in the project.
**Prompt:**
"In the `com.vpk.sprachninja.data.repository` package, create `SettingsRepositoryImpl`.
1.  It must implement `SettingsRepository`.
2.  Its constructor should take a `Context`.
3.  Inside the class, create an instance of `EncryptedSharedPreferences`.
4.  Implement `saveSettings` to save the API key and model name to the encrypted preferences using keys like `KEY_API_KEY` and `KEY_MODEL_NAME`.
5.  Implement `getSettings` to return a `Flow` that reads from the preferences and emits an `AppSettings` object. The `modelName` should default to `gemini-1.5-flash` if it's not found in preferences."

### Step 8.3: Create Settings Use Cases
**Goal:** Encapsulate the business logic for getting and saving settings.
**Context:** `SettingsRepository` interface exists.
**Prompt:**
"In the `com.vpk.sprachninja.domain.usecase` package, create two new files:
1.  `GetSettingsUseCase.kt`: Create a class `GetSettingsUseCase` that takes `SettingsRepository` in its constructor and has a single `operator fun invoke(): Flow<AppSettings>` method.
2.  `SaveSettingsUseCase.kt`: Create a class `SaveSettingsUseCase` that takes `SettingsRepository` in its constructor and has a single `suspend operator fun invoke(appSettings: AppSettings)` method."

### Step 8.4: Wire Settings Dependencies in AppContainer
**Goal:** Update the DI container to provide the new `SettingsRepository`.
**Context:** The `AppContainer` class is defined and already provides `UserRepository`.
**Prompt:**
"Update the `AppContainer` class in `com.vpk.sprachninja.di`. Add a new public `lazy` property for the `SettingsRepository` interface, initializing it with `SettingsRepositoryImpl` and passing the application context to its constructor."

---

## Phase 9: Settings Feature - Presentation Layer

### Step 9.1: Create the Settings ViewModel
**Goal:** Create a ViewModel to manage the state and logic for the settings screen.
**Context:** `GetSettingsUseCase` and `SaveSettingsUseCase` exist.
**Prompt:**
"In `com.vpk.sprachninja.presentation.viewmodel`, create `SettingsViewModel`.
1.  It should inherit from `ViewModel`.
2.  Its constructor should take `GetSettingsUseCase` and `SaveSettingsUseCase`.
3.  Expose a `StateFlow<AppSettings>` named `settings` that is initialized by collecting from the `GetSettingsUseCase`.
4.  Create a public `suspend fun saveSettings(apiKey: String, modelName: String)` function that validates the inputs and calls the `saveSettingsUseCase`."

### Step 9.2: Update the ViewModelFactory for Settings
**Goal:** Teach the factory how to create the `SettingsViewModel`.
**Context:** `ViewModelFactory` exists and already creates other ViewModels.
**Prompt:**
"Update the `ViewModelFactory` class in `com.vpk.sprachninja.presentation.viewmodel`. Add a new `when` branch for `SettingsViewModel::class.java`. Inside this branch, construct and return a `SettingsViewModel`, providing it with the `GetSettingsUseCase` and `SaveSettingsUseCase` (which you will create by passing the `settingsRepository` from the `appContainer`)."

### Step 9.3: Create the Settings UI Screen
**Goal:** Build the Jetpack Compose UI for the settings screen.
**Context:** The `SettingsViewModel` is defined.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create `SettingsScreen.kt`.
1.  Define a `@Composable` function `SettingsScreen` that accepts a `SettingsViewModel`.
2.  Wrap the content in `SprachNinjaTheme`.
3.  The UI should have a `Scaffold` with a `TopAppBar` titled "Settings".
4.  The main content should be a `LazyColumn`. Add four items:
    *   A clickable row for "Gemini API Key". For now, it can just show the key from the ViewModel's state. Add a `TODO` in the `onClick` to open a dialog.
    *   A simple, non-clickable row for "Terms and Conditions".
    *   A simple, non-clickable row for "Privacy Policy".
    *   A simple, non-clickable row for "Data Protection"."

---

## Phase 10: Navigation and Final Setup

### Step 10.1: Create the Settings Activity
**Goal:** Create an Activity to host the `SettingsScreen` composable.
**Context:** `SettingsScreen`, `SettingsViewModel`, and `ViewModelFactory` are complete.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create `SettingsActivity.kt`.
1.  It must extend `ComponentActivity`.
2.  In `onCreate`, retrieve the `AppContainer`.
3.  Instantiate the `SettingsViewModel` using the `by viewModels` delegate and your custom `ViewModelFactory`.
4.  Set the content to the `SettingsScreen` composable, passing the `SettingsViewModel` instance to it."

### Step 10.2: Implement Navigation to Settings
**Goal:** Make the settings icon on the home screen functional.
**Context:** The `IconButton` in `WelcomeScreen` has a `TODO` for navigation. `SettingsActivity` now exists.
**Prompt:**
"Modify the `WelcomeScreen` composable in `HomeActivity.kt`. Find the `IconButton` for settings. In its `onClick` lambda, get the `LocalContext` and use it to create and launch an `Intent` for `SettingsActivity`."

### Step 10.3: Declare SettingsActivity in Manifest
**Goal:** Register the new `SettingsActivity` with the Android system to prevent crashes.
**Context:** `SettingsActivity` has been created.
**Prompt:**
"Open `app/src/main/AndroidManifest.xml` and add a new `<activity>` declaration for `SettingsActivity` inside the `<application>` block. Ensure you set `android:exported="false"` since this activity will only be launched from within your app."

### Step 10.4: Add German Levels JSON Resource
**Goal:** Add the curriculum JSON file to the project resources for future use.
**Context:** The `res` directory exists.
**Prompt:**
1. In the `app/src/main/res/` directory, create a new directory named `raw` if it does not already exist.
2. Inside `res/raw`, create a new file named `german_levels_structure.json`.
3. Copy the entire JSON content from the `IMPLEMENTATION_PLAN.md` (section 6) and paste it into this new file.