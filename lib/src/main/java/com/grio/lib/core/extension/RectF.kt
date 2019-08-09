package com.grio.lib.core.extension

import android.graphics.RectF

fun RectF.updateWithDelta(dx: Float, dy: Float) {
    this.left += dx
    this.top += dy
    this.right += dx
    this.bottom += dy
}