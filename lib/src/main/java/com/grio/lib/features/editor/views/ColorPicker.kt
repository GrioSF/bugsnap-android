package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var palette = arrayListOf<Swatch>()
    private var radius = 0
    private var paint = Paint()

    private lateinit var listener: Listener

    init {
        paint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }

        palette.add(Swatch("#000000"))
        palette.add(Swatch("#FFFFFF"))
        palette.add(Swatch("#0536FF"))
        palette.add(Swatch("#3EB327"))
        palette.add(Swatch("#FFF404"))
        palette.add(Swatch("#FF6612"))
        palette.add(Swatch("#EA0000"))
        palette.add(Swatch("#9F00FF"))
    }

    interface Listener {
        fun colorPicked(color: String)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        radius = width/(palette.size*3)

        var position = radius.toFloat()
        for (swatch in palette) {
            swatch.position = position
            position += radius * 3
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                listener.colorPicked(getColorForTouch(event.x))
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getColorForTouch(x: Float): String {
        for (swatch in palette) {
            if (x > swatch.position - radius && x < swatch.position + radius) {
                return swatch.color
            }
        }
        return "#000000"
    }

    fun setColorPickerListener(listenerToSet: Listener) {
        this.listener = listenerToSet
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (swatch in palette) {
            paint.color = Color.parseColor(swatch.color)
            canvas?.drawCircle(swatch.position, height/2.toFloat(), radius.toFloat(), paint)
        }
    }

}

data class Swatch(
    val color: String,
    var radius: Float = 0f,
    var position: Float = 0f
)