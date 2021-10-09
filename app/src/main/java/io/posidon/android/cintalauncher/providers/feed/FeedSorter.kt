package io.posidon.android.cintalauncher.providers.feed

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import java.time.Instant
import java.util.*

object FeedSorter {
    fun rearrange(items: ArrayList<FeedItem>): List<FeedItem> {
        items.sortByDescending(FeedSorter::sorter)
        return items
    }

    fun getMostRelevant(items: List<FeedItem>): FeedItem? {
        return items.maxByOrNull(FeedSorter::sorter)
    }

    private fun sorter(it: FeedItem): Instant {
        var r = it.instant
        val m = it.meta
        if (m != null) {
            if (m.isNotification) r = r.plusMillis(3600L * 12)
            when (m.importance) {
                1 -> r = r.plusMillis(3600L * 2)
                2 -> r = r.plusMillis(3600L * 7)
            }
        }
        return r
    }
}