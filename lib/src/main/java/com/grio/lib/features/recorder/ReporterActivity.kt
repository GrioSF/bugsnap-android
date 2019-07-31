package com.grio.lib.features.recorder

import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.grio.lib.R
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.extension.observe
import com.grio.lib.core.extension.viewModels
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.*
import com.grio.lib.features.editor.views.ScreenAnnotator
import com.grio.lib.features.editor.views.Tool
import com.grio.lib.features.editor.views.ToolOptions
import kotlinx.android.synthetic.main.a_annotation.*
import kotlinx.android.synthetic.main.a_reporter.*

import java.io.File
import javax.inject.Inject

class ReporterActivity :  BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private lateinit var videoFile: File
    private lateinit var viewModel: ReporterViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_reporter)
        DaggerInjector.getComponent().inject(this)

        viewModel = viewModels(viewModelFactory) {
            observe(isLoading, ::renderLoading)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.report_a_bug)

        videoFile = intent.getSerializableExtra("videoFile") as File

    }


    private fun renderLoading(isLoading: Boolean?) {

        if (isLoading == null) {
            return
        }
        if (isLoading) {
            Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loading stopped", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_reporter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.send) {
            // Send report
            val summary = summary_input.text.toString()
            val description = description_input.text.toString()
            if (summary.isNullOrBlank() || description.isNullOrBlank()) {
                Toast.makeText(this, "Inputs must not be blank.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sendReportClicked(summary = summary, description = description, file = videoFile)
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }


}