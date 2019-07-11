package com.grio.lib.features.editor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

data class RectangleAnnotation(
    override var color: String,
    override var size: Float,
    var start: PointF,
    var end: PointF
) : ShapeAnnotation {

    constructor(color: String, x: Float, y: Float): this(color, 15f, PointF(x, y), PointF(x, y))

    override fun drawShape(canvas: Canvas?, brush: Paint) {
        canvas?.drawRect(
            min(start.x, end.x),
            min(start.y, end.y),
            max(end.x, start.x),
            max(end.y, start.y), brush
        )
    }

    override fun updateShape(x: Float, y: Float) {
        end.x = x
        end.y = y
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        val clickableArea = size * 2
        return when {
            // check left
            x > start.x - clickableArea && x < start.x + clickableArea && y > start.y - clickableArea && y < end.y + clickableArea -> true
            // check right
            x > end.x - clickableArea && x < end.x + clickableArea && y > start.y - clickableArea && y < end.y + clickableArea -> true
            // check top
            y > start.y - clickableArea && y < start.y + clickableArea && x > start.x - clickableArea && x < end.x + clickableArea -> true
            // check bottom
            y > end.y - clickableArea && y < end.y + clickableArea && x > start.x - clickableArea && x < end.x + clickableArea -> true
            // default
            else -> false
        }
    }

    override fun getRect(): RectF {
        return RectF(start.x - size, start.y - size, end.x + size, end.y + size)
    }
}