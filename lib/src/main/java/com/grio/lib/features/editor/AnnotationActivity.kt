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
import com.grio.lib.core.extension.observe
import com.grio.lib.core.extension.viewModels
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.views.ScreenAnnotator
import com.grio.lib.features.editor.views.ToolOptions
import com.grio.lib.features.report.ReportSummaryActivity
import kotlinx.android.synthetic.main.a_annotation.*
import javax.inject.Inject

const val TOOL_OPTIONS_DRAWER_MARGIN = 16
const val TOOL_OPTIONS_DRAWER_ANIMATION_DURATION = 500L
const val TOOL_OPTIONS_DECELERATE_FACTOR = 3f

class AnnotationActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private val collapsedToolOptions = ConstraintSet()
    private val expandedToolOptions = ConstraintSet()
    private val toolOptionsTransition = ChangeBounds()

    private lateinit var viewModel: AnnotationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_annotation)
        DaggerInjector.getComponent().inject(this)

        viewModel = viewModels(viewModelFactory) {
            observe(toolOptionsShown, ::handleToolOptions)
        }
        viewModel.toolOptionsShown.value = false

        setupToolbar()
        setupToolOptionsDrawerAnimations()
        DataHolder.data?.let {
            screenAnnotator.originalScreenshot = it
        }

        // Screen annotator event listener
        screenAnnotator.listener = object : ScreenAnnotator.Listener {
            override fun beginDrawing() {
                viewModel.toolOptionsShown.value = false
            }
        }

        // BottomAppBar menu item click listener
        toolSelector.setOnMenuItemClickListener {
            when {
                it.itemId == R.id.draw -> viewModel.toolOptionsShown.value = true
                //it.itemId == R.id.insertText ->
                //it.itemId == R.id.insertShape ->
                //it.itemId = R.id.delete ->
                it.itemId == R.id.undo -> screenAnnotator.undo()

            }
            return@setOnMenuItemClickListener true
        }

        // Tool options drawer state listener
        toolOptions.listener = object : ToolOptions.Listener {

            override fun strokeWidthSet(strokeWidth: Float) {
                screenAnnotator.strokeWidth = strokeWidth
            }

            override fun colorSelected(color: String) {
                screenAnnotator.paintColor = color
            }

            override fun toggleDrawer(margin: Int) {
                viewModel.toggleToolOptionsDrawer()
            }
        }

        // Confirmation button click listener
        confirmAnnotations.setOnClickListener {
            // Launches the summary Activity.
            DataHolder.data = screenAnnotator.getAnnotatedScreenshot()
            startActivity(Intent(this, ReportSummaryActivity::class.java))
        }
    }

    /**
     * Shows and hides the tool options
     */
    private fun handleToolOptions(isShown: Boolean?) {
        isShown?.let {
            toolOptions.setChildrenToVisible(it)
            for (child in toolOptions.children) {
                child.animate().alpha(if (isShown) 1.0f else 0.0f)
            }
            TransitionManager.beginDelayedTransition(toolConstraintLayout, toolOptionsTransition)
            if (isShown) expandedToolOptions.applyTo(toolConstraintLayout) else collapsedToolOptions.applyTo(
                toolConstraintLayout
            )
        }
    }

    /**
     * Setup constraint sets for tool options drawer animations
     */
    private fun setupToolOptionsDrawerAnimations() {
        collapsedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.clone(toolConstraintLayout)
        expandedToolOptions.connect(
            R.id.toolOptions, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START,
            TOOL_OPTIONS_DRAWER_MARGIN * resources.displayMetrics.density.toInt()
        )
        toolOptionsTransition.interpolator = DecelerateInterpolator(TOOL_OPTIONS_DECELERATE_FACTOR)
        toolOptionsTransition.duration = TOOL_OPTIONS_DRAWER_ANIMATION_DURATION
    }

    /**
     * Sets up toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(topToolbar)
        supportActionBar?.title = getString(R.string.report_a_bug)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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