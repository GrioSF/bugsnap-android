package com.grio.lib.features.editor

import android.graphics.Path
import android.widget.EditText
import android.widget.LinearLayout

interface BugAnnotation {
    var color: String
    var size: Float
}

data class PenAnnotation(
    override var color: String,
    override var size: Float,
    var drawnPath: Path
) : BugAnnotation

data class TextAnnotation(
    override var color: String,
    override var size: Float,
    var text: String,
    val x: Float,
    val y: Float
) : BugAnnotation

data class ShapeAnnotation(
    override var color: String,
    override var size: Float,
    var startX: Float,
    var startY: Float,
    var endX: Float,
    var endY: Float
) : BugAnnotation