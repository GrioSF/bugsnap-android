package com.grio.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * View that allows for simple drawing onto the screen
 */
class ScreenAnnotator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Graphics
    private var brush = Paint()
    private var drawPath = Path()

    // State
    private var xStart = 0f
    private var yStart = 0f
    private var xCurrent = 0f
    private var yCurrent = 0f

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

    /**
     * Record when user touches the screen
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun startDrawing(x: Float, y: Float) {
        drawPath.moveTo(x, y)
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
        drawPath.quadTo(xCurrent, yCurrent, (x + xCurrent) / 2, (y + yCurrent) / 2)
        xCurrent = x
        yCurrent = y
    }

    /**
     * User stopped touching screen. Stop recording.
     */
    private fun stopRecording() {
        drawPath.lineTo(xCurrent, yCurrent)

        // If user taps screen, create a dot
        if (xStart == xCurrent && yStart == yCurrent) {
            brush.style = Paint.Style.FILL
            drawPath.addCircle(xCurrent, yCurrent, 4f, Path.Direction.CW)
            brush.style = Paint.Style.STROKE
        }
    }

    // Draw onto screen
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(drawPath, brush)
    }
}