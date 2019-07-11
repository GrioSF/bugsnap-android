package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import com.grio.lib.R

const val CARET_DIMEN = 12
const val CARET_MARGIN = 16

class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val caret = context.getDrawable(R.drawable.caret_icon)
    private val density = context.resources.displayMetrics.density.toInt()
    private var margin = 0
    private var arrowDimen = CARET_DIMEN * density
    private val colorPicker = ColorPicker(context)
    private val slider = SeekBar(context)
    private val shapeButtonHolder = LinearLayout(context)
    private val rectangleButton = ImageButton(context)
    private val circleButton = ImageButton(context)
    private val arrowButton = ImageButton (context)
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
        shapeButtonHolder.weightSum = 3f

        rectangleButton.setImageDrawable(context.getDrawable(R.drawable.rectangle_shape_tool_icon))
        arrowButton.setImageDrawable(context.getDrawable(R.drawable.arrow_shape_tool_icon))
        circleButton.setImageDrawable(context.getDrawable(R.drawable.circle_shape_tool_icon))
        rectangleButton.background = null
        arrowButton.background = null
        circleButton.background = null
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

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (margin <= 0) {
            arrowDimen = CARET_DIMEN * density
            margin = CARET_MARGIN * density
            layoutParams.width = context.resources.displayMetrics.widthPixels - margin

            val params = LayoutParams(LayoutParams.MATCH_PARENT, height / 2)
            params.marginStart = arrowDimen * 4
            params.marginEnd = arrowDimen * 4
            params.gravity = Gravity.CENTER

            val buttonParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            buttonParams.weight = 1f
            params.gravity = Gravity.CENTER

            slider.layoutParams = params
            colorPicker.layoutParams = params
            arrowButton.layoutParams = buttonParams
            rectangleButton.layoutParams = buttonParams
            circleButton.layoutParams = buttonParams
            shapeButtonHolder.layoutParams = params
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        caret?.setBounds(
            arrowDimen,
            height / 2 - arrowDimen,
            3 * arrowDimen,
            height / 2 + arrowDimen
        )
        canvas?.let {
            caret?.draw(it)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> listener.toggleDrawer(CARET_MARGIN * density)
        }
        return super.onTouchEvent(event)
    }

    /**
     * Sets the drawer to show options based on the selected tool
     *
     * @param type the type of drawer format to display
     */
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
}

enum class ToolDrawerType {
    SLIDER, SHAPES
}