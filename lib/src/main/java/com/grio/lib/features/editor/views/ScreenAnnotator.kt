package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.children
import com.grio.lib.core.extension.screenshot
import com.grio.lib.features.editor.BugAnnotation
import com.grio.lib.features.editor.PenAnnotation
import com.grio.lib.features.editor.TextAnnotation
import androidx.core.content.ContextCompat.getSystemService




/**
 * View that allows for simple drawing onto the screen
 */
class ScreenAnnotator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Graphics
    private var brush = Paint()
    private var textBrush = Paint()
    private var annotations = arrayListOf<BugAnnotation>()
    lateinit var originalScreenshot: Bitmap

    private var key = ""
    // State
    var paintColor = "#000000"
    var strokeWidth = 10f
    private var xStart = 0f
    private var yStart = 0f
    private var xCurrent = 0f
    private var yCurrent = 0f
    var currentTool = Tool.NONE

    lateinit var listener: Listener

    init {
        brush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.RED
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
        }

        textBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.RED
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (currentTool) {
            Tool.PEN -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> startPenDrawing(event.x, event.y)
                    MotionEvent.ACTION_MOVE -> recordPenMovement(event.x, event.y)
                    MotionEvent.ACTION_UP -> stopPenRecording()
                }
            }
            Tool.TEXT -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> return true
//                    MotionEvent.ACTION_MOVE -> recordPenMovement(event.x, event.y)
                    MotionEvent.ACTION_UP -> startTextRecording(event.x, event.y)
                }
            }
            Tool.SHAPE -> {

            }
            Tool.NONE -> {
                // no op
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (::originalScreenshot.isInitialized) {
            canvas?.drawBitmap(originalScreenshot, 0f, 0f, brush)
        }
        for (annotation in annotations) {
            when (annotation) {
                is PenAnnotation -> {
                    brush.color = Color.parseColor(annotation.color)
                    brush.strokeWidth = annotation.size
                    canvas?.drawPath(annotation.drawnPath, brush)
                }

                is TextAnnotation -> {
                    annotation.text.measure(width, height)
                    annotation.text.layout(annotation.x.toInt() - annotation.text.children.first().width/2,
                        annotation.y.toInt() - annotation.text.children.first().height/2,
                        annotation.x.toInt() + annotation.text.children.first().width/2,
                        annotation.y.toInt() + annotation.text.children.first().height/2)
                    (annotation.text.children.first() as Button).setText(key)
                    canvas?.save()
                    canvas?.translate(annotation.x - annotation.text.children.first().width/2,
                        annotation.y - annotation.text.children.first().height/2)
                    annotation.text.draw(canvas)
                    canvas?.restore()
                }
            }
        }
    }

    /**
     * Retrieve the current view of the annotations made
     */
    fun getAnnotatedScreenshot(): Bitmap {
        return this.screenshot()
    }

    /**
     * Listens for Annotation Events
     */
    interface Listener {
        // Fired when user begins to draw on screen
        fun beginDrawing()
    }

    /**
     * Removes the last line drawn
     */
    fun undo() {
        if (annotations.isNotEmpty()) {
            annotations.remove(annotations.last())
        }
        invalidate()
    }

    // TODO: UPDATE ALL THE DOCS TO HANDLE MULTIPLE TOOLS

    /**
     * Record when user touches the screen
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun startPenDrawing(x: Float, y: Float) {
        listener.beginDrawing()
        val newAnnotation = PenAnnotation(paintColor, strokeWidth, Path())
        newAnnotation.drawnPath.moveTo(x, y)
        annotations.add(newAnnotation)
        xStart = x
        yStart = y
        xCurrent = x
        yCurrent = y
    }

    /**
     * Record movement of touch and update path
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun recordPenMovement(x: Float, y: Float) {
        (annotations.last() as PenAnnotation).drawnPath.quadTo(xCurrent, yCurrent, (x + xCurrent) / 2, (y + yCurrent) / 2)
        xCurrent = x
        yCurrent = y
    }

    /**
     * User stopped touching screen. Stop recording.
     */
    private fun stopPenRecording() {
        (annotations.last() as PenAnnotation).drawnPath.lineTo(xCurrent, yCurrent)

        // If user taps screen, create a dot
        if (xStart == xCurrent && yStart == yCurrent) {
            (annotations.last() as PenAnnotation).drawnPath.addCircle(xCurrent, yCurrent, 4f, Path.Direction.CW)
        }
    }

    private fun startTextRecording(x: Float, y: Float) {
        listener.beginDrawing()
        val newAnnotation = TextAnnotation(paintColor, strokeWidth, LinearLayout(context), x, y)
        val editText = Button(context)
        editText.setText("Ayo for Yayo")
        editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        newAnnotation.text.addView(editText)
        annotations.add(newAnnotation)

        editText.isFocusableInTouchMode = true
        editText.inputType = InputType.TYPE_CLASS_TEXT

        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        im!!.showSoftInput(editText, 0)

        editText.requestFocus()
        currentTool = Tool.NONE
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val keyaction = event?.getAction()

        if (keyaction == KeyEvent.ACTION_DOWN) {
            val keyunicode = event.getUnicodeChar(event.getMetaState())
            val character = keyunicode.toChar()
            key += character
            println("DEBUG MESSAGE KEY=$character")
        }
        // you might add if (delete)
        // https://stackoverflow.com/questions/7438612/how-to-remove-the-last-character-from-a-string
        // method to delete last character
        invalidate()
        return super.dispatchKeyEvent(event)
    }
}

enum class Tool {
    PEN, TEXT, SHAPE, NONE
}