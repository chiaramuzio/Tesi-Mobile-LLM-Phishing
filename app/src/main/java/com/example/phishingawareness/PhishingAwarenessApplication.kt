package com.example.phishingawareness

import android.app.Application
import com.example.phishingawareness.di.AppContainer

class PhishingAwarenessApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(
            context = this
        )
    }
}