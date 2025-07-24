package com.vpk.sprachninja.presentation.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vpk.sprachninja.SprachNinjaApp
import com.vpk.sprachninja.presentation.viewmodel.SettingsViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory

/**
 * An activity dedicated to displaying and managing application settings.
 * It hosts the SettingsScreen composable and its corresponding ViewModel.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer

        val viewModel: SettingsViewModel by viewModels {
            ViewModelFactory(appContainer)
        }

        setContent {
            SettingsScreen(viewModel = viewModel)
        }
    }
}