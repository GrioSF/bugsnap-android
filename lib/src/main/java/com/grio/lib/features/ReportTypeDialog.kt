package com.grio.lib.features



import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle


@SuppressLint("ValidFragment")
class ReportTypeDialog constructor(private var callback: SelectionCallback) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = AlertDialog.Builder(activity)
        b.setTitle("Select report type")
        val types = arrayOf("Screenshot", "Screen recording")


        b.setItems(types) { dialog, which ->
            dialog.dismiss()
            when (which) {
                0 -> callback.onSelection(ReportType.SCREENSHOT)
                1 -> callback.onSelection(ReportType.SCREENRECORD)
            }

        }
        return b.create()
    }

    interface SelectionCallback {
        fun onSelection(type: ReportType)
    }

    enum class ReportType {
        SCREENSHOT,
        SCREENRECORD
    }
}