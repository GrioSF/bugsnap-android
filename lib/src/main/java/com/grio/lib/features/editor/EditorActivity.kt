package com.grio.lib.features.editor

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.gson.Gson
import com.grio.lib.R
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.core.extension.observe
import com.grio.lib.core.extension.viewModels
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.views.ScreenAnnotator
import com.grio.lib.features.editor.views.Tool
import com.grio.lib.features.editor.views.ToolOptions

import kotlinx.android.synthetic.main.a_annotation.*
import javax.inject.Inject
import com.google.android.material.bottomsheet.BottomSheetBehavior

import kotlinx.android.synthetic.main.v_bottom_sheet.*

import android.view.View
import android.widget.Toast

const val TOOL_OPTIONS_DRAWER_MARGIN = 16
const val TOOL_OPTIONS_DRAWER_ANIMATION_DURATION = 500L
const val TOOL_OPTIONS_DECELERATE_FACTOR = 3f

class EditorActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private val collapsedToolOptions = ConstraintSet()
    private val expandedToolOptions = ConstraintSet()
    private val toolOptionsTransition = ChangeBounds()

    private lateinit var viewModel: EditorViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_annotation)
        DaggerInjector.getComponent().inject(this)

        viewModel = viewModels(viewModelFactory) {
            observe(toolOptionsShown, ::handleToolOptions)
            observe(isLoading, ::renderLoading)
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
                it.itemId == R.id.draw -> {
                    screenAnnotator.currentTool = Tool.PEN
                    viewModel.toolOptionsShown.value = true
                }
                it.itemId == R.id.insertText -> {
                    screenAnnotator.currentTool = Tool.TEXT
                    viewModel.toolOptionsShown.value = true
                }
                it.itemId == R.id.insertShape -> {
                    screenAnnotator.currentTool = Tool.SHAPE
                    viewModel.toolOptionsShown.value = true
                }
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

        // Initialize bottom sheet behavior.
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        // Confirmation button click listener
        confirmAnnotations.setOnClickListener {
            DataHolder.data = screenAnnotator.getAnnotatedScreenshot()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Add details button/label
        add_details_label.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        // Add Ticket button click listener.
        add_btn.setOnClickListener {
            // Check inputs
            if (summary_input.text.isNullOrBlank()) {
                Toast.makeText(this@EditorActivity, "Summary must not be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addButtonClicked(summary_input.text.toString(), description_input.text.toString(), DataHolder.toFile(this))
        }

        // Cancel button listener.
        cancel_btn.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun renderLoading(isLoading: Boolean?) {
        if (add_btn_progress != null && isLoading != null) {
            add_btn_progress.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            add_btn.text = if (isLoading) "" else "Add Ticket"
            if (isLoading == false && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                // Flow complete, close and exit.
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                Toast.makeText(this, "Ticket successfully created.", Toast.LENGTH_SHORT).show()
                finish()
            }
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

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        screenAnnotator.dispatchKeyEvent(event)
        return super.dispatchKeyEvent(event);
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