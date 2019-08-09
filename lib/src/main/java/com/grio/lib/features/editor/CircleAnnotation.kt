package com.grio.lib.features.editor

import android.graphics.*
import com.grio.lib.features.editor.ShapeAnnotation.Companion.CLICKABLE_AREA_ALLOWANCE
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CircleAnnotation(
    override var color: String,
    override var size: Float,
    override var defaultBrush: Paint,
    override var lastClick: PointF?,
    var center: PointF,
    var radius: Float
) : ShapeAnnotation {

    init {
        defaultBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size
            color = Color.parseColor(this@CircleAnnotation.color)
        }
    }

    constructor(color: String, x: Float, y: Float): this(color, 15f, Paint(), null, PointF(x, y), 0f)

    override fun drawToCanvas(canvas: Canvas?) {
        canvas?.drawCircle(center.x, center.y, radius, defaultBrush)
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

    override fun move(x: Float, y: Float) {
        lastClick?.let {
            val dx = x - it.x
            val dy = y - it.y
            center.x += dx
            center.y += dy
            it.x = x
            it.y = y
        } ?: run {
            lastClick = PointF(x, y)
        }
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
