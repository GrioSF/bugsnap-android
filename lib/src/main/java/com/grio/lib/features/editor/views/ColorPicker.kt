package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

const val STROKE_THICKNESS = 4f
const val HIGHLIGHT_SPACING_FROM_SWATCH = 8f
const val PALETTE_SPACING_FACTOR = 3
const val INITIAL_SWATCH_OFFSET_FACTOR = 1.75f

class ColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var palette = arrayListOf(
        Swatch("#000000"), Swatch("#FFFFFF"), Swatch("#0536FF"), Swatch("#3EB327"),
        Swatch("#FFF404"), Swatch("#FF6612"), Swatch("#EA0000"), Swatch("#9F00FF")
    )
    private var radius = 0
    private var swatchPaint = Paint()
    private var highlightPaint = Paint()
    lateinit var listener: Listener
    private var selectedColorIndex = 0

    init {
        highlightPaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_THICKNESS
            color = Color.WHITE
        }

        swatchPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_THICKNESS
        }
    }

    /**
     * Listens for color changes
     */
    interface Listener {
        // Fires when color is picked
        fun colorPicked(color: String)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // Calculate radius based on screen size
        radius = width / (palette.size * PALETTE_SPACING_FACTOR)

        // Calculate swatch positions and sizes based on screen size
        var position = radius.toFloat() * INITIAL_SWATCH_OFFSET_FACTOR
        palette.forEach { swatch ->
            swatch.position = position
            position += radius * PALETTE_SPACING_FACTOR
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                listener.colorPicked(getColorForTouch(event.x))
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Detect which color was selected
     *
     * @param x the x coordinate of the touch onto the view
     */
    private fun getColorForTouch(x: Float): String {
        for ((index, swatch) in palette.withIndex()) {
            if (x > swatch.position - radius && x < swatch.position + radius) {
                selectedColorIndex = index
                invalidate()
                return swatch.color
            }
        }
        return "#000000"
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for ((index, swatch) in palette.withIndex()) {
            swatchPaint.color = Color.parseColor(swatch.color)
            canvas?.drawCircle(swatch.position, height / 2f, radius.toFloat(), swatchPaint)

            if (index == selectedColorIndex) {
                canvas?.drawCircle(
                    swatch.position,
                    height / 2f,
                    radius.toFloat() + HIGHLIGHT_SPACING_FROM_SWATCH,
                    highlightPaint
                )
            }
        }
    }
}

data class Swatch(
    val color: String,
    var radius: Float = 0f,
    var position: Float = 0f
)