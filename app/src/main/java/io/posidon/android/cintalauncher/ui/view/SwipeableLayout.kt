package io.posidon.android.cintalauncher.ui.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.SpringInterpolator
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.onEnd
import kotlin.math.abs

class SwipeableLayout(
    val frontView: View,
    var onSwipeAway: ((SwipeableLayout) -> Unit)? = null
) : CardView(frontView.context) {

    val closeIcon = ImageView(context).apply {
        setImageResource(R.drawable.ic_cross)
    }

    val backView = FrameLayout(context).apply {
        addView(closeIcon)
        clipBounds = Rect(0, 0, 0, 0)
        visibility = GONE
    }

    fun setSwipeColor(color: Int) = backView.setBackgroundColor(color)
    fun setIconColor(value: Int) {
        closeIcon.imageTintList = ColorStateList.valueOf(value)
    }

    var isSwipeable = true
    
    var cornerRadiusCompensation = 0f

    override fun setOnClickListener(l: OnClickListener?) = frontView.setOnClickListener(l)

    init {
        setCardBackgroundColor(0)
        cardElevation = 0f
        radius = 0f
        addView(backView)
        addView(frontView)
        layoutParams = frontView.layoutParams
        closeIcon.run {
            layoutParams.width = dp(32).toInt()
            layoutParams.height = dp(32).toInt()
        }
    }

    fun reset() {
        currentAnimator?.cancel()
        frontView.translationX = 0f
        backView.clipBounds = Rect(0, 0, 0, 0)
        backView.visibility = GONE
    }

    private var currentAnimator: ValueAnimator? = null
    private var state: State? = null

    private class State(
        var initX: Float = 0f,
        var initY: Float = 0f,
    ) {
        var xOffset = 0f
    }

    private val onAnimEndListener = object : Animator.AnimatorListener {
        private var isCanceled = false
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {
            isCanceled = true
        }
        override fun onAnimationStart(animation: Animator?) {
            isCanceled = false
        }
        override fun onAnimationEnd(animation: Animator?) {
            state = null
            currentAnimator = null
            if (isCanceled) return
            if (isSwipeable) {
                onSwipeAway?.invoke(this@SwipeableLayout)
            }
            else bounceBack()
        }
    }

    private fun bounceBack() {
        currentAnimator?.cancel()
        currentAnimator = ValueAnimator.ofFloat(state!!.xOffset, 0f).apply {
            addUpdateListener {
                val f = it.animatedValue as Float
                backView.clipBounds = when {
                    f > 0 -> Rect(0, 0, f.toInt(), measuredHeight)
                    f < 0 -> Rect(measuredWidth + f.toInt(), 0, measuredWidth, measuredHeight)
                    else -> Rect(0, 0, 0, 0)
                }
                frontView.translationX = f
                state!!.xOffset = f
            }
            interpolator = SpringInterpolator()
            duration = 420L
            onEnd {
                backView.clipBounds = Rect(0, 0, 0, 0)
                backView.visibility = GONE
                currentAnimator = null
                state = null
            }
            start()
        }
    }

    private fun sashayAway(direction: Int) {
        currentAnimator?.cancel()
        currentAnimator = ValueAnimator.ofFloat(frontView.translationX, measuredWidth * direction.toFloat()).apply {
            addUpdateListener {
                val f = it.animatedValue as Float
                if (direction == 1)
                    backView.clipBounds = Rect(0, 0, f.toInt(), measuredHeight)
                else
                    backView.clipBounds = Rect(f.toInt() + measuredWidth, 0, measuredWidth, measuredHeight)
                frontView.translationX = f
                state!!.xOffset = f
            }
            interpolator = DecelerateInterpolator()
            duration = 110L
            addListener(onAnimEndListener)
            start()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_CANCEL -> bounceBack()
            MotionEvent.ACTION_MOVE -> {
                state!!.xOffset = ev.x - state!!.initX
                parent.requestDisallowInterceptTouchEvent(true)
                currentAnimator?.cancel()
                frontView.translationX = state!!.xOffset
                backView.clipBounds =
                    if (state!!.xOffset > 0) {
                        closeIcon.translationX = dp(18)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(0, 0, state!!.xOffset.toInt() + cornerRadiusCompensation.toInt(), measuredHeight)
                    }
                    else {
                        closeIcon.translationX = measuredWidth - dp(50)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(measuredWidth + state!!.xOffset.toInt() - cornerRadiusCompensation.toInt(), 0, measuredWidth, measuredHeight)
                    }
                backView.visibility = VISIBLE
                return true
            }
            MotionEvent.ACTION_UP -> {
                when {
                    state!!.xOffset > measuredWidth/7*3 ||
                            state!!.xOffset > dp(64) && ev.eventTime - ev.downTime < 160 -> {
                        if (isSwipeable) sashayAway(1)
                        else bounceBack()
                    }
                    state!!.xOffset < -measuredWidth/7*3 ||
                    state!!.xOffset < -dp((64)) && ev.eventTime - ev.downTime < 160 -> {
                        if (isSwipeable) sashayAway(-1)
                        else bounceBack()
                    }
                    else -> bounceBack()
                }
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent) = when (ev.action) {
        MotionEvent.ACTION_CANCEL -> {
            bounceBack()
            super.onInterceptTouchEvent(ev)
        }
        MotionEvent.ACTION_MOVE -> {
            state!!.xOffset = ev.x - state!!.initX
            val absYOffset = abs(ev.y - state!!.initY)
            val absXOffset = abs(state!!.xOffset)
            if (abs(absXOffset - absYOffset) > context.dp(2) && absXOffset > absYOffset && !(frontView is ViewGroup && checkForHorizontalScroll(ev, frontView))) {
                true
            } else {
                super.onInterceptTouchEvent(ev)
            }
        }
        MotionEvent.ACTION_UP -> if (abs(state!!.xOffset) < context.dp(12) || frontView is ViewGroup && checkForHorizontalScroll(ev, frontView)) {
            super.onInterceptTouchEvent(ev)
        } else true
        else -> {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val o = (state?.xOffset ?: 0f)
                currentAnimator?.cancel()
                state = State(ev.x - o, ev.y)
                backView.visibility = VISIBLE
            }
            super.onInterceptTouchEvent(ev)
        }
    }

    companion object {
        private tailrec fun checkForHorizontalScroll(ev: MotionEvent, viewGroup: ViewGroup): Boolean {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                val location = IntArray(2)
                child.getLocationOnScreen(location)
                if (child is ViewGroup &&
                    location[0] <= ev.rawX && location[0] + child.measuredWidth >= ev.rawX &&
                    location[1] <= ev.rawY && location[1] + child.measuredHeight >= ev.rawY) {
                    val r = (child.canScrollHorizontally(1) || child.canScrollHorizontally(-1)) ||
                            (child is RecyclerView && child.layoutManager?.canScrollHorizontally() == true) ||
                            child is SwipeableLayout
                    if (r) return true
                    return checkForHorizontalScroll(ev, child)
                }
            }
            return false
        }
    }
}