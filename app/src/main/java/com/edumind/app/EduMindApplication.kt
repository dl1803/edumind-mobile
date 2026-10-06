package com.edumind.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EduMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
