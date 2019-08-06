package com.grio.lib.features.editor.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.grio.lib.R
import kotlinx.android.synthetic.main.v_tool_options.view.*

const val CARET_MARGIN = 16

class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val density = context.resources.displayMetrics.density.toInt()
    lateinit var listener: Listener
    private var currentTool = Tool.PEN

    init {
        View.inflate(context, R.layout.v_tool_options, this)

        color_picker.listener = object : ColorPicker.Listener {
            override fun colorPicked(color: String) {
                listener.colorSelected(color)
                tool_preview.updateColor(color)
            }
        }

        size_slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener.strokeWidthSet(progress.toFloat())
                tool_preview.updateSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }
        })

        rectangle_shape.setOnClickListener { listener.shapeSelected(Shape.RECTANGLE) }
        circle_shape.setOnClickListener { listener.shapeSelected(Shape.CIRCLE) }
        arrow_shape.setOnClickListener { listener.shapeSelected(Shape.ARROW) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> listener.toggleDrawer(CARET_MARGIN * density)
        }
        return super.onTouchEvent(event)
    }

    /**
     * Sets the tool size for tool options
     *
     * @param percentage the size to set the tool
     */
    fun setToolSize(percentage: Int) {
        size_slider.progress = percentage
    }

    /**
     * Sets the drawer to show options based on the selected tool
     *
     * @param type the type of drawer format to display
     */
    fun setDrawerType(tool: Tool) {
        currentTool = tool
        color_label.visibility = View.VISIBLE
        color_picker.visibility = View.VISIBLE
        if (currentTool == Tool.PEN || currentTool == Tool.TEXT) {
            shape_button_holder.visibility = View.GONE
            shape_label.visibility = View.GONE
            size_slider.visibility = View.VISIBLE
            size_label.visibility = View.VISIBLE
        } else if (currentTool == Tool.SHAPE) {
            size_slider.visibility = View.GONE
            size_label.visibility = View.GONE
            shape_button_holder.visibility = View.VISIBLE
            shape_label.visibility = View.VISIBLE
        }
        tool_preview.updateTool(tool)
    }

    fun setDrawerType(shape: Shape) {
        setDrawerType(Tool.SHAPE)
        tool_preview.updateShape(shape)
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
        tool_preview.visibility = if (isVisible) VISIBLE else GONE
        color_label.visibility = if (isVisible) VISIBLE else GONE
        color_picker.visibility = if (isVisible) VISIBLE else GONE
        if (currentTool == Tool.PEN || currentTool == Tool.TEXT) {
            size_label.visibility = if (isVisible) VISIBLE else GONE
            size_slider.visibility = if (isVisible) VISIBLE else GONE
        } else {
            shape_label.visibility = if (isVisible) VISIBLE else GONE
            shape_button_holder.visibility = if (isVisible) VISIBLE else GONE
        }
    }
}