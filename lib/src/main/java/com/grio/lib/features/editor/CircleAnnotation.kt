package com.grio.lib.features.editor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CircleAnnotation(
    override var color: String,
    override var size: Float,
    var center: PointF,
    var radius: Float
) : ShapeAnnotation {

    constructor(color: String, x: Float, y: Float): this(color, 15f, PointF(x, y), 0f)

    override fun drawShape(canvas: Canvas?, brush: Paint) {
        canvas?.drawCircle(center.x, center.y, radius, brush)
    }

    override fun updateShape(x: Float, y: Float) {
        val distance = sqrt(((center.x.toDouble() - x).pow(2.0) + (center.y.toDouble() - y).pow(2.0)))
        radius = distance.toFloat()
    }

    override fun wasSelected(x: Float, y: Float): Boolean {
        val deltaX = x - center.x
        val deltaY = y - center.y
        val angle = atan2(deltaY, deltaX)
        val closestX = (radius * cos(angle.toDouble()) + center.x).toFloat()
        val closestY = (radius * sin(angle.toDouble()) + center.y).toFloat()

        if (x > closestX - CLICKABLE_AREA_ALLOWANCE &&
            x < closestX + CLICKABLE_AREA_ALLOWANCE &&
            y > closestY - CLICKABLE_AREA_ALLOWANCE &&
            y < closestY + CLICKABLE_AREA_ALLOWANCE)
            return true
        return false
    }

    override fun getRect(): RectF {
        return RectF(
            center.x - radius - size,
            center.y - radius - size,
            center.x + radius + size,
            center.y + radius + size
        )
    }
}
