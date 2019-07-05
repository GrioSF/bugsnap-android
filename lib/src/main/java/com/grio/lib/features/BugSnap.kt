package com.grio.lib.features

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.extension.screenshot
import com.grio.lib.features.editor.DataHolder
import com.grio.lib.features.editor.EditorActivity


/**
 * BugSnap allows develops to capture screenshots on device during QA sessions and report
 * them directly to JIRA.
 *
 * Initialization is done by calling [BugSnap.init] in your [Application] subclass.
 */
class BugSnap {

    companion object {

        lateinit var jiraUrl: String
        lateinit var jiraProjectName: String
        lateinit var jiraProjectKey: String
        lateinit var jiraUsername: String
        lateinit var jiraApiKey: String

        /**
         * Initializes the library.
         * @param context The application's context.
         */
        @JvmStatic
        fun init(context: Context, jiraUrl: String, jiraProjectName: String,
                 jiraProjectKey: String, jiraUsername: String, jiraApiKey: String) {

            // Set global fields.
            Companion.jiraUrl = jiraUrl
            Companion.jiraProjectName = jiraProjectName
            Companion.jiraProjectKey = jiraProjectKey
            Companion.jiraUsername = jiraUsername
            Companion.jiraApiKey = jiraApiKey

            // Build object graph.
            DaggerInjector.buildComponent(context)

            // Retrieve a SensorManager.
            val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager?
            var sd : ShakeDetector? = null

            // Register lifecycle.
            (context as Application).registerActivityLifecycleCallbacks(object: Application.ActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity?) {}
                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

                    // Setup listener.
                     sd = ShakeDetector(object :
                         ShakeDetector.Listener {
                         override fun hearShake() {
                             Log.d(TAG, "shaking!")
                             val rootView = activity?.window?.decorView as View
                             val bitmap = rootView.screenshot()

                             val intent = Intent(activity, EditorActivity::class.java)
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


        /**
         * A constant TAG for logging.
         */
        private const val TAG = "BugSnap"
    }
}