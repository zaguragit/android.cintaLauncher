package io.posidon.android.cintalauncher.ui.drawer

import android.content.res.ColorStateList
import android.view.View
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
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import posidon.android.conveniencelib.getStatusBarHeight
import posidon.android.conveniencelib.onEnd

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
        scrollBar.onStartScroll = ::open
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

    fun open(v: View) {
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
        activity.blurBG.animate()
            .alpha(1f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(100)
            .onEnd { activity.blurBG.isVisible = true }
    }

    fun close(v: View? = null) {
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
        activity.blurBG.animate()
            .alpha(0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(100)
            .onEnd { activity.blurBG.isVisible = false }
    }
}
