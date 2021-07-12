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
}