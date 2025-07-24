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
import com.vpk.sprachninja.presentation.ui.view.OnboardingActivity
import com.vpk.sprachninja.presentation.viewmodel.HomeViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer
        val viewModel: HomeViewModel by viewModels { ViewModelFactory(appContainer) }

        setContent {
            val user by viewModel.user.collectAsState()

            SprachNinjaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use a LaunchedEffect that triggers only once to check the user's existence.
                    // If user is null after the initial check, navigate to onboarding.
                    LaunchedEffect(key1 = user) {
                        if (user == null) {
                            // Check again to avoid race condition on first load
                            if (viewModel.user.value == null) {
                                val intent = Intent(this@HomeActivity, OnboardingActivity::class.java)
                                startActivity(intent)
                                finish() // Finish HomeActivity so user can't navigate back to it
                            }
                        }
                    }

                    // Show a loading indicator while the user state is being determined.
                    // Once the user is confirmed to exist, show the welcome screen.
                    if (user != null) {
                        WelcomeScreen(username = user!!.username)
                    } else {
                        LoadingScreen()
                    }
                }
            }
        }
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