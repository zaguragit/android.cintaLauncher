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

    var isSwipeable = true
    
    var cornerRadiusCompensation = 0f

    override fun setOnClickListener(l: OnClickListener?) = frontView.setOnClickListener(l)

    init {
        setCardBackgroundColor(0)
        cardElevation = 0f
        radius = 0f
        preventCornerOverlap = true
        addView(backView)
        addView(frontView)
        layoutParams = frontView.layoutParams
        closeIcon.run {
            layoutParams.width = dp(32).toInt()
            layoutParams.height = dp(32).toInt()
        }
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
            if (isCanceled) return
            if (isSwipeable) {
                onSwipeAway?.invoke(this@SwipeableLayout)
            }
            else bounceBack()
        }
    }

    private var initX = 0f
    private var initY = 0f
    private var xOffset = 0f

    fun reset() {
        frontView.translationX = 0f
        backView.clipBounds = Rect(0, 0, 0, 0)
        backView.visibility = GONE
    }

    private var currentAnimator: ValueAnimator? = null
    private fun bounceBack() {
        currentAnimator?.cancel()
        currentAnimator = ValueAnimator.ofFloat(xOffset, 0f).apply {
            addUpdateListener {
                val f = it.animatedValue as Float
                backView.clipBounds = when {
                    f > 0 -> Rect(0, 0, f.toInt(), measuredHeight)
                    f < 0 -> Rect(measuredWidth + f.toInt(), 0, measuredWidth, measuredHeight)
                    else -> Rect(0, 0, 0, 0)
                }
                frontView.translationX = f
                xOffset = f
            }
            interpolator = SpringInterpolator()
            duration = 420L
            onEnd {
                backView.clipBounds = Rect(0, 0, 0, 0)
                backView.visibility = GONE
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
                xOffset = f
            }
            interpolator = DecelerateInterpolator()
            duration = 110L
            addListener(onAnimEndListener)
            start()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                xOffset = ev.x - initX
                parent.requestDisallowInterceptTouchEvent(true)
                currentAnimator?.cancel()
                frontView.translationX = xOffset
                backView.clipBounds =
                    if (xOffset > 0) {
                        closeIcon.translationX = dp(18)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(0, 0, xOffset.toInt() + cornerRadiusCompensation.toInt(), measuredHeight)
                    }
                    else {
                        closeIcon.translationX = measuredWidth - dp(50)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(measuredWidth + xOffset.toInt() - cornerRadiusCompensation.toInt(), 0, measuredWidth, measuredHeight)
                    }
                backView.visibility = VISIBLE
                return true
            }
            MotionEvent.ACTION_UP -> {
                when {
                    xOffset > measuredWidth/7*3 ||
                    xOffset > dp(64) && ev.eventTime - ev.downTime < 160 -> {
                        if (isSwipeable) sashayAway(1)
                        else bounceBack()
                    }
                    xOffset < -measuredWidth/7*3 ||
                    xOffset < -dp((64)) && ev.eventTime - ev.downTime < 160 -> {
                        if (isSwipeable) sashayAway(-1)
                        else bounceBack()
                    }
                    else -> bounceBack()
                }
                xOffset = 0f
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent) = when (ev.action) {
        MotionEvent.ACTION_MOVE -> {
            xOffset = ev.x - initX
            val absYOffset = abs(ev.y - initY)
            val absXOffset = abs(xOffset)
            if (abs(absXOffset - absYOffset) > context.dp(2) && absXOffset > absYOffset && !(frontView is ViewGroup && checkForHorizontalScroll(ev, frontView))) {
                true
            } else {
                initX = ev.x
                initY = ev.y
                super.onInterceptTouchEvent(ev)
            }
        }
        MotionEvent.ACTION_UP -> if (abs(xOffset) < context.dp(12) || frontView is ViewGroup && checkForHorizontalScroll(ev, frontView)) {
            super.onInterceptTouchEvent(ev)
        } else true
        else -> {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                initX = ev.x - xOffset
                initY = ev.y
                backView.visibility = VISIBLE
            }
            super.onInterceptTouchEvent(ev)
        }
    }

    fun setIconColor(value: Int) { closeIcon.imageTintList = ColorStateList.valueOf(value) }

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