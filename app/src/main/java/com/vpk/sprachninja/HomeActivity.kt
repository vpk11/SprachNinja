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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.presentation.ui.view.OnboardingActivity
import com.vpk.sprachninja.presentation.ui.view.PracticeModeDialog
import com.vpk.sprachninja.presentation.ui.view.ProfileActivity
import com.vpk.sprachninja.presentation.ui.view.QuestionAnswerActivity
import com.vpk.sprachninja.presentation.ui.view.SettingsActivity
import com.vpk.sprachninja.presentation.viewmodel.HomeUiState
import com.vpk.sprachninja.presentation.viewmodel.HomeViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

const val QUESTION_TYPE_EXTRA = "com.vpk.sprachninja.QUESTION_TYPE_EXTRA"

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer
        val viewModel: HomeViewModel by viewModels {
            ViewModelFactory(appContainer, this)
        }

        setContent {
            SprachNinjaTheme {
                val uiState by viewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> LoadingScreen()
                        is HomeUiState.Success -> WelcomeScreen(username = state.user.username)
                        is HomeUiState.NoUser -> NavigateToOnboarding()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(username: String) {
    val context = LocalContext.current
    var showPracticeModeDialog by remember { mutableStateOf(false) }

    if (showPracticeModeDialog) {
        PracticeModeDialog(
            onDismissRequest = { showPracticeModeDialog = false },
            onModeSelected = { questionType ->
                showPracticeModeDialog = false
                val intent = Intent(context, QuestionAnswerActivity::class.java).apply {
                    putExtra(QUESTION_TYPE_EXTRA, questionType)
                }
                context.startActivity(intent)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "My Profile"
                        )
                    }
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome back, $username!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                showPracticeModeDialog = true
            }) {
                Text("Start Learning")
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
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}