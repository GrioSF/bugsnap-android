package com.grio.lib.features.editor.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import android.view.inputmethod.BaseInputConnection
import com.grio.lib.features.editor.ShapeAnnotation
import com.grio.lib.features.editor.views.TextToolState.*
import java.lang.StringBuilder
import kotlin.math.max
import kotlin.math.min


/**
 * View that allows for simple drawing onto the screen
 */
class ScreenAnnotator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Graphics
    private var brush = Paint()
    private var selectionBrush = Paint()
    private var textBrush = Paint()
    private var textBackgroundBrush = Paint()
    var paintColor = "#000000"
    var strokeWidth = 10f

    // State
    private var textInput = StringBuilder()
    private var textToolState = NONE
    private var annotations = arrayListOf<BugAnnotation>()
    private var selectedAnnotation: BugAnnotation? = null
    var attemptToSelectAnnotation = false
    var currentTool = Tool.NONE

    lateinit var listener: Listener
    lateinit var originalScreenshot: Bitmap
    lateinit var lastClick: KeyEvent

    init {
        isFocusableInTouchMode = true
        isFocusable = true

        brush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
        }

        textBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            color = Color.WHITE
        }

        textBackgroundBrush.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }

        selectionBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.BLACK
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Draw screenshot to canvas
        if (::originalScreenshot.isInitialized) {
            canvas?.drawBitmap(originalScreenshot, 0f, 0f, brush)
        }
        // Iterate through all annotations and draw them to the canvas
        for (annotation in annotations) {
            brush.color = Color.parseColor(annotation.color)
            brush.strokeWidth = annotation.size

            when (annotation) {
                is PenAnnotation -> {
                    canvas?.drawPath(annotation.drawnPath, brush)
                }
                is TextAnnotation -> {
                    textBrush.textSize = annotation.size
                    canvas?.drawRect(annotation.getRect(), textBackgroundBrush)
                    canvas?.drawText(
                        annotation.text,
                        annotation.x - textBrush.measureText(annotation.text) / 2,
                        annotation.y - annotation.size / 2,
                        textBrush
                    )
                }
                is ShapeAnnotation ->
                    canvas?.drawRect(
                        min(annotation.startX, annotation.endX),
                        min(annotation.startY, annotation.endY),
                        max(annotation.endX, annotation.startX),
                        max(annotation.endY, annotation.startY), brush
                    )
            }
            // If annotation was selected, draw rectangle around the annotation
            if (attemptToSelectAnnotation && annotation == selectedAnnotation) {
                canvas?.drawRect(annotation.getRect(), selectionBrush)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (currentTool) {
            Tool.PEN -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        attemptToSelectAnnotation = true
                        startPenDrawing(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        attemptToSelectAnnotation = false
                        recordPenMovement(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (attemptToSelectAnnotation && annotationWasSelected(event.x, event.y)) {
                            annotations.removeAt(annotations.size - 1)
                        } else {
                            stopPenRecording()
                        }
                    }
                }
            }
            Tool.TEXT -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (!annotationWasSelected(event.x, event.y) && textToolState == NONE) {
                        textToolState = INITIALIZING
                        initializeTextAnnotation(event.x, event.y)
                    } else {
                        attemptToSelectAnnotation = true
                    }
                    invalidate()
                }
            }
            Tool.SHAPE -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        attemptToSelectAnnotation = true
                        startShapeDrawing(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        attemptToSelectAnnotation = false
                        recordShapeMovement(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (attemptToSelectAnnotation) {
                            annotations.removeAt(annotations.size - 1)
                            annotationWasSelected(event.x, event.y)
                        }
                    }
                }
            }
            Tool.NONE -> {
                // TODO: Used for dragging
            }
        }
        invalidate()
        return true
    }

    // Check for key events when typing into text box
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if ((!::lastClick.isInitialized || event !== lastClick) && textToolState == TYPING) {
            when {
                keyCode == KeyEvent.KEYCODE_DEL && textInput.isNotEmpty() -> {
                    textInput.delete(textInput.length - 1, textInput.length)
                    (annotations.last() as TextAnnotation).text = textInput.toString()
                    invalidate()
                }
                keyCode == KeyEvent.KEYCODE_ENTER -> {
                    textToolState = NONE
                    textInput.clear()
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    invalidate()
                }
                keyCode == KeyEvent.KEYCODE_BACK -> {
                    textToolState = NONE
                    textInput.clear()
                    invalidate()
                }
                else -> {
                    if (event != null) {
                        textInput.append(event.unicodeChar.toChar())
                    }
                    (annotations.last() as TextAnnotation).text = textInput.toString()
                    invalidate()
                }
            }
        }
        if (event != null) {
            lastClick = event
        }
        return true
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

    // Setup view to receive keyboard key events
    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        val fic = BaseInputConnection(this, false)
        outAttrs?.inputType = InputType.TYPE_NULL
        return fic
    }

    /**
     * Record when user touches the screen with the Pen tool
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun startPenDrawing(x: Float, y: Float) {
        listener.beginDrawing()
        val newAnnotation = PenAnnotation(paintColor, strokeWidth, x, y)
        newAnnotation.drawnPath.moveTo(x, y)
        annotations.add(newAnnotation)
    }

    /**
     * Record movement of Pen tool and update annotation model
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun recordPenMovement(x: Float, y: Float) {
        val annotation = (annotations.last() as PenAnnotation)
        (annotations.last() as PenAnnotation).drawnPath.quadTo(
            annotation.endX,
            annotation.endY,
            (x + annotation.endX) / 2,
            (y + annotation.endY) / 2
        )
        (annotations.last() as PenAnnotation).updateBounds(x, y)
    }

    /**
     * User stopped using Pen tool. Stop recording.
     */
    private fun stopPenRecording() {
        val annotation = (annotations.last() as PenAnnotation)
        (annotations.last() as PenAnnotation).drawnPath.lineTo(annotation.endX, annotation.endY)
    }

    /**
     * Create text annotation and toggle keyboard input
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun initializeTextAnnotation(x: Float, y: Float) {
        val newAnnotation = TextAnnotation(paintColor, strokeWidth, x, y)
        listener.beginDrawing()
        annotations.add(newAnnotation)
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        textToolState = TYPING
    }

    /**
     * Start drawing a shape onto the screen
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun startShapeDrawing(x: Float, y: Float) {
        val newAnnotation = ShapeAnnotation(paintColor, strokeWidth, x, y, x, y)
        listener.beginDrawing()
        annotations.add(newAnnotation)
    }

    /**
     * Record user making shape larger and smaller
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     */
    private fun recordShapeMovement(x: Float, y: Float) {
        (annotations.last() as ShapeAnnotation).endX = x
        (annotations.last() as ShapeAnnotation).endY = y
    }

    /**
     * Check if any annotations were selected
     *
     * @param x the x coordinate of the touch
     * @param y the y coordinate of the touch
     *
     * @return true if annotation was selected, false otherwise
     */
    private fun annotationWasSelected(x: Float, y: Float): Boolean {
        selectedAnnotation = null
        annotations.forEach { annotation ->
            if (annotation.wasSelected(x, y)) {
                selectedAnnotation = annotation
                return true
            }
        }
        if (selectedAnnotation == null) attemptToSelectAnnotation = false
        invalidate()
        return false
    }

    /**
     * Remove the currently selected annotation
     */
    fun removeSelectedAnnotation() {
        annotations.remove(selectedAnnotation)
        invalidate()
    }

    /**
     * Removes the last line drawn
     */
    fun undo() {
        if (annotations.isNotEmpty())
            annotations.remove(annotations.last())
        invalidate()
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
}

enum class Tool {
    PEN, TEXT, SHAPE, NONE
}

enum class TextToolState {
    INITIALIZING, TYPING, NONE
}