package com.grio.bugsnap

import android.app.Application
import com.grio.lib.Bugsnap

class Application : Application() {



    override fun onCreate() {
        super.onCreate()
        Bugsnap.init(this)
    }
}