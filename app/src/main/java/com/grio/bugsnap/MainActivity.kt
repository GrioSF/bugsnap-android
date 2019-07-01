package com.grio.bugsnap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.grio.lib.core.extension.screenshot
import com.grio.lib.features.BugSnap
import com.grio.lib.features.editor.AnnotationActivity
import com.grio.lib.features.editor.DataHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        debugButton.setOnClickListener {
            val rootView = window?.decorView as View
            val bitmap = rootView.screenshot()

            val intent = Intent(this, AnnotationActivity::class.java)
            DataHolder.data = bitmap
            startActivity(intent)
        }
    }
}
