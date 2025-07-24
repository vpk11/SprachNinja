package com.vpk.sprachninja.presentation.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vpk.sprachninja.QUESTION_TYPE_EXTRA
import com.vpk.sprachninja.SprachNinjaApp
import com.vpk.sprachninja.presentation.viewmodel.QuestionAnswerViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory

class QuestionAnswerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer

        // 1. Read the question type from the intent extra
        val questionType = intent.getStringExtra(QUESTION_TYPE_EXTRA)

        // Basic validation: if the activity is started without the required extra, finish it.
        if (questionType == null) {
            Toast.makeText(this, "Error: Practice mode not specified.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Instantiate the ViewModel using a custom factory instance that includes the questionType
        val viewModel: QuestionAnswerViewModel by viewModels {
            ViewModelFactory(appContainer, this, questionType)
        }

        setContent {
            QuestionAnswerScreen(
                viewModel = viewModel,
                onNavigateUp = { finish() }
            )
        }
    }
}