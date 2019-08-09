package com.grio.lib.features.editor

import android.graphics.*
import com.grio.lib.core.extension.updateWithDelta
import kotlin.math.max
import kotlin.math.min

data class PenAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var drawnPath: Path,
    var start: PointF,
    var end: PointF,
    var boundingRect: RectF
) : BaseAnnotation {

    init {
        defaultBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size
            color = Color.parseColor(this@PenAnnotation.color)
        }
    }

    constructor(color: String, size: Float, x: Float, y: Float) :
            this(color, size, Paint(), null, Path(), PointF(x, y), PointF(x, y), RectF(x, y, x, y))

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawPath(drawnPath, defaultBrush)
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        // Test if touched point as a path is in the drawn path
        val touchArea = RectF(x - 1, y - 1, x + 1, y + 1)
        val touchPath = Path()
        touchPath.moveTo(x, y)
        touchPath.addRect(touchArea, Path.Direction.CW)
        touchPath.op(drawnPath, Path.Op.DIFFERENCE)
        if (touchPath.isEmpty) return true
        return false
    }

    override fun move(x: Float, y: Float) {
        lastClick?.let {
            val dx = x - it.x
            val dy = y - it.y
            start.updateWithDelta(dx, dy)
            end.updateWithDelta(dx, dy)
            boundingRect.updateWithDelta(dx, dy)
            drawnPath.offset(dx, dy)
            it.set(x, y)
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun getRect(): RectF {
        return RectF(
            boundingRect.left - size / 2,
            boundingRect.top - size / 2,
            boundingRect.right + size / 2,
            boundingRect.bottom + size / 2
        )
    }

    fun updateBounds(x: Float, y: Float) {
        end.set(x, y)
        boundingRect.left = min(x, boundingRect.left)
        boundingRect.top = min(y, boundingRect.top)
        boundingRect.right = max(x, boundingRect.right)
        boundingRect.bottom = max(y, boundingRect.bottom)
    }
}