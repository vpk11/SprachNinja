## 1. Project Overview

This document outlines the implementation plan for a German language learning Android application named **SprachNinja**. The app will leverage Google's Gemini API for generating personalized questionnaires, use modern Android development tools and practices, and store all user data locally on the device.

The architecture will follow the MVVM pattern with a clean, layered structure, utilizing Jetpack Compose for the UI, a manual container for dependency injection, and Room for local data persistence.

### 1.1. Core Features

*   **User Onboarding & Profile:** A simple initial setup for username and German level. A dedicated profile page allows users to manage their level and view statistics.
*   **Dynamic Learning Modes:**
    *   **Grammar & Vocabulary:** Fill-in-the-blank style questions.
    *   **Translation (EN -> DE):** Practice translating sentences to German.
    *   **Learn Words:** Multiple-choice questions to build vocabulary.
*   **AI-Powered Content:**
    *   **Personalized Questions:** Gemini generates questions based on the user's level, selected topic, and practice history to avoid repetition.
    *   **Intelligent Validation:** For translation questions, Gemini provides contextual feedback, accepting semantically correct answers even if they don't match the expected answer exactly.
    *   **Tip of the Day:** A daily, cached tip about German language or culture tailored to the user's level.
*   **Progress & Statistics Tracking:** The app records correct and incorrect answers for each level, displaying them on the user's profile.
*   **Modern User Experience:**
    *   A dynamic home screen dashboard with a user summary, practice mode selection, and the tip of the day.
    *   A modern, card-based settings screen.
    *   A keyboard-aware UI in the practice screen to prevent input fields from being hidden.
*   **User-Managed API Key:** A settings screen for users to enter their own Gemini API key and select a model.
*   **Local-First Data & Privacy:** All user data (profile, progress, stats, API key) is stored exclusively and securely on the device. No backend server or data collection.

## 2. Architecture and Technology Stack

The app will be built using a modern Android architecture to ensure it is scalable, maintainable, and testable.

*   **Overall Architecture:** Clean Architecture with MVVM (Model-View-ViewModel).
    *   **Presentation Layer:** Jetpack Compose for UI, ViewModels for state management, and Activities as entry points.
    *   **Domain Layer:** Use cases encapsulating business logic (e.g., `GenerateQuestionUseCase`). This layer is pure Kotlin.
    *   **Data Layer:** Repositories implementing interfaces from the domain layer, managing data from local storage (Room) and remote services (Retrofit/Gemini API).
*   **Technology Stack:**
    *   **Language:** Kotlin
    *   **UI Toolkit:** Jetpack Compose & Material 3
    *   **Asynchronous Programming:** Kotlin Coroutines & Flow
    *   **Dependency Injection:** Manual DI Container
    *   **Local Storage:** Room Database
    *   **Secure Storage:** EncryptedSharedPreferences (for API Key and cached data)
    *   **Networking:** Retrofit with Kotlinx Serialization Converter
    *   **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)

## 3. Project Structure

The project is organized into packages that reflect the clean architecture layers.

```
/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/vpk/sprachninja/
│       │   ├── di/
│       │   │   └── AppContainer.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   ├── repository/ (Interfaces)
│       │   │   └── usecase/
│       │   ├── data/
│       │   │   ├── local/ (Room Database, DAOs, Entities)
│       │   │   ├── remote/ (Gemini API service, DTOs)
│       │   │   └── repository/ (Implementations)
│       │   ├── presentation/
│       │   │   ├── ui/
│       │   │   │   ├── theme/
│       │   │   │   └── view/ (Activities and Composable screens)
│       │   │   └── viewmodel/ (ViewModel classes and custom ViewModelFactory)
│       │   └── SprachNinjaApp.kt (Application class)
│       └── res/
│           ├── values/ (strings.xml, colors.xml, etc.)
│           └── raw/
│               └── german_levels_structure.json
```

## 4. Dependency Injection (Manual)

A manual DI container provides simplicity and full control over object creation.

1.  **`AppContainer.kt`:** A class that creates and holds dependencies. It is instantiated once in the `Application` class. It provides all repositories (User, Settings, Gemini, LevelStats, etc.).

2.  **`SprachNinjaApp.kt`:** The custom `Application` class holds the singleton instance of `AppContainer`.

3.  **`ViewModelFactory.kt`:** A custom factory provides dependencies from the `AppContainer` to our ViewModels.

    ```kotlin
    // In: /com/vpk/sprachninja/presentation/viewmodel/ViewModelFactory.kt
    class ViewModelFactory(private val appContainer: AppContainer, ...) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> { ... }
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> { ... }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> { ... }
                modelClass.isAssignableFrom(QuestionAnswerViewModel::class.java) -> {
                    // Example of passing extra params from an Activity
                    QuestionAnswerViewModel(
                        questionType = "...", // Passed from Activity
                        geminiRepository = appContainer.geminiRepository,
                        ...
                    ) as T
                }
                modelClass.isAssignableFrom(ProfileViewModel::class.java) -> { ... }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
    ```

## 5. Feature Implementation Plan

