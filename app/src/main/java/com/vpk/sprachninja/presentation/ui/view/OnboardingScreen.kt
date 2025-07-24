package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.presentation.viewmodel.OnboardingViewModel
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

/**
 * The UI screen for user onboarding. It collects user's name and German level.
 *
 * @param viewModel The ViewModel that holds the state and logic for this screen.
 */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    // Collect the state from the ViewModel. Recomposition will occur when these values change.
    val username by viewModel.username.collectAsState()
    val germanLevel by viewModel.germanLevel.collectAsState()

    // The SprachNinjaTheme wrapper applies our custom colors and typography.
    SprachNinjaTheme {
        // A Surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to SprachNinja",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.username.value = it },
                    label = { Text("Your Name") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = germanLevel,
                    onValueChange = { viewModel.germanLevel.value = it },
                    label = { Text("Your German Level (e.g., A1.1)") },
                    placeholder = { Text(text = "A1.1") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveUser() },
                    // The button is only enabled if the username field is not empty.
                    enabled = username.isNotBlank()
                ) {
                    Text("Get Started")
                }
            }
        }
    }
}