package com.grio.lib.features.editor

import android.graphics.*
import kotlin.math.max
import kotlin.math.min

data class PenAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var drawnPath: Path,
    var startX: Float,
    var startY: Float,
    var endX: Float,
    var endY: Float,
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
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
            this(color, size, Paint(), null, Path(), x, y, x, y, x, y, x, y)

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
            startX += dx
            endX += dx
            left += dx
            right += dx
            startY += dy
            endY += dy
            top += dy
            bottom += dy
            drawnPath.offset(dx, dy)
            it.x = x
            it.y = y
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun getRect(): RectF {
        return RectF(
            left - size / 2,
            top - size / 2,
            right + size / 2,
            bottom + size / 2
        )
    }

    fun updateBounds(x: Float, y: Float) {
        endX = x
        endY = y
        left = min(x, left)
        top = min(y, top)
        right = max(x, right)
        bottom = max(y, bottom)
    }
}