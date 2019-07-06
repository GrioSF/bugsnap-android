package com.grio.lib.features.editor

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

interface BugAnnotation {
    var color: String
    var size: Float
    fun wasSelected(x: Float, y: Float): Boolean
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
    override fun wasSelected(x: Float, y: Float): Boolean {
        val touchPath = Path()
        touchPath.moveTo(x, y)
        val touchArea = RectF(x - 1, y - 1, x + 1, y + 1)
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
}

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    var text: String,
    val x: Float,
    val y: Float
) : BugAnnotation {
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
        return false
    }

    override fun getRect(): RectF {
        return RectF()
    }
}