package io.posidon.android.cintalauncher.ui.drawer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.popup.drawer.DrawerLongPressPopup
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.getStatusBarHeight
import posidon.android.conveniencelib.onEnd
import kotlin.math.abs

class AppDrawer(
    private val activity: LauncherActivity,
    private val scrollBar: Scrollbar
) {

    val view = activity.findViewById<View>(R.id.app_drawer_container)!!

    private val adapter = AppDrawerAdapter(activity)

    private val bottomBar = view.findViewById<View>(R.id.bottom_bar)

    private val recycler = view.findViewById<RecyclerView>(R.id.app_recycler)
    private val closeButton = view.findViewById<ImageView>(R.id.back_button).apply {
        setOnClickListener(::close)
    }

    private var popupX = 0f
    private var popupY = 0f
    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        recycler.layoutManager = GridLayoutManager(view.context, 3, RecyclerView.VERTICAL, false).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(i: Int): Int {
                    return when (adapter.getItemViewType(i)) {
                        AppDrawerAdapter.APP_ITEM -> 1
                        AppDrawerAdapter.SECTION_HEADER -> 3
                        else -> -1
                    }
                }
            }
        }
        recycler.adapter = adapter
        recycler.setOnScrollChangeListener { _, _, _, _, _ -> adapter.onScroll() }
        scrollBar.onStartScroll = ::open

        val onLongPress = Runnable {
            recycler.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            DrawerLongPressPopup.show(recycler, popupX, popupY, activity.getNavigationBarHeight(), activity.settings, activity::reloadScrollbarController, activity::loadApps)
        }
        var lastRecyclerViewDownTouchEvent: MotionEvent? = null
        recycler.setOnTouchListener { v, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    popupX = event.rawX
                    popupY = event.rawY
                    if (recycler.findChildViewUnder(event.x, event.y) == null) {
                        v.handler.removeCallbacks(onLongPress)
                        lastRecyclerViewDownTouchEvent = event
                        v.handler.postDelayed(onLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                }
                MotionEvent.ACTION_MOVE -> if (lastRecyclerViewDownTouchEvent != null) {
                    val xDelta = abs(popupX - event.x)
                    val yDelta = abs(popupY - event.y)
                    if (xDelta >= 10 || yDelta >= 10) {
                        v.handler.removeCallbacks(onLongPress)
                        lastRecyclerViewDownTouchEvent = null
                    }
                }
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    v.handler.removeCallbacks(onLongPress)
                    lastRecyclerViewDownTouchEvent = null
                }
            }
            false
        }
    }

    var appSections: List<List<App>>? = null

    fun update(appSections: List<List<App>>) {
        this.appSections = appSections
        adapter.updateAppSections(appSections, activity, scrollBar.controller)
        scrollBar.postInvalidate()
        view.postInvalidate()
    }

    fun updateColorTheme() {
        view.background = ColorDrawable(ColorTheme.appDrawerColor and 0xffffff or 0xaa000000.toInt())
        bottomBar.background = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(ColorTheme.appDrawerBottomBarColor and 0xffffff or 0x88000000.toInt(), 0))
        closeButton.backgroundTintList = ColorStateList.valueOf(ColorTheme.buttonColor)
        closeButton.imageTintList = ColorStateList.valueOf(ColorTheme.titleColorForBG(activity, ColorTheme.buttonColor))
        scrollBar.recycler = this@AppDrawer.recycler
    }

    val isOpen get() = view.isVisible

    private var currentValueAnimator: ValueAnimator? = null

    fun open(v: View) {
        if (isOpen) return
        ItemLongPress.currentPopup?.dismiss()
        val sbh = v.context.getStatusBarHeight()
        recycler.setPadding(recycler.paddingLeft, sbh, recycler.paddingRight, recycler.paddingBottom)
        view.isVisible = true
        activity.feedRecycler.stopScroll()
        scrollBar.controller.showSelection = true
        activity.feedRecycler.animate()
            .alpha(0f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setStartDelay(0)
            .setDuration(100)
            .setInterpolator(AccelerateInterpolator())
            .onEnd { activity.feedRecycler.isInvisible = true }
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(0)
            .setDuration(100)
            .setInterpolator(DecelerateInterpolator())
            .onEnd { view.isVisible = true }
        activity.blurBG.isVisible = true
        val s = currentValueAnimator?.animatedValue as Float? ?: 0f
        currentValueAnimator?.cancel()
        activity.blurBG.isVisible = true
        currentValueAnimator = ValueAnimator.ofFloat(s, 3f).apply {
            addUpdateListener {
                updateBlurAnimation(it.animatedValue as Float)
            }
            interpolator = DecelerateInterpolator()
            duration = 200
            onEnd {
                currentValueAnimator = null
                activity.blurBG.isVisible = true
            }
            start()
        }
    }

    fun close(v: View? = null) {
        if (!isOpen) return
        ItemLongPress.currentPopup?.dismiss()
        scrollBar.controller.showSelection = false
        activity.feedRecycler.isInvisible = false
        activity.feedRecycler.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(0)
            .setDuration(100)
            .setInterpolator(DecelerateInterpolator())
            .onEnd { activity.feedRecycler.isInvisible = false }
        view.animate()
            .alpha(0f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setStartDelay(0)
            .setDuration(100)
            .setInterpolator(AccelerateInterpolator())
            .onEnd { view.isVisible = false }
        val s = currentValueAnimator?.animatedValue as Float? ?: 3f
        currentValueAnimator?.cancel()
        activity.blurBG.isVisible = true
        currentValueAnimator = ValueAnimator.ofFloat(s, 0f).apply {
            addUpdateListener {
                updateBlurAnimation(it.animatedValue as Float)
            }
            interpolator = AccelerateInterpolator()
            duration = 135
            onEnd {
                currentValueAnimator = null
                activity.blurBG.isVisible = false
                activity.updateBlur()
            }
            start()
        }
    }

    fun updateBlurAnimation(x: Float) {
        val l = activity.blurBG.background as? LayerDrawable ?: return
        l.getDrawable(0).alpha = (255 * (x).coerceAtMost(1f)).toInt()
        l.getDrawable(1).alpha = (255 * (x - 1f).coerceAtLeast(0f).coerceAtMost(1f)).toInt()
        l.getDrawable(2).alpha = (255 * (x - 2f).coerceAtLeast(0f)).toInt()
        l.getDrawable(3).alpha = (120 * ((x - .5f) / 2.5f).coerceAtLeast(0f)).toInt()
    }
}
