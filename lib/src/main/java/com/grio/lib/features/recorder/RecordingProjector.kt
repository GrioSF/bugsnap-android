package com.grio.lib.features.recorder

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build


import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RecordingProjector(var context: Context,
                         private var resultCode: Int = 0,
                         private var resultData: Intent? = null) {

    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private val mediaProjectionManager: MediaProjectionManager
    private var screenDensity: Int
    var recordingEncoder: RecordingEncoder? = null


    init {
        mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenDensity = context.resources.displayMetrics.densityDpi
    }

    /**
     * Start projection and related components.
     */
    internal fun start() {

        recordingEncoder?.start() ?: throw RuntimeException("Encoder must be set.")

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData!!)
        if (mediaProjection != null) {
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                recordingEncoder!!.width,
                recordingEncoder!!.height,
                screenDensity,
                VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                recordingEncoder!!.surface, null, null
            )
        }
    }

    /**
     * Stop projector and related components, then release virtual display.
     */
    internal fun stop() {
        recordingEncoder?.apply {
            stop()
            recordingEncoder = null
        }
        mediaProjection?.apply {
            stop()
            mediaProjection = null
        }
        virtualDisplay?.apply {
            release()
            virtualDisplay = null
        }
    }

    companion object {
        private const val VIRTUAL_DISPLAY_NAME = "bugsnap"
    }
}
