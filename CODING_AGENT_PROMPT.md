Of course. Here is the complete content for the `LLM_PROMPT_DETAILED.md` file. You can copy the entire block and save it directly.

---

# LLM_PROMPT_DETAILED.md

This document provides a highly granular, iterative sequence of prompts to build the **SprachNinja** application. Each prompt is designed to generate a small, self-contained, and testable piece of code.

**Core Context for all prompts:**
*   **Project Name:** SprachNinja
*   **Package Name:** `com.vpk.sprachninja`
*   **Architecture:** Clean Architecture with MVVM, Jetpack Compose, and a manual DI container.
*   **Target JDK:** OpenJDK 21

---

## Phase 1: Theming and UI Foundation

### Step 1.1: Create Color Palette (`Color.kt`)
**Goal:** Define all the custom colors for the app's themes.
**Context:** A new project.
**Prompt:**
"In the `com.vpk.sprachninja.ui.theme` package, create a new file named `Color.kt`. Copy the following Kotlin code into this file to define the app's color palette."

```kotlin
package com.vpk.sprachninja.ui.theme

import androidx.compose.ui.graphics.Color

val SpaceGrayBackground = Color(0xFF202124)     // Main background
val SpaceGrayCard = Color(0xFF292A2D)          // Card backgrounds, surfaces
val SpaceGrayText = Color(0xFFE0E0E0)          // Primary text
val SpaceGraySecondaryText = Color(0xFFB0B0B0) // Secondary text
val SpaceGrayPrimary = Color(0xFF00BFAE)       // Primary accent (teal)
val SpaceGraySecondary = Color(0xFFFFB300)     // Secondary accent (amber)
val SpaceGrayBorder = Color(0xFF3C3C3F)        // Borders and dividers

val SolarizedBackground = Color(0xFFFDF6E3)    // Main background
val SolarizedCard = Color(0xFFEEE8D5)          // Card backgrounds, surfaces
val SolarizedText = Color(0xFF657B83)          // Primary text (gray-blue)
val SolarizedSecondaryText = Color(0xFF839496) // Secondary text
val SolarizedPrimary = Color(0xFF268BD2)       // Primary accent (soft blue)
val SolarizedSecondary = Color(0xFFB58900)     // Secondary accent (golden yellow)
val SolarizedBorder = Color(0xFFD3D0C8)        // Borders and dividers

val GithubGreen = Color(0xFF3FB950)        // Success states
val GithubPurple = Color(0xFFBC8EFF)       // Additional accents
```

### Step 1.2: Define Typography (`Type.kt`)
**Goal:** Set up the application's typography styles.
**Context:** `Color.kt` exists.
**Prompt:**
"In the `com.vpk.sprachninja.ui.theme` package, create a file named `Type.kt`. Copy the following code that defines the Material 3 `Typography` object into this file."

```kotlin
package com.vpk.sprachninja.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
    // Add other text styles as needed from the full Material 3 spec
)
```

### Step 1.3: Create the App Theme (`Theme.kt`)
**Goal:** Combine colors and typography into a reusable `MaterialTheme`.
**Context:** `Color.kt` and `Type.kt` are complete.
**Prompt:**
"In `com.vpk.sprachninja.ui.theme`, create `Theme.kt`.
1.  Copy the `darkColorScheme` and `lightColorScheme` definitions provided below. Name them `SpaceGrayDarkColorScheme` and `SolarizedLightColorScheme`.
2.  Create a `@Composable` theme function named `SprachNinjaTheme`.
3.  Inside `SprachNinjaTheme`, select the appropriate color scheme based on `isSystemInDarkTheme()`.
4.  Call the `MaterialTheme` composable, passing in the selected `colorScheme`, the `Typography` from `Type.kt`, and the `content` lambda. Use the exact code provided."

