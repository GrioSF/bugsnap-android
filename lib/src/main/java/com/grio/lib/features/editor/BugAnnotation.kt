package com.grio.lib.features.editor

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

interface BugAnnotation {
    var color: String
    var size: Float
    // Returns whether or not the annotation was selected
    fun wasSelected(x: Float, y: Float): Boolean
    // Returns the rectangle bounding the annotation
    fun getRect(): RectF
}

data class PenAnnotation(
    override var color: String,
    override var size: Float,
    var drawnPath: Path,
    var startX: Float,
    var startY: Float,
    var endX: Float,
    var endY: Float,
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) : BugAnnotation {

    constructor(color: String, size: Float, x: Float, y: Float) :
            this(color, size, Path(), x, y, x, y, x, y, x, y)

    override fun wasSelected(x: Float, y: Float): Boolean {
        val touchArea = RectF(x - 1, y - 1, x + 1, y + 1)
        val touchPath = Path()
        touchPath.moveTo(x, y)
        touchPath.addRect(touchArea, Path.Direction.CW)
        touchPath.op(drawnPath, Path.Op.DIFFERENCE)
        if (touchPath.isEmpty) return true
        return false
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

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    var text: String,
    val x: Float,
    val y: Float
) : BugAnnotation {

    constructor(color: String, size: Float, x: Float, y: Float) :
            this(color, size, "", x, y)

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

data class ShapeAnnotation(
    override var color: String,
    override var size: Float,
    var startX: Float,
    var startY: Float,
    var endX: Float,
    var endY: Float
) : BugAnnotation {
    override fun wasSelected(x: Float, y: Float): Boolean {
        return when {
            // check left
            x > startX - size && x < startX + size && y > startY - size && y < endY + size -> true
            // check right
            x > endX - size && x < endX + size && y > startY - size && y < endY + size -> true
            // check top
            y > startY - size && y < startY + size && x > startX - size && x < endX + size -> true
            // check bottom
            y > endY - size && y < endY + size && x > startX - size && x < endX + size -> true
            // default
            else -> false
        }
    }

    override fun getRect(): RectF {
        return RectF(startX - size, startY - size, endX + size, endY + size)
    }
}