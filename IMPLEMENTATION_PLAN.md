# IMPLEMENTATION PLAN

## 1. Project Overview

This document outlines the implementation plan for a German language learning Android application named **SprachNinja**. The app will leverage Google's Gemini API for generating personalized questionnaires, use modern Android development tools and practices, and store all user data locally on the device.

The architecture will follow the MVVM pattern with a clean, layered structure, utilizing Jetpack Compose for the UI, a manual container for dependency injection, and Room for local data persistence.

### 1.1. Core Features

*   **User Onboarding:** A simple questionnaire to determine the user's name and initial German level.
*   **Personalized Learning:** Gemini-generated questions based on the user's current level and progress, guided by a structured curriculum.
*   **Progress Tracking:** Local storage of user progress to avoid repetition and adapt difficulty.
*   **User-Managed API Key:** A settings screen for users to enter their own Gemini API key and select a model.
*   **Static Content Screens:** Standard legal information screens (Terms & Conditions, Privacy Policy, Data Protection).
*   **Local-First Data:** All user data is stored exclusively on the device. No backend server is required.

## 2. Architecture and Technology Stack

The app will be built using a modern Android architecture to ensure it is scalable, maintainable, and testable.

*   **Overall Architecture:** Clean Architecture with MVVM (Model-View-ViewModel).
    *   **Presentation Layer:** Jetpack Compose for UI, ViewModels for state management, and Activities as entry points.
    *   **Domain Layer:** Use cases encapsulating business logic (e.g., `GenerateQuestionUseCase`). This layer will be pure Kotlin.
    *   **Data Layer:** Repositories implementing interfaces from the domain layer, managing data from local storage (Room) and remote services (Gemini API).
*   **Technology Stack:**
    *   **Language:** Kotlin (Primary)
    *   **JDK:** OpenJDK 21
    *   **UI Toolkit:** Jetpack Compose
    *   **Asynchronous Programming:** Kotlin Coroutines & Flow
    *   **Dependency Injection:** Manual DI Container (No Hilt/Dagger)
    *   **Local Storage:** Room Database
    *   **Secure Storage:** EncryptedSharedPreferences (for API Key)
    *   **Networking:** A type-safe HTTP client like Retrofit or Ktor.
    *   **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)

## 3. Project Structure

The project will be organized into packages that reflect the clean architecture layers.

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
│       │   │   ├── remote/ (Gemini API service)
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

Instead of a framework like Hilt, we will use a manual DI container. This approach provides simplicity and full control over object creation.

1.  **`AppContainer.kt`:** A class that creates and holds dependencies. It will be instantiated once in the `Application` class.

    ```kotlin
    // In: /com/vpk/sprachninja/di/AppContainer.kt
    class AppContainer(private val context: Context) {
    
        // Room Database
        private val appDatabase: AppDatabase by lazy {
            AppDatabase.getDatabase(context)
        }
    
        // Repositories
        val userRepository: UserRepository by lazy {
            UserRepositoryImpl(appDatabase.userDao())
        }
        
        val settingsRepository: SettingsRepository by lazy {
            SettingsRepositoryImpl(context) // Uses EncryptedSharedPreferences
        }

        // Add other repositories and services here...
    }
    ```

2.  **`SprachNinjaApp.kt`:** The custom `Application` class will create and hold the singleton instance of `AppContainer`.

    ```kotlin
    // In: /com/vpk/sprachninja/SprachNinjaApp.kt
    class SprachNinjaApp : Application() {
        lateinit var appContainer: AppContainer
    
        override fun onCreate() {
            super.onCreate()
            appContainer = AppContainer(this)
        }
    }
    ```

3.  **`ViewModelFactory.kt`:** A custom factory to provide dependencies from the `AppContainer` to our ViewModels.

    ```kotlin
    // In: /com/vpk/sprachninja/presentation/viewmodel/ViewModelFactory.kt
    class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                    OnboardingViewModel(
                        saveUserUseCase = SaveUserUseCase(appContainer.userRepository)
                    ) as T
                }
                modelClass.isAssignableFrom(QuestionAnswerViewModel::class.java) -> {
                    QuestionAnswerViewModel(
                        generateQuestionUseCase = GenerateQuestionUseCase(/*...*/),
                        progressRepository = appContainer.progressRepository 
                    ) as T
                }
                // Add other ViewModels here...
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
    ```

4.  **ViewModel Instantiation:** In an `Activity` or `@Composable`, we retrieve the factory from the `Application` instance to create ViewModels.

    ```kotlin
    // Inside an Activity
    val appContainer = (application as SprachNinjaApp).appContainer
    val viewModel: OnboardingViewModel by viewModels { ViewModelFactory(appContainer) }
    ```

## 5. Feature Implementation Plan

### Phase 1: Core Setup and User Onboarding

1.  **Setup:** Configure Gradle, themes, and base project structure.
2.  **`OnboardingActivity`:**
    *   **UI:** A screen with a `TextField` for the username and a list/dropdown to select the initial German level.
    *   **ViewModel (`OnboardingViewModel`):** Manages UI state. Uses `SaveUserUseCase` to persist the initial user profile.
    *   **Data:** Create `User` entity, `UserDao`, `UserRepository`, and `SaveUserUseCase`.

### Phase 2: Question & Answer Flow

