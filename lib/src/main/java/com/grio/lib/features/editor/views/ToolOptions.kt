package com.grio.lib.features.editor.views

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.marginStart


class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val arrow = context.getDrawable(com.grio.lib.R.drawable.ic_keyboard_arrow_left_white_24dp)
    private val density = context.resources.displayMetrics.density.toInt()
    private var drawerState = DrawerState.CLOSED

    private var margin = 0

    private var arrowDimen = 12 * density

    private lateinit var listener: Listener

    private val colorPicker = ColorPicker(context)
    private val slider = SeekBar(context)

    /**
     * Listens for color selections
     */
    interface Listener {
        // Called when color is selected
        fun clicked(margin: Int)
        fun colorSelected(color: String)
        fun strokeWidthSet(strokeWidth: Float)
    }

    /**
     * Sets listener for LineToolSelector
     *
     * @param listenerToSet listener to attach to this view
     */
    fun setToolOptionsListener(listenerToSet: Listener) {
        this.listener = listenerToSet
    }

    fun setViewsToVisible(isVisible: Boolean) {
        colorPicker.visibility = if (isVisible) VISIBLE else GONE
        slider.visibility = if (isVisible) VISIBLE else GONE
    }

    init {
        addView(colorPicker)
        addView(slider)

        colorPicker.id = View.generateViewId()
        slider.id = View.generateViewId()

        colorPicker.visibility = GONE
        slider.visibility = GONE

        colorPicker.setColorPickerListener(object : ColorPicker.Listener {
            override fun colorPicked(color: String) {
                listener.colorSelected(color)
            }
        })

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener.strokeWidthSet(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (margin <= 0) {
            arrowDimen = 12 * density
            margin = 16 * density
            layoutParams.width = context.resources.displayMetrics.widthPixels - margin

            val params = LayoutParams(LayoutParams.MATCH_PARENT, height / 2)
            params.marginStart = arrowDimen * 4
            params.marginEnd = arrowDimen * 4
            params.gravity = Gravity.CENTER

            slider.layoutParams = params
            colorPicker.layoutParams = params
        }
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        arrow?.setBounds(
            arrowDimen,
            height / 2 - arrowDimen,
            3 * arrowDimen,
            height / 2 + arrowDimen
        )
        canvas?.let {
            arrow?.draw(it)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (drawerState == DrawerState.CLOSED) {
                    openDrawer()
                } else if (drawerState == DrawerState.OPENED) {
                    closeDrawer()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun openDrawer() {
        listener.clicked(16 * density)
    }

    private fun closeDrawer() {
        listener.clicked(16 * density)
    }
}

enum class DrawerState {
    OPENED, CLOSED, ANIMATING
}