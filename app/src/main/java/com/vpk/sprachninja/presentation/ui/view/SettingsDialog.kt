package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

/**
 * A dialog composable for editing API settings.
 *
 * @param onDismissRequest Lambda to be invoked when the user requests to dismiss the dialog,
 *   either by tapping outside the dialog or pressing the cancel button.
 * @param onConfirmation Lambda to be invoked when the user presses the "Save" button.
 *   It provides the updated API key and model name.
 * @param initialApiKey The current API key to pre-populate the text field.
 * @param initialModelName The current model name to pre-populate the text field.
 */
@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (apiKey: String, modelName: String) -> Unit,
    initialApiKey: String,
    initialModelName: String
) {
    // Use 'remember' to create internal state for the text fields within the dialog.
    // This state is initialized with the current settings from the ViewModel.
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var modelName by remember { mutableStateOf(initialModelName) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "API Settings")
        },
        text = {
            // Arrange the text fields vertically.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Gemini API Key") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Name") },
                    placeholder = { Text("e.g., gemini-1.5-flash") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Pass the current state of the text fields back to the caller.
                    onConfirmation(apiKey, modelName)
                    // Dismiss the dialog after confirming.
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}