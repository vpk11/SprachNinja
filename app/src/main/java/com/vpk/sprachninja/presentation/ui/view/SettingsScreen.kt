package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vpk.sprachninja.presentation.viewmodel.SettingsViewModel
import com.vpk.sprachninja.ui.theme.SprachNinjaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    // Collect the settings state from the ViewModel.
    // The UI will automatically recompose when the settings change.
    val settings by viewModel.settings.collectAsState()

    SprachNinjaTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Settings") })
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                LazyColumn {
                    item {
                        // A good practice is to mask the API key for security and clean UI.
                        val apiKeySubtitle = if (settings.apiKey.isNotBlank()) "••••••••••••••••" else "Not set"
                        SettingsItem(
                            title = "Gemini API Key",
                            subtitle = apiKeySubtitle,
                            onClick = {
                                // TODO: Open a dialog to edit the API key and model name.
                            }
                        )
                    }
                    item {
                        SettingsItem(
                            title = "Terms and Conditions",
                            onClick = { /* TODO: Navigate to Terms and Conditions screen */ }
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

/**
 * A reusable composable for displaying a single row in a settings list.
 *
 * @param title The main text for the settings item.
 * @param subtitle Optional smaller text displayed below the title.
 * @param onClick The lambda to be executed when the item is clicked. If null, item is not clickable.
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The clickable modifier is only enabled if onClick is not null.
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