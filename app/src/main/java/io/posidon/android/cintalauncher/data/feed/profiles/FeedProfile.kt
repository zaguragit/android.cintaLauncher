package io.posidon.android.cintalauncher.data.feed.profiles

import android.graphics.drawable.Drawable
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.FeedItemSuggestedApps
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithMedia
import io.posidon.android.cintalauncher.data.feed.items.isToday

class FeedProfile(
    val name: String? = null,
    val icon: Drawable? = null,

    val showAppSuggestions: Boolean,
    val showMedia: Boolean,
    val showNews: Boolean,
    val showNotifications: Boolean,
    val onlyToday: Boolean,
    val extraFeedProfileSettings: ExtraFeedProfileSettings? = null,
) {
    fun filter(item: FeedItem): Boolean {
        val today = !onlyToday || item.isToday()
        val suggestions = showAppSuggestions || item !is FeedItemSuggestedApps
        val media = showMedia || item !is FeedItemWithMedia
        val notification = showNotifications || item.meta?.isNotification != true
        val news = showNews || item.meta?.isNotification == true
        return today && suggestions && media && notification && news && extraFeedProfileSettings?.filter(item) ?: true
    }
}