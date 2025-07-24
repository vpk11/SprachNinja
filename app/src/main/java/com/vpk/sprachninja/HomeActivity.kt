package com.vpk.sprachninja

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.presentation.ui.view.OnboardingActivity
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
                val dailyTip by viewModel.dailyTip.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> LoadingScreen()
                        is HomeUiState.Success -> WelcomeScreen(user = state.user, dailyTip = dailyTip)
                        is HomeUiState.NoUser -> NavigateToOnboarding()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(user: User, dailyTip: String?) {
    val context = LocalContext.current

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
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Willkommen, ${user.username}!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            UserSummaryCard(user = user)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose Your Practice",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    // Align cards to the top to handle text wrapping gracefully
                    verticalAlignment = Alignment.Top
                ) {
                    val cardModifier = Modifier.weight(1f)

                    PracticeModeCard(
                        modifier = cardModifier,
                        text = "Learn Words",
                        icon = Icons.Default.Book,
                        onClick = {
                            val intent = Intent(context, QuestionAnswerActivity::class.java).apply {
                                putExtra(QUESTION_TYPE_EXTRA, "MULTIPLE_CHOICE_WORD")
                            }
                            context.startActivity(intent)
                        }
                    )
                    PracticeModeCard(
                        modifier = cardModifier,
                        text = "Grammar",
                        icon = Icons.Default.Edit,
                        onClick = {
                            val intent = Intent(context, QuestionAnswerActivity::class.java).apply {
                                putExtra(QUESTION_TYPE_EXTRA, "FILL_IN_THE_BLANK")
                            }
                            context.startActivity(intent)
                        }
                    )
                    PracticeModeCard(
                        modifier = cardModifier,
                        text = "Translate",
                        icon = Icons.Default.Translate,
                        onClick = {
                            val intent = Intent(context, QuestionAnswerActivity::class.java).apply {
                                putExtra(QUESTION_TYPE_EXTRA, "TRANSLATE_EN_DE")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            TipOfTheDayCard(tip = dailyTip)
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

@Composable
private fun UserSummaryCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Profile",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current Level: ${user.germanLevel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PracticeModeCard(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center, // Ensure text is centered
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TipOfTheDayCard(tip: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡 Tip of the Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tip == null || tip.contains("Loading")) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}