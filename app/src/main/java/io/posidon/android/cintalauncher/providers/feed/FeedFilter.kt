package io.posidon.android.cintalauncher.providers.feed

import android.graphics.drawable.Drawable
import io.posidon.android.cintalauncher.data.feed.items.FeedItem

class FeedFilter(
    val name: String,
    val icon: Drawable? = null,
    val filter: (FeedItem) -> Boolean,
)