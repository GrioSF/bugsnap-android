package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import com.grio.lib.R

class ToolPreview @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    var previewType = Tool.PEN
    var shape = Shape.RECTANGLE
    var size = 0f
    var textWidth: Float = 0f
    private var paint = Paint()
    private var textBrush = Paint()
    private var textBackgroundBrush = Paint()
    private var rectangleIcon = context.getDrawable(R.drawable.rectangle_shape_tool_icon)
    private var circleIcon = context.getDrawable(R.drawable.circle_shape_tool_icon)
    private var arrowIcon = context.getDrawable(R.drawable.arrow_shape_tool_icon)

    init {
        paint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        textBrush.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = Color.WHITE
        }

        textBackgroundBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }

        rectangleIcon!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        circleIcon!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        arrowIcon!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when (previewType) {
            Tool.PEN -> {
                canvas?.drawCircle(width/2f, height/2f, size/2, paint)
            }
            Tool.TEXT -> {
                canvas?.drawRect(
                    width/2f - size,
                    height/2f - size,
                    width/2f + size,
                    height/2f + size,
                    textBackgroundBrush)

                canvas?.drawText(
                    "A",
                    width/2f - textWidth / 2,
                    height/2f + size / 4,
                    textBrush
                )
            }
            Tool.SHAPE -> {
                when (shape) {
                    Shape.RECTANGLE -> {
                        rectangleIcon!!.setBounds(10, 10, width - 10, height - 10)
                        rectangleIcon!!.draw(canvas!!)
                    }
                    Shape.CIRCLE -> {
                        circleIcon!!.setBounds(10, 10, width - 10, height - 10)
                        circleIcon!!.draw(canvas!!)
                    }
                    Shape.ARROW -> {
                        arrowIcon!!.setBounds(10, 10, width - 10, height - 10)
                        arrowIcon!!.draw(canvas!!)
                    }
                }
            }
            Tool.NONE -> {
                // no op
            }
        }
    }

    fun updateColor(color: String) {
        paint.color = Color.parseColor(color)
        textBackgroundBrush.color = Color.parseColor(color)
        rectangleIcon!!.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP)
        circleIcon!!.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP)
        arrowIcon!!.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP)
        invalidate()
    }

    fun updateSize(size: Float) {
        this.size = size
        textBrush.textSize = size
        textWidth = textBrush.measureText("A")
        invalidate()
    }

    fun updateTool(tool: Tool) {
        this.previewType = tool
        invalidate()
    }

    fun updateShape(selectedShape: Shape) {
        this.shape = selectedShape
        invalidate()
    }
}