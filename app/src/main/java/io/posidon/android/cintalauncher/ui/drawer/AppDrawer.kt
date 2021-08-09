package io.posidon.android.cintalauncher.ui.drawer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
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

        var x = 0f
        var y = 0f
        val onLongPress = Runnable {
            recycler.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            DrawerLongPressPopup.show(recycler, x, y, activity.getNavigationBarHeight(), activity.settings, activity::reloadScrollbarController, activity::loadApps)
        }
        var lastRecyclerViewDownTouchEvent: MotionEvent? = null
        recycler.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                    if (recycler.findChildViewUnder(event.x, event.y) == null) {
                        lastRecyclerViewDownTouchEvent = event
                        v.handler.postDelayed(onLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                }
                MotionEvent.ACTION_UP ->
                    v.handler.removeCallbacks(onLongPress)
                MotionEvent.ACTION_MOVE -> if (lastRecyclerViewDownTouchEvent != null) {
                    // Check to see if it was a tap or a swipe
                    val xDelta = abs(lastRecyclerViewDownTouchEvent!!.x - event.x)
                    val yDelta = abs(lastRecyclerViewDownTouchEvent!!.y - event.y)
                    if (xDelta >= 30 || yDelta >= 30) {
                        v.handler.removeCallbacks(onLongPress)
                    }
                    lastRecyclerViewDownTouchEvent = null
                }
                MotionEvent.ACTION_CANCEL ->
                    v.handler.removeCallbacks(onLongPress)
            }
            false
        }
        bottomBar.setOnTouchListener { _, e ->
            when (e.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    x = e.rawX
                    y = e.rawY
                }
            }
            false
        }
        bottomBar.setOnLongClickListener {
            DrawerLongPressPopup.show(it, x, y, activity.getNavigationBarHeight(), activity.settings, activity::reloadScrollbarController, activity::loadApps)
            true
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
        view.setBackgroundColor(ColorTheme.appDrawerColor and 0xffffff or 0xdd000000.toInt())
        bottomBar.setBackgroundColor(ColorTheme.appDrawerBottomBarColor)
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
                val l = activity.blurBG.background as? LayerDrawable ?: return@addUpdateListener
                val x = it.animatedValue as Float
                l.getDrawable(0).alpha = (255 * (x).coerceAtMost(1f)).toInt()
                l.getDrawable(1).alpha = (255 * (x - 1f).coerceAtLeast(0f).coerceAtMost(1f)).toInt()
                l.getDrawable(2).alpha = (255 * (x - 2f).coerceAtLeast(0f)).toInt()
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
                val l = activity.blurBG.background as? LayerDrawable ?: return@addUpdateListener
                val x = it.animatedValue as Float
                l.getDrawable(0).alpha = (255 * (x).coerceAtMost(1f)).toInt()
                l.getDrawable(1).alpha = (255 * (x - 1f).coerceAtLeast(0f).coerceAtMost(1f)).toInt()
                l.getDrawable(2).alpha = (255 * (x - 2f).coerceAtLeast(0f)).toInt()
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
}
