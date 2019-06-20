package com.grio.lib.features.report

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.google.gson.Gson
import com.grio.lib.core.di.DaggerInjector
import com.grio.lib.features.editor.views.LineToolSelector
import com.grio.lib.R
import com.grio.lib.core.platform.BaseActivity
import com.grio.lib.features.editor.views.ScreenAnnotator
import kotlinx.android.synthetic.main.a_report.*
import javax.inject.Inject

const val UNDO_BUTTON_MARGIN_END = 20f


class ReportActivity : BaseActivity() {

    @Inject
    lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_report)
        DaggerInjector.getComponent().inject(this)

        setupToolbar()
        setupScreenAnnotator()
        setScreenshotToHolder()
        setupLineSelector()

        finishEditing.setOnClickListener {
            // TODO: Update to transfer proper data to secondary Activity.
            // Launches the summary Activity.
            startActivity(Intent(this, ReportSummaryActivity::class.java))
        }

        undo.setOnClickListener {
            screenAnnotator.undo()
        }
    }

    /**
     * Sets up toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Report a bug"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Attaches Annotation Listener to show and hide undo button.
     */
    private fun setupScreenAnnotator() {
        screenAnnotator.setPaintColor(lineSelector.getPaintColor())
        screenAnnotator.setEventListener(object : ScreenAnnotator.Listener {
            override fun lineDrawn() {
                toggleUndoButton(true)
            }

            override fun canvasIsBlank() {
                toggleUndoButton(false)
            }
        })
    }

    /**
     * Attaches Color Listener to select line colors
     */
    private fun setupLineSelector() {
        lineSelector.setColorListener(object : LineToolSelector.Listener {
            override fun colorSelected(color: String) {
                screenAnnotator.setPaintColor(color)
            }
        })
    }

    /**
     * Toggles undo button on and off of the screen
     *
     * @param shouldShowUndo whether or not undo button should be shown
     */
    private fun toggleUndoButton(shouldShowUndo: Boolean) {
        // Calculate pixels for end margin of undo button
        val pixelsForDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            UNDO_BUTTON_MARGIN_END,
            resources.displayMetrics
        ).toInt()

        val layout = ConstraintSet()
        layout.clone(reportActivityConstrainLayout)
        // Clear previous constraint so view is properly hidden/shown
        layout.clear(
            R.id.undo,
            if (shouldShowUndo) ConstraintSet.START else ConstraintSet.END
        )
        // Attach either start or end constraint of undo button to parent (showing/hiding)
        layout.connect(
            R.id.undo,
            if (shouldShowUndo) ConstraintSet.END else ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        // Set previously calculated end margin for view
        layout.setMargin(R.id.undo, ConstraintSet.END, pixelsForDp)

        val transitionStyle = ChangeBounds()
        transitionStyle.interpolator = OvershootInterpolator()
        TransitionManager.beginDelayedTransition(reportActivityConstrainLayout, transitionStyle)
        layout.applyTo(reportActivityConstrainLayout)
    }

    /**
     * Attaches saved screenshot to ImageView
     */
    private fun setScreenshotToHolder() {
        val bitmap = DataHolder.data
        screenAnnotator.setScreenshot(bitmap)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
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