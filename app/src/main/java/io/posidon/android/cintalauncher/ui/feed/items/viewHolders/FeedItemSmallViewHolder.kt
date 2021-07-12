package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.view.View
import io.posidon.android.cintalauncher.data.feed.items.FeedItemSmall
import io.posidon.android.cintalauncher.ui.color.ColorTheme

class FeedItemSmallViewHolder(itemView: View) : FeedItemViewHolder(itemView)

fun bindFeedItemSmallViewHolder(
    holder: FeedItemSmallViewHolder,
    item: FeedItemSmall,
    color: Int
) {
    bindFeedItemViewHolder(holder, item, color)
}