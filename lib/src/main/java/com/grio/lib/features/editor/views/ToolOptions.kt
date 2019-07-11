package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar

const val ARROW_DIMENSION = 12
const val ARROW_MARGIN = 16

class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val arrow = context.getDrawable(com.grio.lib.R.drawable.ic_keyboard_arrow_left_white_24dp)
    private val density = context.resources.displayMetrics.density.toInt()
    private var margin = 0
    private var arrowDimen = ARROW_DIMENSION * density
    private val colorPicker = ColorPicker(context)
    private val slider = SeekBar(context)
    private val shapeButtonHolder = LinearLayout(context)
    private val rectangleButton = Button(context)
    private val circleButton = Button(context)
    private val arrowButton = Button (context)
    lateinit var listener: Listener
    private var toolOptionsDrawerType = ToolDrawerType.SLIDER

    init {
        addView(colorPicker)
        addView(slider)
        addView(shapeButtonHolder)
        shapeButtonHolder.addView(rectangleButton)
        shapeButtonHolder.addView(circleButton)
        shapeButtonHolder.addView(arrowButton)
        shapeButtonHolder.orientation = HORIZONTAL

        rectangleButton.text = "Rectangle"
        circleButton.text = "Circle"
        arrowButton.text = "Arrow"
        rectangleButton.setBackgroundColor(Color.parseColor("#595959"))
        arrowButton.setBackgroundColor(Color.parseColor("#595959"))
        circleButton.setBackgroundColor(Color.parseColor("#595959"))
        rectangleButton.setTextColor(Color.WHITE)
        circleButton.setTextColor(Color.WHITE)
        arrowButton.setTextColor(Color.WHITE)

        colorPicker.visibility = GONE
        slider.visibility = GONE
        shapeButtonHolder.visibility = GONE

        colorPicker.listener = object : ColorPicker.Listener {
            override fun colorPicked(color: String) {
                listener.colorSelected(color)
            }
        }

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

        rectangleButton.setOnClickListener { listener.shapeSelected(Shape.RECTANGLE) }
        circleButton.setOnClickListener { listener.shapeSelected(Shape.CIRCLE) }
        arrowButton.setOnClickListener { listener.shapeSelected(Shape.ARROW) }
    }

    fun setDrawerType(type: ToolDrawerType) {
        toolOptionsDrawerType = type
        if (toolOptionsDrawerType == ToolDrawerType.SLIDER) {
            slider.visibility = View.VISIBLE
            shapeButtonHolder.visibility = View.GONE
        } else if (toolOptionsDrawerType == ToolDrawerType.SHAPES) {
            slider.visibility = View.GONE
            shapeButtonHolder.visibility = View.VISIBLE
        }
    }

    /**
     * Listens for tool options changes
     */
    interface Listener {
        // Called when view is toggleDrawer
        fun toggleDrawer(margin: Int)

        // Called when color is selected
        fun colorSelected(color: String)

        // Called when stroke width is set
        fun strokeWidthSet(strokeWidth: Float)

        // Called when shape is selected
        fun shapeSelected(shape: Shape)
    }

    /**
     * Sets children of the tool options to be gone when off screen
     */
    fun setChildrenToVisible(isVisible: Boolean) {
        colorPicker.visibility = if (isVisible) VISIBLE else GONE
        slider.visibility = if (isVisible) VISIBLE else GONE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (margin <= 0) {
            arrowDimen = ARROW_DIMENSION * density
            margin = ARROW_MARGIN * density
            layoutParams.width = context.resources.displayMetrics.widthPixels - margin

            val params = LayoutParams(LayoutParams.MATCH_PARENT, height / 2)
            params.marginStart = arrowDimen * 4
            params.marginEnd = arrowDimen * 4
            params.gravity = Gravity.CENTER

            val buttomParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            params.gravity = Gravity.CENTER

            slider.layoutParams = params
            colorPicker.layoutParams = params
            arrowButton.layoutParams = buttomParams
            rectangleButton.layoutParams = buttomParams
            circleButton.layoutParams = buttomParams
            shapeButtonHolder.layoutParams = params
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
            MotionEvent.ACTION_UP -> listener.toggleDrawer(ARROW_MARGIN * density)
        }
        return super.onTouchEvent(event)
    }
}

enum class ToolDrawerType {
    SLIDER, SHAPES
}