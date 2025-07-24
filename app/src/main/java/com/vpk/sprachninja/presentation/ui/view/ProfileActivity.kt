package com.vpk.sprachninja.presentation.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vpk.sprachninja.SprachNinjaApp
import com.vpk.sprachninja.presentation.viewmodel.ProfileViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory

class ProfileActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(
            appContainer = (application as SprachNinjaApp).appContainer,
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateUp = { finish() }
            )
        }
    }
}