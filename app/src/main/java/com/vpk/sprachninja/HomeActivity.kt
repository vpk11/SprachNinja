package com.vpk.sprachninja

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vpk.sprachninja.presentation.ui.view.OnboardingActivity
import com.vpk.sprachninja.presentation.viewmodel.HomeUiState
import com.vpk.sprachninja.presentation.viewmodel.HomeViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer
        val viewModel: HomeViewModel by viewModels { ViewModelFactory(appContainer) }

        setContent {
            SprachNinjaTheme {
                val uiState by viewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use an exhaustive 'when' to handle each state explicitly
                    when (val state = uiState) {
                        is HomeUiState.Loading -> {
                            // State 1: We are waiting for the database to respond.
                            LoadingScreen()
                        }
                        is HomeUiState.Success -> {
                            // State 2: Loading is finished, and we found a user.
                            WelcomeScreen(username = state.user.username)
                        }
                        is HomeUiState.NoUser -> {
                            // State 3: Loading is finished, and there is no user.
                            // This is the only time we should navigate to Onboarding.
                            NavigateToOnboarding()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigateToOnboarding() {
    val context = LocalContext.current
    // This side-effect runs once when this Composable enters the screen
    LaunchedEffect(key1 = Unit) {
        val intent = Intent(context, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}

@Composable
fun WelcomeScreen(username: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome back, $username!",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}