```kotlin
package com.vpk.sprachninja.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceGrayDarkColorScheme = darkColorScheme(
    primary = SpaceGrayPrimary,
    onPrimary = Color.White,
    secondary = SpaceGraySecondary,
    onSecondary = Color.Black,
    background = SpaceGrayBackground,
    onBackground = SpaceGrayText,
    surface = SpaceGrayCard,
    onSurface = SpaceGrayText,
    surfaceVariant = SpaceGrayCard,
    onSurfaceVariant = SpaceGraySecondaryText,
    outline = SpaceGrayBorder,
    error = Color(0xFFF85149)
)

private val SolarizedLightColorScheme = lightColorScheme(
    primary = SolarizedPrimary,
    onPrimary = Color.White,
    secondary = SolarizedSecondary,
    onSecondary = Color.Black,
    background = SolarizedBackground,
    onBackground = SolarizedText,
    surface = SolarizedCard,
    onSurface = SolarizedText,
    surfaceVariant = SolarizedCard,
    onSurfaceVariant = SolarizedSecondaryText,
    outline = SolarizedBorder,
    error = Color(0xFFCF222E)
)

@Composable
fun SprachNinjaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> SpaceGrayDarkColorScheme
        else -> SolarizedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Phase 2: Project Foundation

### Step 2.1: Configure Gradle Dependencies
**Goal:** Set up all required libraries in the project's build file.
**Context:** A new project where the theme has been defined.
**Prompt:**
"In the app-level `build.gradle.kts` for the **SprachNinja** project, add the `id("com.google.devtools.ksp")` plugin. Then, add the following dependencies:
*   Jetpack Compose BOM (`androidx.compose:compose-bom`)
*   Lifecycle ViewModel for Compose (`androidx.lifecycle:lifecycle-viewmodel-compose`)
*   RoomDB for local storage (`androidx.room:room-runtime`, `androidx.room:room-ktx`, and `androidx.room:room-compiler` using KSP)
*   Android Security for encrypted preferences (`androidx.security:security-crypto`)
*   Kotlin Coroutines (`org.jetbrains.kotlinx:kotlinx-coroutines-android`)
    Also, ensure the `compileOptions` are set to `JavaVersion.VERSION_1_8` and the Kotlin `jvmTarget` is `'1.8'`."

### Step 2.2: Create Application and DI Container Stubs
**Goal:** Create the essential `Application` and `AppContainer` classes.
**Context:** Gradle dependencies have been added.
**Prompt:**
"In the `com.vpk.sprachninja` package:
1.  Create a class named `SprachNinjaApp` that extends `android.app.Application`.
2.  In the `com.vpk.sprachninja.di` package, create an empty class `AppContainer` that takes a `Context` as its primary constructor parameter.
3.  In `SprachNinjaApp`, create a `lateinit var` property for the `AppContainer` and instantiate it in the `onCreate` method.
4.  Finally, register `SprachNinjaApp` in the `<application>` tag of the `AndroidManifest.xml`."

---

## Phase 3: Onboarding Feature - Data Layer

### Step 3.1: Create the User Entity
**Goal:** Define the data structure for a user in the database.
**Context:** The core application classes exist.
**Prompt:**
"In the `com.vpk.sprachninja.data.local` package, create a Room `@Entity` data class named `User`. It must have the following properties:
*   `id`: An `Int` that is the `@PrimaryKey` and is `autoGenerate = true`.
*   `username`: A `String`.
*   `germanLevel`: A `String`."

### Step 3.2: Create the User DAO
**Goal:** Define the database access methods for the `User` entity.
**Context:** The `User` entity class (`com.vpk.sprachninja.data.local.User`) has been created.
**Prompt:**
"In the `com.vpk.sprachninja.data.local` package, create a Room `@Dao` interface named `UserDao`. It needs to define two methods:
1.  An `upsertUser` function that takes a `User` object and is annotated with `@Upsert`.
2.  A `getUser` function that takes no arguments, returns a `Flow<User?>`, and is annotated with `@Query("SELECT * FROM user LIMIT 1")`."

### Step 3.3: Create the Room Database
**Goal:** Create the main database class that ties the entities and DAOs together.
**Context:** The `User` entity and `UserDao` interface are complete.
**Prompt:**
"In `com.vpk.sprachninja.data.local`, create the `AppDatabase` class.
1.  It must be an abstract class that extends `RoomDatabase`.
2.  Annotate it with `@Database`, listing `User::class` in the `entities` array and setting `version = 1`.
3.  It must contain a single abstract function that returns a `UserDao`.
4.  Include a `companion object` with a `getDatabase` singleton method to build and return a database instance."

---

## Phase 4: Onboarding Feature - Domain & Repository Layer

### Step 4.1: Define the Repository Interface
**Goal:** Create the domain-layer contract for user data operations.
**Context:** The data layer (DAO) is defined.
**Prompt:**
"In the `com.vpk.sprachninja.domain.repository` package, create a public interface named `UserRepository`. It should define two functions matching the `UserDao`:
*   A `suspend` function `upsertUser(user: com.vpk.sprachninja.data.local.User)`.
*   A function `getUser()` that returns a `Flow<com.vpk.sprachninja.data.local.User?>`."

### Step 4.2: Implement the Repository
**Goal:** Create the data-layer implementation of the `UserRepository`.
**Context:** `UserDao` and `UserRepository` interfaces exist.
**Prompt:**
"In the `com.vpk.sprachninja.data.repository` package, create a class `UserRepositoryImpl` that implements the `UserRepository` interface. It should take a `UserDao` in its constructor and delegate its method calls to the corresponding DAO methods."

### Step 4.3: Create the Save User Use Case
**Goal:** Encapsulate the business logic for saving a user.
**Context:** The `UserRepository` interface exists.
**Prompt:**
"In the `com.vpk.sprachninja.domain.usecase` package, create a class named `SaveUserUseCase`. It should take a `UserRepository` in its constructor and contain a single `suspend operator fun invoke(user: com.vpk.sprachninja.data.local.User)` method that calls `userRepository.upsertUser()`."

### Step 4.4: Wire Dependencies in AppContainer
**Goal:** Update the DI container to create and provide the new repository.
**Context:** `AppDatabase` and `UserRepositoryImpl` have been created.
**Prompt:**
"Update the `AppContainer` class in `com.vpk.sprachninja.di`.
1.  Add a private `lazy` property to instantiate the `AppDatabase`.
2.  Add a public `lazy` property for the `UserRepository` interface, initializing it with `UserRepositoryImpl` and passing it the `userDao` from the database instance."

---

## Phase 5: Onboarding Feature - Presentation Layer

### Step 5.1: Create the Onboarding ViewModel
**Goal:** Create the ViewModel to manage the state and logic for the onboarding screen.
**Context:** The `SaveUserUseCase` class exists.
**Prompt:**
"In `com.vpk.sprachninja.presentation.viewmodel`, create `OnboardingViewModel`.
1.  It should inherit from `androidx.lifecycle.ViewModel`.
2.  It should take a `SaveUserUseCase` in its private constructor.
3.  Expose a `MutableStateFlow<String>` for `username`.
4.  Expose a `MutableStateFlow<String>` for `germanLevel`.
5.  Expose a private `MutableStateFlow<Boolean>` for `_onboardingComplete` and a public `StateFlow` to expose it.
6.  Create a public `saveUser()` function that launches a coroutine, creates a `User` object from the state flows, and calls the `saveUserUseCase`. On success, it should set `_onboardingComplete` to `true`."

### Step 5.2: Create the ViewModelFactory
**Goal:** Create the factory responsible for instantiating ViewModels with their dependencies.
**Context:** `AppContainer` and `OnboardingViewModel` classes are defined.
**Prompt:**
"In `com.vpk.sprachninja.presentation.viewmodel`, create a `ViewModelFactory` class.
1.  It must implement `ViewModelProvider.Factory`.
2.  It should take the `AppContainer` in its constructor.
3.  Override the `create` method. Inside, use a `when` block to check for `OnboardingViewModel::class.java`. If it matches, construct and return an `OnboardingViewModel` instance, providing the `SaveUserUseCase` from the `AppContainer`."

### Step 5.3: Create the Onboarding UI Screen
**Goal:** Build the Jetpack Compose UI for the onboarding screen.
**Context:** The `OnboardingViewModel` is fully defined and the app theme exists.
**Prompt:**
"In `com.vpk.sprachninja.presentation.ui.view`, create `OnboardingScreen.kt`.
1.  Define a `@Composable` function `OnboardingScreen` that accepts an `OnboardingViewModel` as a parameter.
2.  **The entire content of the composable must be wrapped in your `SprachNinjaTheme` composable.**
3.  Collect the `username` and `germanLevel` state flows from the ViewModel.
4.  The UI should have a central `Column` on a `Surface` containing:
    *   A `Text` with the title "Welcome to SprachNinja", using `MaterialTheme.typography.headlineSmall`.
    *   An `OutlinedTextField` for the username.
    *   Another `OutlinedTextField` for the German level.
    *   A `Button` with the text "Get Started", enabled only when username is not blank, calling `viewModel.saveUser()`."

---

## Phase 6: Activities & Navigation

### Step 6.1: Create the Onboarding Activity
**Goal:** Create an Activity to host the `OnboardingScreen` composable.
**Context:** `OnboardingScreen`, `OnboardingViewModel`, and `ViewModelFactory` are complete.
**Prompt:**
"In the `com.vpk.sprachninja.presentation.ui.view` package, create `OnboardingActivity.kt`.
1.  It must extend `ComponentActivity`.
2.  In `onCreate`, retrieve the `AppContainer` from the `Application` class.
3.  Instantiate the `OnboardingViewModel` using the `by viewModels` delegate, passing in your custom `ViewModelFactory`.
4.  Set the content to the `OnboardingScreen` composable, passing the created ViewModel instance to it. The `OnboardingScreen` will handle applying the theme internally."

### Step 6.2: Create the Home Activity & Implement Navigation
**Goal:** Create the main launcher Activity and implement the logic to navigate between Home and Onboarding.
**Context:** All components for the onboarding flow are complete.
**Prompt:**
"This is a two-part task.
1.  Create `HomeActivity.kt` in `com.vpk.sprachninja.presentation.ui.view`. Its content should be a `SprachNinjaTheme` block wrapping a `Surface` and a `Text("Welcome to SprachNinja")`. In `AndroidManifest.xml`, make this the main `LAUNCHER` activity.
2.  Add logic to `HomeActivity`'s `onCreate` to check if a user exists (create and use a `GetUserUseCase` and a new `HomeViewModel`). If no user exists, it must launch `OnboardingActivity` and `finish()` itself.
3.  Modify `OnboardingActivity` to collect the `onboardingComplete` state from its ViewModel. When this state becomes `true`, it must launch `HomeActivity` (with `FLAG_ACTIVITY_CLEAR_TOP`) and then call `finish()`."