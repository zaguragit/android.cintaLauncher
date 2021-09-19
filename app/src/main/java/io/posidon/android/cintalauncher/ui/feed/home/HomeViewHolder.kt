package io.posidon.android.cintalauncher.ui.feed.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.providers.feed.notification.NotificationService
import io.posidon.android.cintalauncher.providers.feed.summary.NotificationSummariesProvider
import io.posidon.android.cintalauncher.providers.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.feed.home.pinned.PinnedItemsAdapter
import io.posidon.android.cintalauncher.ui.feed.home.suggestions.SuggestionsAdapter
import io.posidon.android.cintalauncher.ui.feed.home.summary.NotificationIconsAdapter
import io.posidon.android.cintalauncher.ui.feed.home.summary.SummaryAdapter
import io.posidon.android.cintalauncher.ui.popup.home.HomeLongPressPopup
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.cintalauncher.util.InvertedRoundRectDrawable
import io.posidon.android.lookerupper.ui.SearchActivity
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.getStatusBarHeight
import kotlin.math.ceil

class HomeViewHolder(
    val scrollIndicator: ImageView,
    parentView: ViewGroup,
    val launcherActivity: LauncherActivity,
    val launcherContext: LauncherContext,
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    val clockContainer = itemView.findViewById<View>(R.id.clock_container)!!

    val weekDay = clockContainer.findViewById<TextView>(R.id.week_day)!!
    val time = clockContainer.findViewById<TextView>(R.id.time)!!
    val date = clockContainer.findViewById<TextView>(R.id.date)!!

    val summaryCard = itemView.findViewById<View>(R.id.summary_card)!!
    val summaryAdapter = SummaryAdapter()
    val summaryRecycler = summaryCard.findViewById<RecyclerView>(R.id.summary_recycler)!!.apply {
        layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
        adapter = summaryAdapter
    }
    val pinnedAdapter = PinnedItemsAdapter(launcherActivity, launcherContext)
    val pinnedRecycler = summaryCard.findViewById<RecyclerView>(R.id.pinned_recycler)!!.apply {
        layoutManager = GridLayoutManager(itemView.context, 3, RecyclerView.VERTICAL, true)
        adapter = pinnedAdapter
    }
    val recentlyOpenedAdapter = SuggestionsAdapter(launcherActivity)
    val recentlyOpenedRecycler = summaryCard.findViewById<RecyclerView>(R.id.recents_recycler)!!.apply {
        layoutManager = GridLayoutManager(itemView.context, 3, RecyclerView.VERTICAL, false)
        adapter = recentlyOpenedAdapter
    }
    val notificationIconsAdapter = NotificationIconsAdapter()
    val notificationIconsContainer = itemView.findViewById<View>(R.id.notification_icon_container)!!
    val notificationIconsRecycler = itemView.findViewById<RecyclerView>(R.id.notification_icon_list)!!.apply {
        layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
        adapter = notificationIconsAdapter
    }
    val notificationIconsText = itemView.findViewById<TextView>(R.id.notification_icon_text)!!

    val searchCard = itemView.findViewById<CardView>(R.id.search_bar_container)!!.apply {
        setOnClickListener {
            val context = it.context
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
    val searchIcon = searchCard.findViewById<ImageView>(R.id.search_bar_icon)!!
    @SuppressLint("ClickableViewAccessibility")
    val appDrawerIcon = searchCard.findViewById<ImageView>(R.id.app_drawer_icon)!!.apply {
        setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    launcherActivity.appDrawer.open(v)
                    launcherActivity.appDrawer.scrollIcon.onTouchEvent(makeMotionEventForAppDrawerScrollIcon(e))
                }
                else ->
                    launcherActivity.appDrawer.scrollIcon.onTouchEvent(makeMotionEventForAppDrawerScrollIcon(e))
            }
            parent.requestDisallowInterceptTouchEvent(true)
            true
        }
    }

    private fun makeMotionEventForAppDrawerScrollIcon(e: MotionEvent): MotionEvent {
        val location = IntArray(2)
        launcherActivity.appDrawer.scrollIcon.getLocationOnScreen(location)
        return MotionEvent.obtain(
            e.downTime,
            e.eventTime,
            e.action,
            e.rawX - location[0],
            e.rawY - location[1],
            e.pressure,
            e.size,
            e.metaState,
            e.xPrecision,
            e.yPrecision,
            e.deviceId,
            e.edgeFlags,
        )
    }

    val blurBG = searchCard.findViewById<SeeThoughView>(R.id.blur_bg)!!

    val vertical = itemView.findViewById<LinearLayout>(R.id.vertical)!!
    val indicatorContainer = itemView.findViewById<ViewGroup>(R.id.swipe_up_indicator_container)!!

    init {
        NotificationSummariesProvider.init(itemView.context) {
            itemView.post(::updateSummary)
        }
        itemView.layoutParams.apply {
            height = parentView.measuredHeight - parentView.paddingBottom - parentView.paddingTop
        }
        val s = itemView.dp(24).toInt()
        indicatorContainer.addView(scrollIndicator, LinearLayout.LayoutParams(s, s))
        val r = itemView.context.resources.getDimension(R.dimen.dock_corner_radius)
        itemView.background = InvertedRoundRectDrawable(
            floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), 0f, 0)
        clockContainer.setPadding(0, itemView.context.getStatusBarHeight(), 0, 0)
    }

    fun updateSummary() {
        val ns = NotificationSummariesProvider.get()
        if (ns.isEmpty()) {
            summaryRecycler.isVisible = false
        } else {
            summaryRecycler.isVisible = true
            summaryAdapter.updateSummaries(ns)
        }
    }

    fun updatePinned() {
        pinnedAdapter.showDropTarget(-1)
        val pinned = launcherContext.appManager.pinnedItems
        if (pinned.isEmpty()) {
            pinnedRecycler.isVisible = false
        } else {
            pinnedRecycler.isVisible = true
            pinnedAdapter.updateItems(pinned)
        }
    }

    fun updateRecents() {
        val recent = SuggestionsManager.getNonPinnedSuggestions(launcherContext.appManager.pinnedItems).let { it.subList(0, run {
            val l = it.size.coerceAtMost(6)
            if (l < 6) l.coerceAtMost(3) else l
        }) }
        if (recent.isEmpty()) {
            recentlyOpenedRecycler.isVisible = false
        } else {
            recentlyOpenedRecycler.isVisible = true
            recentlyOpenedAdapter.updateItems(recent)
        }
    }

    fun updateNotificationIcons() {
        val icons = NotificationService.notifications.groupBy {
            it.sourceIcon?.constantState
        }.mapNotNull { it.key?.newDrawable() }
        if (icons.isEmpty()) {
            notificationIconsContainer.isVisible = false
        } else {
            notificationIconsContainer.isVisible = true
            if (notificationIconsAdapter.updateItems(icons)) {
                notificationIconsText.text =
                    itemView.resources.getQuantityString(R.plurals.x_notifications, icons.size, icons.size)
            }
        }
    }

    fun onScroll() {
        blurBG.invalidate()
        summaryAdapter.onScroll()
        recentlyOpenedAdapter.onScroll()
        pinnedAdapter.onScroll()
    }

    fun getPinnedItemIndex(x: Float, y: Float): Int {
        val location = IntArray(2)
        pinnedRecycler.getLocationOnScreen(location)
        val lx = (x - location[0])
        val ly = (y - location[1])
        if (lx < 0 || ly < 0 || lx > pinnedRecycler.width || ly > pinnedRecycler.height)
            return -1
        val count = pinnedAdapter.itemCount
        val xx = lx.toInt() * 3 / pinnedRecycler.width
        val yy = (pinnedRecycler.height - ly.toInt()) * ceil(count / 3f).toInt() / pinnedRecycler.height
        val i = xx + yy * 3
        return i.coerceAtMost(count - 1)
    }
}

