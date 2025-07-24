package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.presentation.viewmodel.SettingsViewModel
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SettingsDialog(
            initialApiKey = settings.apiKey,
            initialModelName = settings.modelName,
            onDismissRequest = {
                showDialog = false
            },
            onConfirmation = { apiKey, modelName ->
                coroutineScope.launch {
                    viewModel.saveSettings(apiKey, modelName)
                }
                showDialog = false
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
                LazyColumn {
                    item {
                        val apiKeySubtitle = if (settings.apiKey.isNotBlank()) "••••••••••••••••" else "Not set"
                        SettingsItem(
                            title = "Gemini API Key",
                            subtitle = apiKeySubtitle,
                            onClick = {
                                showDialog = true
                            }
                        )
                    }
                    item {
                        SettingsItem(
                            title = "Terms and Conditions",
                            onClick = { /* TODO: Navigate to Terms screen */ }
                        )
                    }
                    item {
                        SettingsItem(
                            title = "Privacy Policy",
                            onClick = { /* TODO: Navigate to Privacy Policy screen */ }
                        )
                    }
                    item {
                        SettingsItem(
                            title = "Data Protection",
                            onClick = { /* TODO: Navigate to Data Protection screen */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}