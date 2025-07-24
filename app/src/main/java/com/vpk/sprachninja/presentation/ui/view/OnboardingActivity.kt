package com.vpk.sprachninja.presentation.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vpk.sprachninja.HomeActivity
import com.vpk.sprachninja.SprachNinjaApp
import com.vpk.sprachninja.presentation.viewmodel.OnboardingViewModel
import com.vpk.sprachninja.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as SprachNinjaApp).appContainer
        val viewModel: OnboardingViewModel by viewModels {
            ViewModelFactory(appContainer)
        }

        // Add this block to handle navigation on completion
        lifecycleScope.launch {
            // repeatOnLifecycle ensures the collector is active only when the
            // activity is in the STARTED state.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onboardingComplete.collect { isComplete ->
                    if (isComplete) {
                        val intent = Intent(this@OnboardingActivity, HomeActivity::class.java).apply {
                            // These flags clear the activity stack and start HomeActivity as a new task.
                            // This prevents the user from navigating back to the onboarding screen.
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }

        setContent {
            OnboardingScreen(viewModel = viewModel)
        }
    }
}