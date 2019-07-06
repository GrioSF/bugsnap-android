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

    // Graphics
    private var brush = Paint()
    private var selectionBrush = Paint()
    private var textBrush = Paint()
    private var annotations = arrayListOf<BugAnnotation>()
    lateinit var originalScreenshot: Bitmap

    // State
    var paintColor = "#000000"
    var strokeWidth = 10f
    var currentTool = Tool.NONE
    private lateinit var lastClick: KeyEvent
    private var selectedAnnotation: BugAnnotation? = null
    var annotationIsSelected = false

    private val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private var textInput = StringBuilder()
    private var textToolState = NONE

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

        selectionBrush.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.BLACK
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }
    }

    fun removeSelectedAnnotation() {
        annotations.remove(selectedAnnotation)
        invalidate()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!::lastClick.isInitialized || event !== lastClick) {
            if (textToolState == TYPING) {
                if (keyCode == KeyEvent.KEYCODE_DEL && textInput.isNotEmpty()) {
                    textInput.delete(textInput.length - 1, textInput.length)
                    (annotations.last() as TextAnnotation).text = textInput.toString()
                    invalidate()
                } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    textToolState = NONE
                    textInput.clear()
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    invalidate()
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    textToolState = NONE
                    textInput.clear()
                    invalidate()
                } else {
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (currentTool) {
            Tool.PEN -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        annotationIsSelected = true
                        startPenDrawing(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        annotationIsSelected = false
                        recordPenMovement(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!annotationIsSelected) {
                            stopPenRecording()
                        }
                        else {
                            annotations.removeAt(annotations.size - 1)

                            selectedAnnotation = null
                            annotations.forEach { annotation ->
                                if (annotation.wasSelected(event.x, event.y)) {
                                    selectedAnnotation = annotation
                                }
                            }
                            if (selectedAnnotation == null) annotationIsSelected = false
                            invalidate()
                        }
                    }
                }
            }
            Tool.TEXT -> {
                if (textToolState == NONE) {
                    textToolState = INITIALIZING
                    initializeTextAnnotation(event!!.x, event.y)
                }
            }
            Tool.SHAPE -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> startShapeDrawing(event.x, event.y)
                    MotionEvent.ACTION_MOVE -> recordShapeMovement(event.x, event.y)
                }
            }
            Tool.NONE -> {
                // no op
            }
        }
        invalidate()
        return true
    }

    private fun startShapeDrawing(x: Float, y: Float) {
        listener.beginDrawing()
        val newAnnotation = ShapeAnnotation(paintColor, strokeWidth, x, y, x, y)
        annotations.add(newAnnotation)
    }

    private fun recordShapeMovement(x: Float, y: Float) {
        (annotations.last() as ShapeAnnotation).endX = x
        (annotations.last() as ShapeAnnotation).endY = y
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
                    brush.style = Paint.Style.STROKE
                    canvas?.drawPath(annotation.drawnPath, brush)
                    if (annotationIsSelected && annotation == selectedAnnotation) {
                        canvas?.drawRect(annotation.getRect(), selectionBrush)
                    }
                }

                is TextAnnotation -> {
                    textBrush.color = Color.WHITE
                    textBrush.textSize = annotation.size
                    brush.color = Color.parseColor(annotation.color)
                    brush.strokeWidth = 1f
                    brush.style = Paint.Style.FILL_AND_STROKE
                    canvas?.drawRect(
                        (annotation.x - textBrush.measureText(annotation.text) / 2) - annotation.size / 2,
                        (annotation.y - annotation.size / 2) - annotation.size,
                        (annotation.x + textBrush.measureText(annotation.text) / 2) + annotation.size / 2,
                        (annotation.y),
                        brush
                    )
                    canvas?.drawText(
                        annotation.text,
                        (annotation.x - textBrush.measureText(annotation.text) / 2),
                        (annotation.y - annotation.size / 2),
                        textBrush
                    )
                }

                is ShapeAnnotation -> {
                    brush.color = Color.parseColor(annotation.color)
                    brush.strokeWidth = annotation.size
                    brush.style = Paint.Style.STROKE
                    canvas?.drawRect(
                        min(annotation.startX, annotation.endX),
                        min(annotation.startY, annotation.endY),
                        max(annotation.endX, annotation.startX),
                        max(annotation.endY, annotation.startY), brush
                    )
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
        val newAnnotation = PenAnnotation(paintColor, strokeWidth, Path(), x, y, x, y, x, y, x, y)
        newAnnotation.drawnPath.moveTo(x, y)
        newAnnotation.startX = x
        newAnnotation.startY = y
        newAnnotation.endX = x
        newAnnotation.endY = y
        annotations.add(newAnnotation)
    }

    /**
     * Record movement of touch and update path
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
        (annotations.last() as PenAnnotation).endX = x
        (annotations.last() as PenAnnotation).endY = y
        if (x < annotation.left) (annotations.last() as PenAnnotation).left = x
        if (y < annotation.top) (annotations.last() as PenAnnotation).top = y
        if (x > annotation.right) (annotations.last() as PenAnnotation).right = x
        if (y > annotation.bottom) (annotations.last() as PenAnnotation).bottom = y
    }

    /**
     * User stopped touching screen. Stop recording.
     */
    private fun stopPenRecording() {
        val annotation = (annotations.last() as PenAnnotation)

        (annotations.last() as PenAnnotation).drawnPath.lineTo(annotation.endX, annotation.endY)

        // If user taps screen, create a dot
        if (annotation.startX == annotation.endX && annotation.startY == annotation.endY) {
            (annotations.last() as PenAnnotation).drawnPath.addCircle(
                annotation.endX,
                annotation.endY,
                4f,
                Path.Direction.CW
            )
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