package com.grio.lib

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View


class Bugsnap {

    companion object {

        /**
         * Initializes the library.
         * @param context The application's context.
         */
        @JvmStatic
        fun init(context: Context) {


            // Retrieve a SensorManager.
            val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager?
            var sd : ShakeDetector? = null

            // Register lifecycle.
            (context as Application).registerActivityLifecycleCallbacks(object: Application.ActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity?) {}
                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

                    // Setup listener.
                     sd = ShakeDetector(object : ShakeDetector.Listener {
                        override fun hearShake() {
                            Log.d(TAG, "shaking!")
                            val rootView = activity?.window?.decorView as View
                            val bitmap = Utils.takeScreenshot(rootView)

                            val intent = Intent(activity, ReportActivity::class.java)
                            DataHolder.data = bitmap
                            activity.startActivity(intent)

                        }
                    })

                    // Begin listening.
                    sd?.start(sensorManager!!)
                }

                override fun onActivityPaused(activity: Activity?) {}
                override fun onActivityStopped(activity: Activity?) {}
                override fun onActivityResumed(activity: Activity?) {}
                override fun onActivityDestroyed(activity: Activity?) {
                    sd?.stop()
                }
            })

        }

        private const val TAG = "Bugsnap"
    }
}