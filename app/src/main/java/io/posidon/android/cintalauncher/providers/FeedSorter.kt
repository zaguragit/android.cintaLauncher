package io.posidon.android.cintalauncher.providers

import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import java.util.*

object FeedSorter {
    fun rearrange(items: ArrayList<FeedItem>): List<FeedItem> {
        items.sortByDescending {
            var r = it.instant
            val m = it.meta
            if (m != null) {
                if (m.isNotification) r = r.plusMillis(3600L * 12)
                when (m.importance) {
                    1 -> r = r.plusMillis(3600L * 2)
                    2 -> r = r.plusMillis(3600L * 7)
                }
            }
            r
        }
        return items
    }

    fun getMostRelevant(items: List<FeedItem>): FeedItem? {
        return items.maxByOrNull {
            var r = it.instant
            val m = it.meta
            if (m != null) {
                if (m.isNotification) r = r.plusMillis(3600L * 12)
                when (m.importance) {
                    1 -> r = r.plusMillis(3600L * 2)
                    2 -> r = r.plusMillis(3600L * 7)
                }
            }
            r
        }
    }
}