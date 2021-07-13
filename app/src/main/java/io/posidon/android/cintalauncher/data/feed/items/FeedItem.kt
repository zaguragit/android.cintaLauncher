package io.posidon.android.cintalauncher.data.feed.items

import android.graphics.drawable.Drawable
import android.view.View
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