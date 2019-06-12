package com.grio.bugsnap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.grio.lib.DataHolder
import com.grio.lib.ReportActivity
import com.grio.lib.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shake_button.setOnClickListener {
            val rootView = this.window?.decorView as View
            val bitmap = Utils.takeScreenshot(rootView)

            val intent = Intent(this, ReportActivity::class.java)
            DataHolder.data = bitmap
            this.startActivity(intent)
        }
    }
}
