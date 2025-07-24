package com.vpk.sprachninja.presentation.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vpk.sprachninja.SprachNinjaApp
import com.vpk.sprachninja.presentation.viewmodel.QuestionAnswerViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory

class QuestionAnswerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer

        // Instantiate the ViewModel using our factory, providing the necessary context.
        val viewModel: QuestionAnswerViewModel by viewModels {
            ViewModelFactory(appContainer, this)
        }

        setContent {
            QuestionAnswerScreen(
                viewModel = viewModel,
                onNavigateUp = { finish() }
            )
        }
    }
}