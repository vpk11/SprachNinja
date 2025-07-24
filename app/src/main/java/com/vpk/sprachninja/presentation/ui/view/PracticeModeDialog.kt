package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A dialog that allows the user to select the type of practice session they want to start.
 *
 * @param onDismissRequest Lambda to call when the dialog is dismissed.
 * @param onModeSelected Lambda called with the selected question type string (e.g., "FILL_IN_THE_BLANK").
 */
@Composable
fun PracticeModeDialog(
    onDismissRequest: () -> Unit,
    onModeSelected: (questionType: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Choose a Practice Mode") },
        text = {
            Column {
                PracticeModeItem(
                    title = "Grammar & Vocabulary",
                    description = "Fill-in-the-blank questions.",
                    onClick = { onModeSelected("FILL_IN_THE_BLANK") }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                PracticeModeItem(
                    title = "Translation",
                    description = "Translate sentences from English to German.",
                    onClick = { onModeSelected("TRANSLATE_EN_DE") }
                )
                // Add the new option here
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                PracticeModeItem(
                    title = "Learn Words",
                    description = "Multiple choice vocabulary.",
                    onClick = { onModeSelected("MULTIPLE_CHOICE_WORD") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PracticeModeItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}