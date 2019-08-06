package com.grio.lib.features.editor


import android.content.Context
import android.content.Intent
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
import com.grio.lib.features.editor.views.*
import com.grio.lib.features.reporter.ReporterActivity
import kotlinx.android.synthetic.main.a_editor.*
import javax.inject.Inject

const val TOOL_OPTIONS_DRAWER_MARGIN = 16

class EditorActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    private val collapsedToolOptions = ConstraintSet()
    private val expandedToolOptions = ConstraintSet()

    private lateinit var viewModel: EditorViewModel

    companion object {
        private val INTENT_LOG = "BUGSNAP_INTENT_LOG"

        fun newIntent(context: Context, logDump: String = "") =
            Intent(context, EditorActivity::class.java).apply {
                putExtra(INTENT_LOG, logDump)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_editor)
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
                it.itemId == R.id.draw -> {
                    screenAnnotator.currentTool = Tool.PEN
                    screenAnnotator.resetTextAnnotation()
                    viewModel.toolOptionsShown.value = true
                    toolOptions.setDrawerType(Tool.PEN)
                }
                it.itemId == R.id.insertText -> {
                    screenAnnotator.currentTool = Tool.TEXT
                    screenAnnotator.resetTextAnnotation()
                    viewModel.toolOptionsShown.value = true
                    toolOptions.setDrawerType(Tool.TEXT)
                }
                it.itemId == R.id.insertShape -> {
                    screenAnnotator.currentTool = Tool.SHAPE
                    screenAnnotator.resetTextAnnotation()
                    viewModel.toolOptionsShown.value = true
                    toolOptions.setDrawerType(screenAnnotator.selectedShapeType)
                }
                it.itemId == R.id.delete -> {
                    if (screenAnnotator.attemptToSelectAnnotation)
                        screenAnnotator.removeSelectedAnnotation()
                }
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

            override fun shapeSelected(shape: Shape) {
                screenAnnotator.selectedShapeType = shape
                toolOptions.setDrawerType(shape)
            }
        }

        // Set initial tool size to 50%
        toolOptions.setToolSize(50)

        // Confirmation button click listener
        confirmAnnotations.setOnClickListener {
            screenAnnotator.resetTextAnnotation()
            DataHolder.data = screenAnnotator.getAnnotatedScreenshot()
            // Launch Reporter
            val intent = Intent(this, ReporterActivity::class.java)
            intent.putExtra("reportType", "screenshot")
            startActivity(intent)
        }
    }

    /**
     * Shows and hides the tool options
     */
    private fun handleToolOptions(isShown: Boolean?) {
        isShown?.let { toolOptionsExpanded ->
            toolOptions.setChildrenToVisible(toolOptionsExpanded)
            if (toolOptionsExpanded) {
                expandedToolOptions.applyTo(toolConstraintLayout)
            } else {
                collapsedToolOptions.applyTo(toolConstraintLayout)
            }
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
        return (screenAnnotator.dispatchKeyEvent(event))
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