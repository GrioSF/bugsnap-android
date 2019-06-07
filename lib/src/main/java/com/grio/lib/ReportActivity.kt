package com.grio.lib

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.a_report.*

class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_report)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Report a bug"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val bitmap = DataHolder.data
        screeny.setImageBitmap(bitmap)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

/**
 * Required to pass large bitmaps between components (in this case, a bitmap).
 */
enum class DataHolder {
    INSTANCE;

    private var bitmap: Bitmap? = null

    companion object {

        fun hasData(): Boolean {
            return INSTANCE.bitmap != null
        }

        var data: Bitmap?
            get() {
                val obj = INSTANCE.bitmap
                INSTANCE.bitmap = null
                return obj
            }
            set(bitmap) {
                INSTANCE.bitmap = bitmap
            }
    }
}