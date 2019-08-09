package com.grio.lib.features.editor

import android.graphics.*
import android.util.Log
import com.grio.lib.core.extension.updateWithDelta

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var text: String,
    var center: PointF,
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
            this(color, size, Paint(), null, "", PointF(x, y), Paint(), Paint())

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawRect(getRect(), textBackgroundBrush)
        canvas?.drawText(
            text,
            center.x - textBrush.measureText(text) / 2,
            center.y - size / 2,
            textBrush
        )
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        val paint = Paint()
        paint.textSize = size
        val textWidth = paint.measureText(text)

        if (x > (center.x - textWidth / 2 - (size / 2)) &&
            x < (center.x + textWidth / 2 + (size / 2)) &&
            y > (center.y - (size / 2) - size) &&
            y < center.y + size / 2
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
            center.updateWithDelta(dx, dy)
            it.set(x, y)
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun getRect(): RectF {
        val paint = Paint()
        paint.textSize = size
        val textWidth = paint.measureText(text)

        return RectF(
            center.x - textWidth / 2 - size / 2,
            center.y - size / 2 - size,
            center.x + textWidth / 2 + size / 2,
            center.y
        )
    }
}