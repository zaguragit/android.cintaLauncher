package io.posidon.android.cintalauncher.data.feed.items

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import io.posidon.android.cintalauncher.R
import java.time.Instant

interface FeedItem {
    val color: Int
    val title: String
    val sourceIcon: Drawable?
    val description: String?
    val source: String?

    val actions: Array<FeedItemAction> get() = emptyArray()

    val instant: Instant
    val importance get() = 0

    val isNotification get() = false

    fun onTap(view: View)

    val isDismissible: Boolean
    fun onDismiss(view: View) {}

    val shouldTintIcon get() = true

    /**
     * Unique identifier (globally unique to this feed item)
     */
    val uid: String

    /**
     * Identifier (should be unique to this feed item, but that's not guaranteed)
     */
    val id: Long
}

fun String.longHash(): Long {
    var h = 1125899906842597L // prime
    for (i in 0 until length) {
        h = 31 * h + this[i].code.toLong()
    }
    return h
}

fun FeedItem.formatTimeAgo(resources: Resources): String {
    val now = System.currentTimeMillis()
    val passed = now - instant.toEpochMilli()
    val seconds = passed / 1000
    if (seconds < 60) {
        return resources.getString(R.string.now)
    }
    val minutes = seconds / 60
    if (minutes < 60) {
        return "${minutes}m"
    }
    val hours = minutes / 60
    if (hours < 24) {
        return "${hours}h"
    }
    return "${hours / 24}d"
}