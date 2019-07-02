package com.grio.lib.features.editor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.gson.Gson
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.R
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.views.ScreenAnnotator
import com.grio.lib.features.editor.views.ToolOptions
import com.grio.lib.features.report.ReportSummaryActivity
import kotlinx.android.synthetic.main.a_annotation.*
import javax.inject.Inject


class AnnotationActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private val collapsedToolOptions = ConstraintSet()
    private val expandedToolOptions = ConstraintSet()
    private val toolOptionsTransition = ChangeBounds()

    var toolsShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_annotation)
        DaggerInjector.getComponent().inject(this)
        collapsedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.connect(
            R.id.toolOptions, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START,
            16 * resources.displayMetrics.density.toInt()
        )
        toolOptionsTransition.interpolator = DecelerateInterpolator(3f)
        toolOptionsTransition.duration = 500

        setupToolbar()
        setupScreenAnnotator()

        screenAnnotator.setEventListener(object : ScreenAnnotator.Listener {
            override fun beginDrawing() {
                if (toolsShown) toggleToolOptions()
            }
        })

        toolSelector.setOnMenuItemClickListener {
            when {
                it.itemId == R.id.draw -> {
                    if (!toolsShown) {
                        toggleToolOptions()
                    }
                }
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

        toolOptions.setToolOptionsListener(object : ToolOptions.Listener {

            override fun strokeWidthSet(strokeWidth: Float) {
                screenAnnotator.setPaintStroke(strokeWidth)
            }

            override fun colorSelected(color: String) {
                screenAnnotator.setPaintColor(color)
            }

            override fun toggleDrawer(margin: Int) {
                toggleToolOptions()
            }
        })
    }

    /**
     * Sets up toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(topToolbar)
        supportActionBar?.title = getString(R.string.report_a_bug)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Attaches Annotation Listener to show and hide undo button.
     */
    private fun setupScreenAnnotator() {
        screenAnnotator.setPaintColor("#000000")
        screenAnnotator.setScreenshot(DataHolder.data)
    }

    /**
     * Shows and hides the tool options
     */
    private fun toggleToolOptions() {
        toolOptions.setChildrenToVisible(!toolsShown)
        for (child in toolOptions.children) {
            child.animate().alpha(if (toolsShown) 0.0f else 1.0f)
        }
        TransitionManager.beginDelayedTransition(toolConstraintLayout, toolOptionsTransition)
        if (toolsShown) collapsedToolOptions.applyTo(toolConstraintLayout) else expandedToolOptions.applyTo(toolConstraintLayout)
        toolsShown = !toolsShown
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