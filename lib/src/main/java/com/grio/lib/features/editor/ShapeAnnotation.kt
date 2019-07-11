package com.grio.lib.features.editor

import android.graphics.Canvas
import android.graphics.Paint
import com.grio.lib.features.editor.views.Shape

interface ShapeAnnotation : BugAnnotation {
    fun drawShape(canvas: Canvas?, brush: Paint)
    fun updateShape(x: Float, y: Float)

    companion object {
        fun createShape(color: String, size: Float, x: Float, y: Float, type: Shape): ShapeAnnotation {
            return when (type) {
                Shape.RECTANGLE -> RectangleAnnotation(color, x, y)
                Shape.CIRCLE -> CircleAnnotation(color, x, y)
                Shape.ARROW -> ArrowAnnotation(color, x, y)
            }
        }
    }
}