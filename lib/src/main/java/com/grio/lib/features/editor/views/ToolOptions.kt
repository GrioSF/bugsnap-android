package com.grio.lib.features.editor.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.view.animation.*


class ToolOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val arrow = context.getDrawable(com.grio.lib.R.drawable.ic_keyboard_arrow_left_white_24dp)
    private val density = context.resources.displayMetrics.density.toInt()
    private var drawerState = DrawerState.CLOSED

    private var palette = arrayListOf<Swatch>()

    private var originalWidth = 0
    private var arrowDimen = 12 * density

    init {
        palette.add(Swatch("#000000", 0f, 0f))
        palette.add(Swatch("#FFFFFF", 0f, 0f))
        palette.add(Swatch("#0536FF", 0f, 0f))
        palette.add(Swatch("#3EB327", 0f, 0f))
        palette.add(Swatch("#FFF404", 0f, 0f))
        palette.add(Swatch("#FF6612", 0f, 0f))
        palette.add(Swatch("#EA0000", 0f, 0f))
        palette.add(Swatch("#9F00FF", 0f, 0f))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (originalWidth <= 0) {
            originalWidth = width
            arrowDimen = width/3
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        arrow?.setBounds(arrowDimen,
            height/2-arrowDimen,
            3*arrowDimen,
            height/2+arrowDimen)
        canvas?.let {
            arrow?.draw(it)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (drawerState == DrawerState.CLOSED) {
                    openDrawer()
                } else if (drawerState == DrawerState.OPENED) {
                    closeDrawer()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun openDrawer() {
        drawerState = DrawerState.ANIMATING
        val screenWidth = context.resources.displayMetrics.widthPixels
        val marginEnd = 16 * density
        val widthAnimator = ValueAnimator.ofInt(originalWidth, screenWidth - marginEnd)
        widthAnimator.duration = 500
        widthAnimator.interpolator = DecelerateInterpolator(3f)
        widthAnimator.addUpdateListener { valueAnimator ->
            layoutParams.width = valueAnimator.animatedValue as Int
            requestLayout()
            invalidate()
        }
        widthAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                drawerState = DrawerState.OPENED
                invalidate()
            }
        })
        widthAnimator.start()
    }

    private fun closeDrawer() {
        drawerState = DrawerState.ANIMATING
        val widthAnimator = ValueAnimator.ofInt(width, originalWidth)
        widthAnimator.duration = 500
        widthAnimator.interpolator = DecelerateInterpolator(3f)
        widthAnimator.addUpdateListener { valueAnimator ->
            layoutParams.width = valueAnimator.animatedValue as Int
            requestLayout()
            invalidate()
        }
        widthAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                drawerState = DrawerState.CLOSED
                invalidate()
            }
        })
        widthAnimator.start()
    }
}

enum class DrawerState {
    OPENED, CLOSED, ANIMATING
}

data class Swatch(
    val color: String,
    var radius: Float = 0f,
    var position: Float = 0f
)