package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.grio.lib.R

const val CARET_DIMEN = 12
const val CARET_MARGIN = 16

class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val caret = context.getDrawable(R.drawable.caret_icon)
    private val density = context.resources.displayMetrics.density.toInt()
    private var arrowDimen = CARET_DIMEN * density
    private val colorPicker: ColorPicker
    private val slider: SeekBar
    private val shapeButtonHolder: LinearLayout
    private val rectangleButton: ImageView
    private val circleButton: ImageView
    private val arrowButton: ImageView
    private val sizeLabel: TextView
    private val shapeLabel: TextView
    lateinit var listener: Listener
    private var toolOptionsDrawerType = ToolDrawerType.SLIDER

    init {
        View.inflate(context, R.layout.v_tool_options, this)
        colorPicker = findViewById(R.id.color_picker)
        slider = findViewById(R.id.slider)
        shapeButtonHolder = findViewById(R.id.shape_button_holder)
        rectangleButton = findViewById(R.id.rectangle_shape)
        circleButton = findViewById(R.id.circle_shape)
        arrowButton = findViewById(R.id.arrow_shape)
        sizeLabel = findViewById(R.id.size_label)
        shapeLabel = findViewById(R.id.shape_label)

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
            shapeButtonHolder.visibility = View.GONE
            shapeLabel.visibility = View.GONE
            slider.visibility = View.VISIBLE
            sizeLabel.visibility = View.VISIBLE
        } else if (toolOptionsDrawerType == ToolDrawerType.SHAPES) {
            slider.visibility = View.GONE
            sizeLabel.visibility = View.GONE
            shapeButtonHolder.visibility = View.VISIBLE
            shapeLabel.visibility = View.VISIBLE
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