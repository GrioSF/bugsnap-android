package com.grio.lib.features.recorder

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import android.content.Context.MEDIA_PROJECTION_SERVICE

/**
 * Required for permissions flow necessary to begin screen recording.
 */
@TargetApi(Build.VERSION_CODES.M)
class RecordingFragment : Fragment() {

    private var permissionCallback: PermissionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkOverlayPermissions()
    }

    fun setPermissionCallback(permissionCallback: PermissionCallback) {
        this.permissionCallback = permissionCallback
    }

    private fun checkOverlayPermissions() {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
            startActivityForResult(intent, SCREEN_OVERLAY_REQUEST_CODE)
        } else {
            onOverlayPermissionsGranted()
        }
    }

    private fun onOverlayPermissionsGranted() {
        val manager = context.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = manager.createScreenCaptureIntent()
        startActivityForResult(intent, SCREEN_RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == SCREEN_OVERLAY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onOverlayPermissionsGranted()
            } else {
                permissionCallback!!.onPermissionDenied(PermissionType.OVERLAY)
            }
        } else if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                permissionCallback!!.onPermissionGranted(resultCode, data)
            } else {
                permissionCallback!!.onPermissionDenied(PermissionType.RECORDING)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    enum class PermissionType {
        OVERLAY, RECORDING
    }

    interface PermissionCallback {
        fun onPermissionGranted(resultCode: Int, data: Intent)
        fun onPermissionDenied(permissionType: PermissionType)
    }

    companion object {
        const val TAG = "com.grio.lib.features.recorder.RecordingFragment"
        private const val SCREEN_OVERLAY_REQUEST_CODE = 1234
        private const val SCREEN_RECORD_REQUEST_CODE = 12345
    }
}
