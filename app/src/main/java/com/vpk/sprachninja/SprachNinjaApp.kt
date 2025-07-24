package com.vpk.sprachninja

import android.app.Application
import com.vpk.sprachninja.di.AppContainer

class SprachNinjaApp : Application() {

    // AppContainer instance that will be used by the rest of the app
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize the container with the application context
        appContainer = AppContainer(this)
    }
}