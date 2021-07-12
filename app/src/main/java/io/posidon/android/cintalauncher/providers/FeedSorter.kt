package io.posidon.android.cintalauncher.providers

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import java.util.ArrayList

object FeedSorter {
    fun rearrange(feedItems: ArrayList<FeedItem>): List<FeedItem> {
        feedItems.sortByDescending {
            var r = it.instant
            if (it.isNotification) r = r.plusMillis(3600 * 12)
            when (it.importance) {
                1 -> r = r.plusMillis(3600 * 2)
                2 -> r = r.plusMillis(3600 * 7)
            }
            r
        }
        return feedItems
    }
}