package com.grio.lib.features.editor

import android.graphics.*
import android.util.Log

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var text: String,
    var x: Float,
    var y: Float,
    var textBackgroundBrush: Paint,
    private var textBrush: Paint
) : BaseAnnotation {

    init {
        textBackgroundBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
            color = Color.parseColor(this@TextAnnotation.color)
        }

        textBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            color = Color.WHITE
            textSize = size
        }
    }

    constructor(color: String, size: Float, x: Float, y: Float) :
            this(color, size, Paint(), null, "", x, y, Paint(), Paint())

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawRect(getRect(), textBackgroundBrush)
        canvas?.drawText(
            text,
            x - textBrush.measureText(text) / 2,
            y - size / 2,
            textBrush
        )
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        val paint = Paint()
        paint.textSize = size
        val textWidth = paint.measureText(text)

        if (x > (this.x - textWidth / 2 - (size / 2)) &&
            x < (this.x + textWidth / 2 + (size / 2)) &&
            y > (this.y - (size / 2) - size) &&
            y < this.y + size / 2
        ) {
            Log.wtf("WAS_SELECTED", "True")
            return true
        }
        Log.wtf("WAS_SELECTED", "False")
        return false
    }

    override fun move(x: Float, y: Float) {
        lastClick?.let {
            val dx = x - it.x
            val dy = y - it.y
            this.x += dx
            this.y += dy
            it.x = x
            it.y = y
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun getRect(): RectF {
        val paint = Paint()
        paint.textSize = size
        val textWidth = paint.measureText(text)

        return RectF(
            x - textWidth / 2 - size / 2,
            y - size / 2 - size,
            x + textWidth / 2 + size / 2,
            y
        )
    }
}