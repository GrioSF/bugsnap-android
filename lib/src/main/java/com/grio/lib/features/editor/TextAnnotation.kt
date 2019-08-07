package com.grio.lib.features.editor

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
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
            this(color, size, Paint(), "", x, y, Paint(), Paint())

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
            y < this.y
        ) {
            return true
        }
        return false
    }

    override fun move(x: Float, y: Float) {
        this.x = x
        this.y = y
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