1.  **`HomeActivity`:**
    *   **Logic:** Checks if a user profile exists. If not, redirects to `OnboardingActivity`. Otherwise, displays a welcome message and a "Start Learning" button.
    *   **UI (`HomeScreen.kt`):** A simple `Column` with a `Text` and a `Button`.
2.  **`QuestionAnswerActivity`:**
    *   **UI (`QuestionAnswerScreen.kt`):** Displays the question, a dynamic input (text field or selectable cards), and control buttons.
    *   **ViewModel (`QuestionAnswerViewModel`):**
        *   Holds the current question and UI state in a `StateFlow`.
        *   Uses `GenerateQuestionUseCase` to build a prompt and call the Gemini API. The prompt will be constructed using the user's level and a topic from `german_levels_structure.json`.
        *   Uses `SaveProgressUseCase` to track completed topics locally.
    *   **Gemini Integration:**
        *   Create a `GeminiRepository` to handle API requests.
        *   **Example Prompt:** `"Generate a German A2.1 multiple-choice question about 'Dative Prepositions'. Provide the question, four options (one correct), and the correct answer in a JSON format: { \"question\": \"...\", \"options\": [\"...\", \"...\"], \"answer\": \"...\" }"`

### Phase 3: Settings and Legal Screens

1.  **`SettingsActivity`:**
    *   **UI (`SettingsScreen.kt`):** A `LazyColumn` of clickable items.
        *   **Gemini API Key:** Opens a dialog to enter/update the key and model name (default: `gemini-1.5-flash`).
        *   **Legal:** Items for "Terms," "Privacy," and "Data Protection" that navigate to a generic content screen.
    *   **ViewModel (`SettingsViewModel`):** Manages saving/retrieving settings via `SettingsRepository`.
    *   **Data:** Use `EncryptedSharedPreferences` within `SettingsRepositoryImpl` to securely store the API key.
2.  **Legal Screens:**
    *   **UI (`LegalInfoScreen.kt`):** A reusable composable that takes a title and a string resource ID for the content text.
    *   **Content:** Populate `strings.xml` with the required legal text, emphasizing that all data is stored locally and network calls to Gemini use the user's own API key.

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
        {
          "sub_level": "A1.2",
          "topics": [
            "Daily Routine and Activities",
            "Shopping and Prices",
            "Professions and Work",
            "Places in the City and Asking for Directions",
            "Grammar: Accusative Case",
            "Grammar: Modal Verbs (können, müssen, wollen)",
            "Grammar: Separable Verbs (trennbare Verben)",
            "Grammar: Imperative (Imperativ)"
          ]
        }
      ]
    },
    {
      "level": "A2",
      "sub_levels": [
        {
          "sub_level": "A2.1",
          "topics": [
            "Describing People, Things, and Places",
            "Past Events (Perfekt tense)",
            "Travel and Vacation",
            "Health and Body Parts",
            "Grammar: Dative Case",
            "Grammar: Dative Prepositions",
            "Grammar: Comparative and Superlative Adjectives"
          ]
        },
        {
          "sub_level": "A2.2",
          "topics": [
            "Giving Advice and Making Suggestions",
            "Clothing and Weather",
            "Living and Accommodation",
            "Grammar: Subordinate Clauses (weil, dass)",
            "Grammar: Past Tense (Präteritum) of 'sein' and 'haben'",
            "Grammar: Genitive Case (Possession)",
            "Grammar: Relative Clauses (Basics)"
          ]
        }
      ]
    },
    {
      "level": "B1",
      "sub_levels": [
        {
          "sub_level": "B1.1",
          "topics": [
            "Expressing Opinions, Agreement, and Disagreement",
            "Work and Career",
            "Media and Technology",
            "Grammar: Subjunctive II (Konjunktiv II) for wishes and polite requests",
            "Grammar: Passive Voice (Passiv) in Present Tense",
            "Grammar: Reflexive Verbs"
          ]
        },
        {
          "sub_level": "B1.2",
          "topics": [
            "Environment and Climate",
            "Social and Cultural Life",
            "Future Plans and Ambitions",
            "Grammar: Adjective Declension without articles",
            "Grammar: Subordinate clauses with 'obwohl', 'während'",
            "Grammar: 'n-Deklination'"
          ]
        },
        {
          "sub_level": "B1.3",
          "topics": [
            "Education and Training",
            "History and Politics (simple topics)",
            "Literature and Art (simple discussions)",
            "Grammar: Past Perfect (Plusquamperfekt)",
            "Grammar: Future Tense (Futur I)",
            "Grammar: Verbs with Prepositions"
          ]
        }
      ]
    },
    {
      "level": "B2",
      "sub_levels": [
        {
          "sub_level": "B2.1",
          "topics": [
            "Advanced Discussions on Work and Economy",
            "Science and Research",
            "Grammar: Advanced Subjunctive (Konjunktiv I for indirect speech)",
            "Grammar: Complex Passive Voice constructions",
            "Grammar: Participles as Adjectives (Partizip I & II)"
          ]
        },
        {
          "sub_level": "B2.2",
          "topics": [
            "Detailed Argumentation and Debating",
            "Psychology and Sociology",
            "Grammar: Complex sentence structures with various subordinate clauses",
            "Grammar: Noun-Verb combinations (Nomen-Verb-Verbindungen)",
            "Grammar: Modal verbs in different tenses and moods"
          ]
        }
      ]
    }
  ]
}
```