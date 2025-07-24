package com.vpk.sprachninja.presentation.ui.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.R
import com.vpk.sprachninja.presentation.viewmodel.SettingsViewModel
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val user by viewModel.user.collectAsState()

    var showApiDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showApiDialog) {
        SettingsDialog(
            initialApiKey = settings.apiKey,
            initialModelName = settings.modelName,
            onDismissRequest = { showApiDialog = false },
            onConfirmation = { apiKey, modelName ->
                viewModel.saveSettings(apiKey, modelName)
                showApiDialog = false
            }
        )
    }

    if (showLevelDialog) {
        LevelSelectorDialog(
            onDismissRequest = { showLevelDialog = false },
            onLevelSelected = { newLevel ->
                viewModel.updateUserLevel(newLevel)
                showLevelDialog = false
            }
        )
    }

    SprachNinjaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SettingsHeader(text = "Account")
                    }
                    item {
                        SettingsCardItem(
                            title = user?.username ?: "Account",
                            subtitle = "Level: ${user?.germanLevel ?: "..."}",
                            icon = Icons.Default.Person,
                            onClick = { showLevelDialog = true }
                        )
                    }

                    item {
                        SettingsHeader(text = "API Configuration")
                    }
                    item {
                        val apiKeySubtitle = if (settings.apiKey.isNotBlank()) "••••••••••••••••" else "Not set"
                        SettingsCardItem(
                            title = "Gemini API Key",
                            subtitle = apiKeySubtitle,
                            icon = Icons.Filled.Key,
                            onClick = { showApiDialog = true }
                        )
                    }

                    item {
                        SettingsHeader(text = "Legal")
                    }
                    item {
                        SettingsCardItem(
                            title = "Terms & Conditions",
                            subtitle = "Read our terms of service",
                            icon = Icons.Filled.Description,
                            onClick = {
                                navigateToLegal(
                                    context = context,
                                    titleResId = R.string.terms_and_conditions_title,
                                    contentResId = R.string.terms_and_conditions_content
                                )
                            }
                        )
                    }
                    item {
                        SettingsCardItem(
                            title = "Privacy Policy",
                            subtitle = "How we handle your data",
                            icon = Icons.Filled.Shield, // Corrected
                            onClick = {
                                navigateToLegal(
                                    context = context,
                                    titleResId = R.string.privacy_policy_title,
                                    contentResId = R.string.privacy_policy_content
                                )
                            }
                        )
                    }
                    item {
                        SettingsCardItem(
                            title = "Data Protection",
                            subtitle = "Information about on-device storage",
                            icon = Icons.Filled.Policy,
                            onClick = {
                                navigateToLegal(
                                    context = context,
                                    titleResId = R.string.data_protection_title,
                                    contentResId = R.string.data_protection_content
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun navigateToLegal(context: Context, titleResId: Int, contentResId: Int) {
    val intent = Intent(context, LegalActivity::class.java).apply {
        putExtra(LegalActivity.EXTRA_TITLE_RES_ID, titleResId)
        putExtra(LegalActivity.EXTRA_CONTENT_RES_ID, contentResId)
    }
    context.startActivity(intent)
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun SettingsCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}