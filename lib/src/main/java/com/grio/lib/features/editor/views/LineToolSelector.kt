package com.grio.lib.features.editor.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.grio.lib.R

const val EXPAND_COLLAPSE_DURATION = 200L
const val SWATCH_POP_DURATION = 400L
const val SWATCH_RADIUS_FRACTION = 0.25f
const val SWATCH_ANIMATION_OFFSET = 50L

/**
 * Color picker and tool selection for drawing lines onto the screen
 */
class LineToolSelector @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    // Graphics
    private var pencilIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.pencil_icon)!!
    private var swatchPaint = Paint()
    private var palette = arrayListOf<Swatch>()

    // State
    private var viewState = LineToolState.COLLAPSED
    private var showColors = false
    private var originalHeight = 0f
    private var colorDistance = 0f
    private var selectedIndex = 0

    private lateinit var listener: Listener

    init {
        elevation = 8f

        swatchPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4f
        }

        palette.add(Swatch("#EF3D59"))
        palette.add(Swatch("#E17A47"))
        palette.add(Swatch("#EFC958"))
        palette.add(Swatch("#4AB19D"))
        palette.add(Swatch("#344E5C"))

        pencilIcon.setColorFilter(Color.parseColor(palette[0].color), PorterDuff.Mode.SRC_ATOP)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            pencilIcon.draw(it)
        }
        if (showColors) palette.forEach { swatch ->
            swatchPaint.color = Color.parseColor(swatch.color)
            canvas?.drawCircle(width.toFloat() / 2, swatch.position, swatch.radius, swatchPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (viewState == LineToolState.EXPANDED) {
                    val colorIndex = colorPicked(event.y)
                    if (colorIndex >= 0) selectColor(colorIndex)
                    collapse()
                } else expand()
            }
        }
        return true
    }

    /**
     * Listens for color selections
     */
    interface Listener {
        // Called when color is selected
        fun colorSelected(color: String)
    }

    /**
     * Sets listener for LineToolSelector
     *
     * @param listenerToSet listener to attach to this view
     */
    fun setColorListener(listenerToSet: Listener) {
        this.listener = listenerToSet
    }

    /**
     * onLayout used for tracking initial height of view and calculating radius
     * for turning card view into circle
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (originalHeight == 0f) {
            originalHeight = height.toFloat()
            radius = width.toFloat() / 2

            pencilIcon.setBounds(
                width / 4,
                width / 4,
                width - width / 4,
                width - width / 4
            )
        }
    }

    /**
     * Getter for currently selected paint color
     *
     * @return the color of the currently selected color
     */
    fun getPaintColor(): String {
        return palette[selectedIndex].color
    }

    /**
     * Animates color selection and updates state
     */
    private fun selectColor(position: Int) {
        val colorAnimator = ValueAnimator.ofInt(
            Color.parseColor(palette[selectedIndex].color),
            Color.parseColor(palette[position].color)
        )
        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.addUpdateListener {
            pencilIcon.setColorFilter(it.animatedValue as Int, PorterDuff.Mode.SRC_ATOP)
        }
        colorAnimator.start()
        selectedIndex = position
        listener.colorSelected(palette[selectedIndex].color)
    }

    /**
     * Finds index of clicked color
     *
     * @return index of chosen color
     */
    private fun colorPicked(yPos: Float): Int {
        return palette.indexOfFirst { yPos > it.position - radius && yPos < it.position + radius }
    }

    /**
     * Expands the LineToolSelector to show colors
     */
    private fun expand() {
        val heightAnimator = ValueAnimator.ofInt(originalHeight.toInt(), originalHeight.toInt() * (palette.size + 1))
        heightAnimator.duration = EXPAND_COLLAPSE_DURATION
        heightAnimator.interpolator = AccelerateDecelerateInterpolator()
        heightAnimator.addUpdateListener { valueAnimator ->
            layoutParams.height = valueAnimator.animatedValue as Int
            requestLayout()
            invalidate()
        }
        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                viewState = LineToolState.EXPANDED
                invalidate()
            }
        })
        heightAnimator.start()

        showColors = true
        colorDistance = originalHeight
        var colorPosition = colorDistance * 1.25f
        var animationOffset = 100L

        for (swatch in palette) {
            swatch.position = colorPosition
            colorPosition += colorDistance

            val colorAnimator = ValueAnimator.ofFloat(0f, width.toFloat() * SWATCH_RADIUS_FRACTION)
            colorAnimator.duration = SWATCH_POP_DURATION
            colorAnimator.interpolator = OvershootInterpolator()
            colorAnimator.startDelay = animationOffset
            animationOffset += SWATCH_ANIMATION_OFFSET
            colorAnimator.addUpdateListener { valueAnimator ->
                swatch.radius = valueAnimator.animatedValue as Float
                invalidate()
            }
            colorAnimator.start()
        }
    }

    /**
     * Collapses the LineToolSelector to hide colors
     */
    private fun collapse() {
        var animationOffset = 0L

        palette.reversed().withIndex().forEach { (index, swatch) ->
            val colorAnimator = ValueAnimator.ofFloat(swatch.radius, 0f)
            colorAnimator.duration = SWATCH_POP_DURATION
            colorAnimator.interpolator = DecelerateInterpolator()
            colorAnimator.startDelay = animationOffset
            animationOffset += SWATCH_ANIMATION_OFFSET
            colorAnimator.addUpdateListener { valueAnimator ->
                swatch.radius = valueAnimator.animatedValue as Float
                invalidate()
            }
            if (index == palette.size - 1) {
                colorAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        showColors = false
                    }
                })
            }
            colorAnimator.start()
        }

        val heightAnimator = ValueAnimator.ofInt(layoutParams.height, originalHeight.toInt())
        heightAnimator.duration = EXPAND_COLLAPSE_DURATION
        heightAnimator.startDelay = 150
        heightAnimator.interpolator = AccelerateDecelerateInterpolator()
        heightAnimator.addUpdateListener { valueAnimator ->
            layoutParams.height = valueAnimator.animatedValue as Int
            requestLayout()
            invalidate()
        }
        heightAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                viewState = LineToolState.COLLAPSED
                requestLayout()
                invalidate()
            }
        })
        heightAnimator.start()
    }
}

enum class LineToolState {
    EXPANDED, COLLAPSED
}

data class Swatch(
    val color: String,
    var radius: Float = 0f,
    var position: Float = 0f
)