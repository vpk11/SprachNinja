package com.vpk.sprachninja

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.presentation.ui.view.OnboardingActivity
import com.vpk.sprachninja.presentation.ui.view.SettingsActivity
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
                    when (val state = uiState) {
                        is HomeUiState.Loading -> {
                            LoadingScreen()
                        }
                        is HomeUiState.Success -> {
                            WelcomeScreen(username = state.user.username)
                        }
                        is HomeUiState.NoUser -> {
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
    LaunchedEffect(key1 = Unit) {
        val intent = Intent(context, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}

@Composable
fun WelcomeScreen(username: String) {
    // Get the current context, which is needed to launch an Intent.
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome back, $username!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { /* TODO: Navigate to QuestionAnswerActivity */ }) {
                Text("Start Learning")
            }
        }

        IconButton(
            // The onClick lambda now launches the SettingsActivity.
            onClick = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings"
            )
        }
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