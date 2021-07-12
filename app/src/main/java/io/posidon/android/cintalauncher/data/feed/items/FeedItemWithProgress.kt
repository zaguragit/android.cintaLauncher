package io.posidon.android.cintalauncher.data.feed.items

import android.graphics.drawable.Drawable

interface FeedItemWithProgress : FeedItem {
    val max: Int
    val progress: Int
    val isIntermediate: Boolean
}