package com.grio.lib.features



import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log


@SuppressLint("ValidFragment")
class ReportTypeDialog constructor(private var callback: SelectionCallback) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = AlertDialog.Builder(activity)
        b.setTitle("Select Report Type")
        val types = arrayOf("Screenshot", "Screen recording")
        b.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> dismiss()}


        b.setItems(types) { dialog, which ->
            dialog.dismiss()
            when (which) {
                0 -> callback.onSelection(ReportType.SCREENSHOT)
                1 -> callback.onSelection(ReportType.SCREENRECORD)
            }

        }
        return b.create()
    }

    override fun show(manager: FragmentManager, tag: String) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commit()
        } catch (e: IllegalStateException) {
            Log.e("ReportTypeDialog", "error:", e)
        }
    }

    interface SelectionCallback {
        fun onSelection(type: ReportType)
    }

    enum class ReportType {
        SCREENSHOT,
        SCREENRECORD
    }
}