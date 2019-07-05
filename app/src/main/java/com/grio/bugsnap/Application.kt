package com.grio.bugsnap

import android.app.Application
import com.grio.lib.features.BugSnap

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            BugSnap.init(this,
                BuildConfig.BUGSNAP_URL,
                BuildConfig.BUGSNAP_PROJECT_NAME,
                BuildConfig.BUGSNAP_PROJECT_KEY,
                BuildConfig.BUGSNAP_JIRA_USERNAME,
                BuildConfig.BUGSNAP_JIRA_API_KEY)
        }
    }
}