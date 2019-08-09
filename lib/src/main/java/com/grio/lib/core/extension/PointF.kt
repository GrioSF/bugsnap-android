package com.grio.lib.core.extension

import android.graphics.PointF

/**
 * Update point based on delta for dragging feature
 */
fun PointF.updateWithDelta(dx: Float, dy: Float) {
    this.x += dx
    this.y += dy
}