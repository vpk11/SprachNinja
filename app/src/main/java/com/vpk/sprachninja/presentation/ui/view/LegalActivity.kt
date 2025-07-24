package com.vpk.sprachninja.presentation.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vpk.sprachninja.R

class LegalActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TITLE_RES_ID = "EXTRA_TITLE_RES_ID"
        const val EXTRA_CONTENT_RES_ID = "EXTRA_CONTENT_RES_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the resource IDs from the intent extras.
        // We provide a default value to avoid crashes, although a valid ID should always be passed.
        val titleResId = intent.getIntExtra(EXTRA_TITLE_RES_ID, R.string.app_name)
        val contentResId = intent.getIntExtra(EXTRA_CONTENT_RES_ID, R.string.app_name)

        setContent {
            LegalInfoScreen(
                titleResId = titleResId,
                contentResId = contentResId,
                onNavigateUp = {
                    // When the user presses the back arrow in the TopAppBar, finish this activity.
                    finish()
                }
            )
        }
    }
}