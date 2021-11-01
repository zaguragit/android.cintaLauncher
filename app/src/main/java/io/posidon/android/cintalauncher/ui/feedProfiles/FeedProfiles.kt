package io.posidon.android.cintalauncher.ui.feedProfiles

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.profiles.FeedProfile
import io.posidon.android.cintalauncher.ui.LauncherActivity

class FeedProfiles(val activity: LauncherActivity) {

    val feedFilterAdapter = FeedProfileAdapter(activity.launcherContext)
    val feedFilterRecycler = activity.findViewById<RecyclerView>(R.id.feed_filters_recycler)!!.apply {
        layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        adapter = feedFilterAdapter
    }

    init {
        feedFilterAdapter.updateItems(
            FeedProfile(
                icon = activity.getDrawable(R.drawable.ic_home),
                showAppSuggestions = true,
                showMedia = true,
                showNews = true,
                showNotifications = true,
                onlyToday = false,
            ),
            FeedProfile(
                activity.getString(R.string.today),
                icon = activity.getDrawable(R.drawable.ic_lightness),
                showAppSuggestions = true,
                showMedia = true,
                showNews = true,
                showNotifications = true,
                onlyToday = true,
            ),
            FeedProfile(
                activity.getString(R.string.news),
                icon = activity.getDrawable(R.drawable.ic_news),
                showAppSuggestions = false,
                showMedia = false,
                showNews = true,
                showNotifications = false,
                onlyToday = false,
            ),
            FeedProfile(
                activity.getString(R.string.notifications),
                icon = activity.getDrawable(R.drawable.ic_notification),
                showAppSuggestions = false,
                showMedia = false,
                showNews = false,
                showNotifications = true,
                onlyToday = false,
            ),
        )
    }

    fun updateColorTheme() {
        feedFilterAdapter.updateColorTheme()
    }
}