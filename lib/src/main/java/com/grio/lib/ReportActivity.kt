package com.grio.lib

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.a_report.*
import android.util.DisplayMetrics
import androidx.constraintlayout.widget.ConstraintSet
import android.util.TypedValue
import android.view.animation.OvershootInterpolator

const val UNDO_BUTTON_MARGIN_END = 20f

class ReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_report)

        setupToolbar()
        setupScreenAnnotator()
        setScreenshotToHolder()

        finishEditing.setOnClickListener {
            // do something on finish editing
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
     * Attaches Annotator to Screenshot by setting them both to
     * be 75% of the screen size. Both views are centered on screen in XML.
     * Attaches Annotation Listener to show and hide undo button.
     */
    private fun setupScreenAnnotator() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val layoutParams = screenshotHolder.layoutParams
        layoutParams.width = (width * 0.75).toInt()
        layoutParams.height = (height * 0.75).toInt()
        screenshotHolder.layoutParams = layoutParams
        screenAnnotator.layoutParams = layoutParams

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
        screenshotHolder.setImageBitmap(bitmap)
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