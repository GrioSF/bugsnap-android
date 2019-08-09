package com.grio.lib.core.extension

import android.graphics.PointF

fun PointF.updateWithDelta(dx: Float, dy: Float) {
    this.x += dx
    this.y += dy
}