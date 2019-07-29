package com.grio.lib.features.recorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*

import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import androidx.annotation.Nullable
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.grio.lib.R


class RecordingButton
@JvmOverloads
constructor(context: Context,
            attrs: AttributeSet? = null) : FloatingActionButton(context, attrs) {

    private var windowManager: WindowManager
    private val inAnimator = AnimatorSet()
    private val outAnimator = AnimatorSet()

    init {

        val backgroundColor = Color.BLACK
        ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(backgroundColor))

        scaleType = ScaleType.CENTER_INSIDE
        setImageResource(R.drawable.ic_stop)
        setBackgroundResource(R.drawable.bg_circle_ripple)

        val inXAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
        val inYAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, 1f)
        val outXAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 0f)
        val outYAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 0f)
        inAnimator.playTogether(inXAnimator, inYAnimator)
        outAnimator.playTogether(outXAnimator, outYAnimator)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
        setMeasuredDimension(dimen, dimen)
    }

   override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeGlobalOnLayoutListener(this)
                val dm = resources.displayMetrics
                val params = layoutParams as WindowManager.LayoutParams
                params.x = dm.widthPixels / 2 - measuredWidth / 2
                params.y = dm.heightPixels / 2 - measuredHeight / 2
                windowManager.updateViewLayout(this@RecordingButton, params)
                inAnimator.start()
            }
        })
    }

    fun hide(@Nullable callback: VisibilityCallback?) {
        isEnabled = false
        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                outAnimator.removeListener(this)
                callback?.onViewHidden()
            }
        })
        outAnimator.startDelay = 300
        outAnimator.start()
    }

    interface VisibilityCallback {
        fun onViewHidden()
    }
}
