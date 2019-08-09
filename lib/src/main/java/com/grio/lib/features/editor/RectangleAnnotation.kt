package com.grio.lib.features.editor

import android.graphics.*
import com.grio.lib.core.extension.updateWithDelta
import com.grio.lib.features.editor.ShapeAnnotation.Companion.CLICKABLE_AREA_ALLOWANCE
import kotlin.math.max
import kotlin.math.min

data class RectangleAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var start: PointF,
    var end: PointF
) : ShapeAnnotation {

    init {
        defaultBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size
            color = Color.parseColor(this@RectangleAnnotation.color)
        }
    }

    constructor(color: String, x: Float, y: Float) : this(color, 15f, Paint(), null, PointF(x, y), PointF(x, y))

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawRect(
            min(start.x, end.x),
            min(start.y, end.y),
            max(end.x, start.x),
            max(end.y, start.y), defaultBrush
        )
    }

    override fun updateShape(x: Float, y: Float) {
        end.x = x
        end.y = y
    }

    override fun move(x: Float, y: Float) {
        lastClick?.let {
            val dx = x - it.x
            val dy = y - it.y
            start.updateWithDelta(dx, dy)
            end.updateWithDelta(dx, dy)
            it.set(x, y)
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        return when {
            // check left
            x > start.x - CLICKABLE_AREA_ALLOWANCE && x < start.x + CLICKABLE_AREA_ALLOWANCE &&
                    y > start.y - CLICKABLE_AREA_ALLOWANCE && y < end.y + CLICKABLE_AREA_ALLOWANCE -> true
            // check right
            x > end.x - CLICKABLE_AREA_ALLOWANCE && x < end.x + CLICKABLE_AREA_ALLOWANCE &&
                    y > start.y - CLICKABLE_AREA_ALLOWANCE && y < end.y + CLICKABLE_AREA_ALLOWANCE -> true
            // check top
            y > start.y - CLICKABLE_AREA_ALLOWANCE && y < start.y + CLICKABLE_AREA_ALLOWANCE &&
                    x > start.x - CLICKABLE_AREA_ALLOWANCE && x < end.x + CLICKABLE_AREA_ALLOWANCE -> true
            // check bottom
            y > end.y - CLICKABLE_AREA_ALLOWANCE && y < end.y + CLICKABLE_AREA_ALLOWANCE && x
                    > start.x - CLICKABLE_AREA_ALLOWANCE && x < end.x + CLICKABLE_AREA_ALLOWANCE -> true
            // default
            else -> false
        }
    }

    override fun getRect(): RectF {
        return RectF(start.x - size, start.y - size, end.x + size, end.y + size)
    }
}