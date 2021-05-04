package com.shkcodes.aurora.ui

import android.app.Application
import com.shkcodes.aurora.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AuroraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        plantTimber()
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
