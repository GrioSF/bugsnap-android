package com.grio.lib.core.extension

import android.graphics.RectF

/**
 * Update Rect based on delta for dragging feature
 */
fun RectF.updateWithDelta(dx: Float, dy: Float) {
    this.left += dx
    this.top += dy
    this.right += dx
    this.bottom += dy
}