package com.grio.lib.features.editor

import android.graphics.Path
import com.grio.lib.features.editor.views.Tool

interface BugAnnotation {
    var color: String
    var size: Float
    val type: Tool
}

data class PenAnnotation(
    override var color: String,
    override var size: Float,
    override val type: Tool,
    var drawnPath: Path
) : BugAnnotation