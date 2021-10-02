package io.posidon.android.cintalauncher.ui.feed.items.viewHolders.home

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.providers.feed.notification.NotificationService
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.popup.home.HomeLongPressPopup
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.getStatusBarHeight

class HomeViewHolder(
    val launcherActivity: LauncherActivity,
    val launcherContext: LauncherContext,
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    val clockContainer = itemView.findViewById<View>(R.id.clock_container)!!

    val weekDay = clockContainer.findViewById<TextView>(R.id.week_day)!!
    val time = clockContainer.findViewById<TextView>(R.id.time)!!
    val date = clockContainer.findViewById<TextView>(R.id.date)!!

    val notificationIconsAdapter = NotificationIconsAdapter()
    val notificationIconsContainer = itemView.findViewById<View>(R.id.notification_icon_container)!!
    val notificationIconsRecycler = itemView.findViewById<RecyclerView>(R.id.notification_icon_list)!!.apply {
        layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
        adapter = notificationIconsAdapter
    }
    val notificationIconsText = itemView.findViewById<TextView>(R.id.notification_icon_text)!!

    val vertical = itemView.findViewById<LinearLayout>(R.id.vertical)!!

    init {
        NotificationService.setOnUpdate(javaClass.name) { itemView.post(::updateNotificationIcons) }
        clockContainer.setPadding(0, itemView.context.getStatusBarHeight(), 0, 0)
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
}

private var popupX = 0f
private var popupY = 0f
@SuppressLint("ClickableViewAccessibility")
fun bindHomeViewHolder(
    holder: HomeViewHolder
) {
    holder.updateNotificationIcons()
    holder.time.setTextColor(ColorTheme.uiTitle)
    holder.date.setTextColor(ColorTheme.uiDescription)
    holder.weekDay.setTextColor(ColorTheme.uiDescription)
    holder.notificationIconsText.setTextColor(ColorTheme.uiTitle)
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