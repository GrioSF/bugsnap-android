package com.grio.bugsnap

import android.app.Application
import com.grio.lib.features.BugSnap

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        BugSnap.init(this,
            BuildConfig.BUGSNAP_URL,
            BuildConfig.BUGSNAP_PROJECT_NAME)


    }
}