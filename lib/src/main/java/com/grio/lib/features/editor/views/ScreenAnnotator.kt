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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.grio.lib.core.extension.screenshot
import com.grio.lib.features.editor.BugAnnotation
import com.grio.lib.features.editor.PenAnnotation
import com.grio.lib.features.editor.TextAnnotation
import android.text.SpannableStringBuilder
import android.view.inputmethod.BaseInputConnection
import com.grio.lib.features.editor.views.TextToolState.*


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

    // State
    var paintColor = "#000000"
    var strokeWidth = 10f
    private var xStart = 0f
    private var yStart = 0f
    private var xCurrent = 0f
    private var yCurrent = 0f
    var currentTool = Tool.NONE

    private val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    var textInput = SpannableStringBuilder()
    var textToolState = NONE

    lateinit var listener: Listener

    init {
        isFocusableInTouchMode = true;
        isFocusable = true;

        brush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.RED
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
        }

        textBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            color = Color.RED
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event!!.action == KeyEvent.ACTION_UP && textToolState == TYPING) {
            if (keyCode == KeyEvent.KEYCODE_DEL && textInput.isNotEmpty()) {
                textInput.delete(textInput.length - 1, textInput.length)
                (annotations.last() as TextAnnotation).text = textInput.toString()
                invalidate()
                return true
            } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                textToolState = NONE
                textInput.clear()
                imm.hideSoftInputFromWindow(windowToken, 0)
                invalidate()
                return true
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                textToolState = NONE
                textInput.clear()
                invalidate()
                return true
            } else { // text character
                textInput.append(event.unicodeChar.toChar())
                (annotations.last() as TextAnnotation).text = textInput.toString()
                invalidate()
                return true
            }
        }
        return false
    }

    // Handle hardware back press
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
            textToolState = NONE
            textInput.clear()
            invalidate()
            return super.onKeyPreIme(keyCode, event)
        }
        return super.onKeyPreIme(keyCode, event)
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
                when (textToolState) {
                    INITIALIZING -> {
                        if (event?.action == MotionEvent.ACTION_UP) {

                        }
                    }
                    TYPING -> {
                        if (event?.action == MotionEvent.ACTION_UP) {

                        }
                    }
                    NONE -> {
                        textToolState = INITIALIZING
                        initializeTextAnnotation(event!!.x, event.y)
                    }
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
                    textBrush.color = Color.parseColor(annotation.color)
                    textBrush.textSize = annotation.size
                    canvas?.drawText(annotation.text, (annotation.x - textBrush.measureText(annotation.text)/2), (annotation.y - annotation.size/2), textBrush)
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

    private fun initializeTextAnnotation(x: Float, y: Float) {
        listener.beginDrawing()
        val newAnnotation = TextAnnotation(paintColor, strokeWidth, "", x, y)

        annotations.add(newAnnotation)
        // show the keyboard so we can enter text
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        textToolState = TYPING
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        val fic = BaseInputConnection(this, false)
        outAttrs?.inputType = InputType.TYPE_NULL
        return fic
    }
}

enum class Tool {
    PEN, TEXT, SHAPE, NONE
}

enum class TextToolState {
    INITIALIZING, TYPING, NONE
}