private var popupX = 0f
private var popupY = 0f
@SuppressLint("ClickableViewAccessibility")
fun bindHomeViewHolder(
    holder: HomeViewHolder
) {
    holder.updateSummary()
    holder.updatePinned()
    holder.updateRecents()
    holder.updateNotificationIcons()
    (holder.itemView.background as InvertedRoundRectDrawable).outerColor = ColorTheme.uiBG
    holder.searchCard.setCardBackgroundColor(ColorTheme.searchBarBG)
    holder.searchIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
    holder.appDrawerIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
    holder.blurBG.drawable = acrylicBlur?.smoothBlur?.let { BitmapDrawable(holder.itemView.resources, it) }
    holder.time.setTextColor(ColorTheme.wallTitle)
    holder.date.setTextColor(ColorTheme.wallDescription)
    holder.weekDay.setTextColor(ColorTheme.wallDescription)
    holder.scrollIndicator.imageTintList = ColorStateList.valueOf(ColorTheme.wallHint)
    holder.notificationIconsText.setTextColor(ColorTheme.wallTitle)
    holder.clockContainer.backgroundTintList = ColorStateList.valueOf(ColorTheme.wallColor)
    holder.itemView.setOnTouchListener { _, e ->
        when (e.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                popupX = e.rawX
                popupY = e.rawY
            }
        }
        false
    }
    holder.itemView.setOnLongClickListener {
        HomeLongPressPopup.show(it, popupX, popupY, holder.launcherActivity.getNavigationBarHeight(), holder.launcherContext.settings, holder.launcherActivity::reloadColorThemeSync)
        true
    }
}