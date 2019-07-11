package com.grio.lib.features.editor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

interface BaseAnnotation {
    var color: String
    var size: Float
    var defaultBrush: Paint

    // Draws annotation onto the screen
    fun drawToCanvas(canvas: Canvas?)

    // Returns whether or not the annotation was selected
    fun wasSelected(x: Float, y: Float): Boolean

    // Returns the rectangle bounding the annotation
    fun getRect(): RectF
}