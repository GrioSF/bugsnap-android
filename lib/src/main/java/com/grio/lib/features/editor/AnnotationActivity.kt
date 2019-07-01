package com.grio.lib.features.editor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
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

    val collapsedToolOptions = ConstraintSet()
    val expandedToolOptions = ConstraintSet()
    val toolOptionsTransition = ChangeBounds()

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
            // Launches the summary Activity.
            DataHolder.data = screenAnnotator.getAnnotatedScreenshot()
            startActivity(Intent(this, ReportSummaryActivity::class.java))
        }

        collapsedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.connect(R.id.toolOptions, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16 * resources.displayMetrics.density.toInt())

        toolOptionsTransition.interpolator = DecelerateInterpolator(3f)
        toolOptionsTransition.duration = 500

        toolOptions.setToolOptionsListener(object : ToolOptions.Listener {

            override fun strokeWidthSet(strokeWidth: Float) {
                screenAnnotator.setPaintStroke(strokeWidth)
            }

            override fun colorSelected(color: String) {
                screenAnnotator.setPaintColor(color)
            }

            override fun clicked(margin: Int) {
                if (toolsShown) {
                    toolOptions.setViewsToVisible(false)
                    for (child in toolOptions.children) {
                        child.animate().alpha(0.0f)
                    }
                    TransitionManager.beginDelayedTransition(toolConstraintLayout, toolOptionsTransition)
                    collapsedToolOptions.applyTo(toolConstraintLayout)
                } else {
                    toolOptions.setViewsToVisible(true)
                    for (child in toolOptions.children) {
                        child.animate().alpha(1.0f)
                    }
                    TransitionManager.beginDelayedTransition(toolConstraintLayout, toolOptionsTransition)
                    expandedToolOptions.applyTo(toolConstraintLayout)
                }
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