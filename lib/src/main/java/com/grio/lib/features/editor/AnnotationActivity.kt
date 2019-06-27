package com.grio.lib.features.editor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.gson.Gson
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.R
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.views.ToolOptions
import com.grio.lib.features.report.ReportSummaryActivity
import kotlinx.android.synthetic.main.a_annotation.*
import javax.inject.Inject


class AnnotationActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    var toolsShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_annotation)
        DaggerInjector.getComponent().inject(this)
        setupToolbar()
        setupScreenAnnotator()

        toolSelector.setOnMenuItemClickListener {
            when {
                //it.itemId == R.id.draw ->
                //it.itemId == R.id.insertText ->
                //it.itemId == R.id.insertShape ->
                //it.itemId = R.id.delete ->
                it.itemId == R.id.undo -> screenAnnotator.undo()

            }
            return@setOnMenuItemClickListener true
        }

        confirmAnnotations.setOnClickListener {
            // TODO: Update to transfer proper data to secondary Activity.
            // Launches the summary Activity.
            DataHolder.data = screenAnnotator.getAnnotatedScreenshot()
            startActivity(Intent(this, ReportSummaryActivity::class.java))
        }

        toolOptions.setToolOptionsListener(object : ToolOptions.Listener {
            override fun clicked(margin: Int) {
                val constraintSet1 = ConstraintSet()
                constraintSet1.clone(toolConstraintLayout)
                val constraintSet2 = ConstraintSet()
                constraintSet2.clone(toolConstraintLayout)
                if (toolsShown) {
                    constraintSet2.connect(R.id.toolOptions, ConstraintSet.START, R.id.toolCollapsedGuideline, ConstraintSet.START, 0)
                } else {
                    constraintSet2.connect(R.id.toolOptions, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin)
                }

                val transition = ChangeBounds()
                transition.interpolator = DecelerateInterpolator(3f)
                transition.duration = 500
                TransitionManager.beginDelayedTransition(toolConstraintLayout, transition)
                constraintSet2.applyTo(toolConstraintLayout)
                toolsShown = !toolsShown
            }
        })
    }

    /**
     * Sets up toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(topToolbar)
        supportActionBar?.title = "Report a bug"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Attaches Annotation Listener to show and hide undo button.
     */
    private fun setupScreenAnnotator() {
        // TODO: Make this dynamic once new color picker is implemented
        screenAnnotator.setPaintColor("#000000")

        // Attach screenshot to annotator
        val bitmap = DataHolder.data
        screenAnnotator.setScreenshot(bitmap)
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