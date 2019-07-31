package com.grio.lib.features

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.extension.screenshot
import com.grio.lib.features.editor.DataHolder
import com.grio.lib.features.editor.EditorActivity
import com.grio.lib.features.recorder.Recorder
import com.grio.lib.features.recorder.RecordingFragment
import com.grio.lib.features.recorder.ReporterActivity
import java.io.File

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

                             // Start dialog for choosing
                             // reporting flow.
                             val dialog = ReportTypeDialog(object : ReportTypeDialog.SelectionCallback {
                                 override fun onSelection(type: ReportTypeDialog.ReportType) {
                                    when (type) {
                                        ReportTypeDialog.ReportType.SCREENSHOT -> {
                                            // Begin screenshot flow.
                                            activity?.window?.decorView?.post {
                                                val rootView = activity.window?.decorView as View
                                                val bitmap = rootView.screenshot()
                                 val logDump = LogSnapshotManager.getLogTail()


                                                DataHolder.data = bitmap
                                                activity.startActivity(EditorActivity.newIntent(activity, logDump))
                                            }
                                        }
                                        ReportTypeDialog.ReportType.SCREENRECORD -> {
                                            // Begin screen recording flow.
                                            initiateScreenRecording(activity!!, activity)
                                        }
                                    }
                                 }
                             })
                             dialog.show(activity!!.fragmentManager, "dialog")
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

        private fun initiateScreenRecording(context: Context, activity: Activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Toast.makeText(context, com.grio.lib.R.string.record_api_level_too_low, Toast.LENGTH_LONG).show()
                return
            }

            val fragmentManager = activity.fragmentManager
            var fragment: RecordingFragment? = fragmentManager!!.findFragmentByTag(
                RecordingFragment.TAG
            ) as RecordingFragment?

            if (fragment == null) {
                fragment = RecordingFragment()
                fragment.setPermissionCallback(object : RecordingFragment.PermissionCallback {
                    override fun onPermissionGranted(resultCode: Int, data: Intent) {
                        Recorder(context, resultCode, data).apply {
                            setCallback(object : Recorder.Callback {
                                override fun onRecordingFinished(file: File) {
                                    val intent = Intent(context, ReporterActivity::class.java)
                                    intent.putExtra("videoFile", file)
                                    context.startActivity(intent)
                                }
                             })
                            showOverlayWithButton()
                            initiateRecording()
                        }
                    }

                    override fun onPermissionDenied(permissionType: RecordingFragment.PermissionType) {
                        val msg = when (permissionType) {
                            RecordingFragment.PermissionType.OVERLAY -> "You need to allow the overlay to use screen recording."
                            RecordingFragment.PermissionType.RECORDING -> "You need to allow screen recording permissions to use screen recording."
                        }

                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                })
                fragmentManager.beginTransaction().add(fragment, RecordingFragment.TAG).commit()
            }
        }
    }
}