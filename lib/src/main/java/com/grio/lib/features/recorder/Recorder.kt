package com.grio.lib.features.recorder

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast


import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.graphics.PixelFormat.TRANSLUCENT
import com.grio.lib.R

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Recorder(private val context: Context,
               resultCode: Int, data: Intent) {

    /**
     * The file path the video will be written to.
     */
    private var outputFilePath: File? = null

    /**
     * A button used to stop the recording.
     */
    private var recordingButton: RecordingButton? = null

    /**
     * An instance of [WindowManager].
     */
    private val windowManager: WindowManager

    /**
     * The output directory.
     */
    private val outputDir: File

    /**
     * A [Boolean] tracking the recording state.
     */
    private var isRecording: Boolean = false

    /**
     * A timer to prevent extra long recordings.
     */
    private var countdownTimer: CountDownTimer? = null
    private val recordingProjector: RecordingProjector
    private var callback: Callback? = null
    
    init {
        outputDir = File(context.externalCacheDir, "Bugsnap")
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        recordingProjector = RecordingProjector(context, resultCode, data)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun showOverlayWithButton() {
        recordingButton = RecordingButton(context)
        recordingButton!!.setOnClickListener { finishRecording() }

        val type: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            TRANSLUCENT
        )

        layoutParams.gravity = Gravity.END

        windowManager.addView(recordingButton, layoutParams)
    }

    private fun hideOverlay() {
        if (recordingButton != null) {
            recordingButton!!.hide(object : RecordingButton.VisibilityCallback {
                override fun onViewHidden() {
                    windowManager.removeView(recordingButton)
                    recordingButton = null
                }
            })
        }
    }

    fun initiateRecording() {

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            Toast.makeText(context, context.getString(R.string.error_device_storage), Toast.LENGTH_SHORT).show()
            return
        }

        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val scaledDisplayWidth = displayMetrics.widthPixels * VIDEO_SCALE / 100
        val scaledDisplayHeight = displayMetrics.heightPixels * VIDEO_SCALE / 100

        val fileFormat = SimpleDateFormat(VIDEO_FILE_NAME_PATTERN, Locale.US)
        val outputFilename = fileFormat.format(Date())
        outputFilePath = File(outputDir, outputFilename)

        recordingProjector.recordingEncoder = RecordingEncoder(scaledDisplayWidth, scaledDisplayHeight, outputFilePath!!)
        recordingProjector.start()

        isRecording = true
        countdownTimer =
            object : CountDownTimer(MAX_RECORD_TIME_MS.toLong(), 1000) {
                override fun onTick(millisecondsUntilFinished: Long) {}
                override fun onFinish() { finishRecording() }
            }.start()
    }

    private fun finishRecording() {

        if (!isRecording) {
            throw RuntimeException("The recorder must be started before it can be stopped.")
        }
        countdownTimer!!.cancel()
        isRecording = false
        hideOverlay()
        recordingProjector.stop()
        MediaScannerConnection.scanFile(context, arrayOf(outputFilePath!!.absolutePath), null) { path, _ ->
            Handler(Looper.getMainLooper()).post {
                if (callback != null) {
                    callback!!.onRecordingFinished(File(path))
                }
            }
        }
    }

    interface Callback {
        fun onRecordingFinished(file: File)
    }

    companion object {
        const val TAG = "Recorder"
        private const val VIDEO_FILE_NAME_PATTERN = "'Bugsnap_'yyyy-MM-dd-HH-mm-ss'.mp4'"
        private const val VIDEO_SCALE = 20
        private const val MAX_RECORD_TIME_MS = 30 * 1000
    }
}
