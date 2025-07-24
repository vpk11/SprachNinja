package com.vpk.sprachninja.presentation.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A dialog for selecting a German proficiency level from a predefined list.
 *
 * @param onDismissRequest Lambda to dismiss the dialog.
 * @param onLevelSelected Lambda that provides the selected level string.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectorDialog(
    onDismissRequest: () -> Unit,
    onLevelSelected: (String) -> Unit
) {
    val germanLevels = listOf(
        "A1.1", "A1.2",
        "A2.1", "A2.2",
        "B1.1", "B1.2", "B1.3",
        "B2.1", "B2.2"
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Your Level") },
        text = {
            LazyColumn {
                items(germanLevels) { level ->
                    Text(
                        text = level,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLevelSelected(level)
                                onDismissRequest()
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}