### Phase 1: Foundation and Theming
1.  **Project Setup:** Configure Gradle, plugins, and dependencies.
2.  **Theming:** Implement a dual-theme system using `isSystemInDarkTheme()`.
    *   **Dark Theme:** `SpaceGray` color palette.
    *   **Light Theme:** `Solarized` color palette.
3.  **DI Container:** Create the manual `AppContainer` and `ViewModelFactory` structure.

### Phase 2: User Onboarding & Profile Management
1.  **Data Layer:** Define `User` and `LevelStats` Room entities, DAOs, and the `AppDatabase`. Implement `UserRepository` and `LevelStatsRepository`.
2.  **Onboarding:** Create `OnboardingActivity` with a simple UI (`OnboardingScreen`) and `OnboardingViewModel` to save the initial user profile.
3.  **Profile:** Create `ProfileActivity` hosted by `ProfileScreen` and `ProfileViewModel`.
    *   Displays user's name and current level.
    *   Shows correct/wrong answer statistics for the current level.
    *   Allows the user to change their German level via a dialog.

### Phase 3: Core Learning Loop & Gemini Integration
1.  **Data Models:**
    *   `PracticeQuestion`: A serializable data class to hold the question (`questionText`), `correctAnswer`, `questionType`, and a nullable list of `options` for multiple-choice.
    *   `TranslationValidationResult`: A data class to hold the structured feedback (`isCorrect`, `feedback`) from the validation prompt.
2.  **Gemini Repository:** Implement `GeminiRepository` to handle all API interactions.
    *   `generateQuestion`: Builds a detailed prompt based on user level, a topic from `german_levels_structure.json`, and the selected `questionType`. It instructs Gemini to return a specific JSON structure matching the `PracticeQuestion` model. It also includes a list of recent questions to avoid repetition.
    *   `validateTranslation`: Builds a specific prompt asking Gemini to act as a language expert, evaluating a user's translation for semantic equivalence and grammatical correctness, returning a `TranslationValidationResult` JSON.
    *   `getDailyTip`: Fetches a short, level-appropriate language tip.
3.  **Repetition Prevention:** Create a `RecentQuestion` Room entity to store the last 20 questions asked. These are passed to the `generateQuestion` prompt to ensure variety.
4.  **Q&A Flow:** Create `QuestionAnswerActivity`.
    *   It receives the `questionType` as an Intent extra.
    *   The `QuestionAnswerViewModel` orchestrates the flow: fetches a question, validates the user's answer using the appropriate logic (simple check or Gemini validation), and updates the UI state (`Loading`, `Success`, `Error`).
    *   The `QuestionAnswerScreen` adapts its UI based on the `questionType`, showing a text field or multiple-choice buttons. It also displays validation status and feedback.
    *   The UI is scrollable to avoid being obscured by the on-screen keyboard.

### Phase 4: Home Screen Dashboard
1.  **Home Activity:** The main launcher activity. It checks if a user exists; if not, it redirects to `OnboardingActivity`.
2.  **Home View:** The `WelcomeScreen` composable, powered by `HomeViewModel`.
    *   Displays a "Willkommen, [username]!" message.
    *   Includes a `UserSummaryCard` with the user's name and current level.
    *   Features distinct `PracticeModeButton`s for each learning mode, which navigate to `QuestionAnswerActivity` with the correct `questionType`.
    *   Shows a "Tip of the Day" card. The tip is fetched once per day and cached in `EncryptedSharedPreferences` to minimize API calls.
    *   Provides icon-based navigation to the Profile and Settings screens.

### Phase 5: Settings and Legal
1.  **Data Layer:** Implement `SettingsRepository` using `EncryptedSharedPreferences` to securely store the user's Gemini API key.
2.  **Settings Screen:** `SettingsActivity` hosts a modern, card-based `SettingsScreen` built with reusable `SettingsCardItem` and `SettingsHeader` composables.
    *   **Account:** Change user level.
    *   **API Config:** Open a dialog to securely enter or update the Gemini API key.
    *   **Legal:** Navigate to a generic `LegalActivity`.
3.  **Legal Screens:** A reusable `LegalInfoScreen` displays text content from string resources for Terms & Conditions, Privacy Policy, etc.

## 6. German Level Structure JSON

This file will be placed in `res/raw/german_levels_structure.json` to provide a curriculum for question generation.

```json
{
  "levels": [
    {
      "level": "A1",
      "sub_levels": [
        {
          "sub_level": "A1.1",
          "topics": [
            "Greetings and Introductions",
            "The Alphabet and Pronunciation",
            "Numbers, Time, and Dates",
            "Basic Personal Information (Name, Origin, Residence)",
            "Family and Friends",
            "Hobbies and Leisure Activities",
            "Food and Drink (Ordering in a cafe)",
            "Grammar: Present Tense (Präsens) of regular verbs, 'sein', 'haben'",
            "Grammar: Nominative Case, Personal Pronouns",
            "Grammar: W-Questions (Wer, Was, Wo, Wie)"
          ]
        },
        // ... more sub-levels and topics ...
      ]
    },
    // ... more levels from A2 to B2 ...
  ]
}
```