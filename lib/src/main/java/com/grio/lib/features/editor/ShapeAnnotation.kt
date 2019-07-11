package com.grio.lib.features.editor

import com.grio.lib.features.editor.views.Shape

interface ShapeAnnotation : BaseAnnotation {
    fun updateShape(x: Float, y: Float)

    companion object {
        const val CLICKABLE_AREA_ALLOWANCE = 40f

        fun createShape(color: String, size: Float, x: Float, y: Float, type: Shape): ShapeAnnotation {
            return when (type) {
                Shape.RECTANGLE -> RectangleAnnotation(color, x, y)
                Shape.CIRCLE -> CircleAnnotation(color, x, y)
                Shape.ARROW -> ArrowAnnotation(color, x, y)
            }
        }
    }
}