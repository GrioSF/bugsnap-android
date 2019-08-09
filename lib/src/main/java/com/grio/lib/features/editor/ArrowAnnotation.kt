package com.grio.lib.features.editor

import android.graphics.*
import com.grio.lib.features.editor.ShapeAnnotation.Companion.CLICKABLE_AREA_ALLOWANCE
import kotlin.math.max
import kotlin.math.min

data class ArrowAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var start: PointF,
    var end: PointF,
    var arrowLeftEnd: PointF,
    var arrowRightEnd: PointF
) : ShapeAnnotation {

    init {
        defaultBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size
            color = Color.parseColor(this@ArrowAnnotation.color)
        }
    }

    constructor(color: String, x: Float, y: Float) :
            this(color, 15f, Paint(), null, PointF(x, y), PointF(x, y), PointF(x, y), PointF(x, y))

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawLine(start.x, start.y, end.x, end.y, defaultBrush)
        canvas?.drawLine(end.x, end.y, arrowLeftEnd.x, arrowLeftEnd.y, defaultBrush)
        canvas?.drawLine(end.x, end.y, arrowRightEnd.x, arrowRightEnd.y, defaultBrush)
    }

    override fun updateShape(x: Float, y: Float) {
        end.x = x
        end.y = y
        val deltaX = x - start.x
        val deltaY = y - start.y
        val headSizeToLineLengthRatio = 0.1f

        // calculate left side of arrow head
        arrowLeftEnd.x = end.x - (headSizeToLineLengthRatio * deltaX + headSizeToLineLengthRatio * deltaY)
        arrowLeftEnd.y = end.y - (headSizeToLineLengthRatio * deltaY - headSizeToLineLengthRatio * deltaX)

        // calculate right side of arrow head
        arrowRightEnd.x = end.x - (headSizeToLineLengthRatio * deltaX - headSizeToLineLengthRatio * deltaY)
        arrowRightEnd.y = end.y - (headSizeToLineLengthRatio * deltaY + headSizeToLineLengthRatio * deltaX)
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        // Use the below form for the equations for the line of the arrow and
        // the line normal to it which passes through the touched point
        // a1x + b1y = c1
        // a2x + b2y = c2
        val slope = (end.y - start.y) / (end.x - start.x)
        val normalSlope = -1 / slope
        val a1 = -slope
        val a2 = -normalSlope
        val b1 = 1
        val b2 = 1
        val c1 = start.y - (slope * start.x)
        val c2 = y - (normalSlope * x)

        // Use the determinant method to solve for x and y and bound the result to the drawn line
        // this produces the closest point on the line to the touched point
        var closestX = ((c1 * b2) - (b1 * c2)) / ((a1 * b2) - (b1 * a2))
        var closestY = ((a1 * c2) - (c1 * a2)) / ((a1 * b2) - (b1 * a2))
        when {
            closestX >= max(start.x, end.x) -> closestX = max(start.x, end.x)
            closestX <= min(start.x, end.x) -> closestX = min(start.x, end.x)
        }
        when {
            closestY >= max(start.y, end.y) -> closestY = max(start.y, end.y)
            closestY <= min(start.y, end.y) -> closestY = min(start.y, end.y)
        }

        // Check if the touched point is within the allowed area of the closest point
        if (x > closestX - CLICKABLE_AREA_ALLOWANCE && x < closestX + CLICKABLE_AREA_ALLOWANCE &&
            y > closestY - CLICKABLE_AREA_ALLOWANCE && y < closestY + CLICKABLE_AREA_ALLOWANCE
        ) {
            return true
        }
        return false
    }

    override fun move(x: Float, y: Float) {
        lastClick?.let {
            val dx = x - it.x
            val dy = y - it.y
            start.x += dx
            start.y += dy
            end.x += dx
            end.y += dy
            arrowLeftEnd.x += dx
            arrowLeftEnd.y += dy
            arrowRightEnd.x += dx
            arrowRightEnd.y += dy
            it.x = x
            it.y = y
        } ?: run {
            lastClick = PointF(x, y)
        }
    }

    override fun getRect(): RectF {
        return RectF(
            min(min(start.x - size, arrowLeftEnd.x - size), min(end.x - size, arrowRightEnd.x - size)),
            min(min(start.y - size, arrowLeftEnd.y - size), min(end.y - size, arrowRightEnd.y - size)),
            max(max(start.x + size, arrowLeftEnd.x + size), max(end.x + size, arrowRightEnd.x + size)),
            max(max(start.y + size, arrowLeftEnd.y + size), max(end.y + size, arrowRightEnd.y + size))
        )
    }
}