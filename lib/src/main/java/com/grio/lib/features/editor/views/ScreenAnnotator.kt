package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.grio.lib.core.extension.screenshot

/**
 * View that allows for simple drawing onto the screen
 */
class ScreenAnnotator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Graphics
    private var brush = Paint()
    private var annotations = arrayListOf<Annotation>()
    private lateinit var originalScreenshot: Bitmap

    // State
    private var paintColor = "#000000"
    private var xStart = 0f
    private var yStart = 0f
    private var xCurrent = 0f
    private var yCurrent = 0f

    private lateinit var listener: Listener

    init {
        brush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.RED
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startDrawing(event.x, event.y)
            MotionEvent.ACTION_MOVE -> recordMovement(event.x, event.y)
            MotionEvent.ACTION_UP -> performClick()
        }
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        stopRecording()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (::originalScreenshot.isInitialized) {
            canvas?.drawBitmap(originalScreenshot, 0f, 0f, brush)
        }
        for (annotation in annotations) {
            brush.color = Color.parseColor(annotation.color)
            canvas?.drawPath(annotation.drawnPath, brush)
        }
    }

    /**
     * Set originalScreenshot to annotator
     */
    fun setScreenshot(screenshotToAnnotate: Bitmap?) {
        screenshotToAnnotate?.let {
            originalScreenshot = it
        }
    }

    /**
     * Retrieve the current view of the annotations made
     */
    fun getAnnotatedScreenshot(): Bitmap {
        return this.screenshot()
    }

    /**
     * Listens for Annotation Events
     */
    interface Listener {
        // Fired when a line is drawn to the screen
        fun lineDrawn()

        // Fired when all lines drawn to screen have been removed
        fun canvasIsBlank()
    }

    /**
     * Sets listener for ScreenAnnotator
     */
    fun setEventListener(listener: Listener) {
        this.listener = listener
    }

    /**
     * Removes the last line drawn
     */
    fun undo() {
        if (annotations.isNotEmpty()) {
            annotations.remove(annotations.last())
            if (annotations.isEmpty())
                listener.canvasIsBlank()
            invalidate()
        }
    }

    fun setPaintColor(color: String) {
        paintColor = color
    }

    /**
     * Record when user touches the screen
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun startDrawing(x: Float, y: Float) {
        annotations.add(Annotation(paintColor, Path()))
        annotations.last().drawnPath.moveTo(x, y)
        xStart = x
        yStart = y
        xCurrent = x
        yCurrent = y
    }

    /**
     * Record movement of touch and update path
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun recordMovement(x: Float, y: Float) {
        annotations.last().drawnPath.quadTo(xCurrent, yCurrent, (x + xCurrent) / 2, (y + yCurrent) / 2)
        xCurrent = x
        yCurrent = y
    }

    /**
     * User stopped touching screen. Stop recording.
     */
    private fun stopRecording() {
        annotations.last().drawnPath.lineTo(xCurrent, yCurrent)

        // If user taps screen, create a dot
        if (xStart == xCurrent && yStart == yCurrent) {
            brush.style = Paint.Style.FILL
            annotations.last().drawnPath.addCircle(xCurrent, yCurrent, 4f, Path.Direction.CW)
            brush.style = Paint.Style.STROKE
        }
        listener.lineDrawn()
    }
}

data class Annotation(
    val color: String,
    val drawnPath: Path
)