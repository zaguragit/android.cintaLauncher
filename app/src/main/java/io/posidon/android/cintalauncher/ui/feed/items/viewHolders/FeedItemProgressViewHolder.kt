package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.content.res.ColorStateList
import android.view.View
import android.widget.ProgressBar
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItemWithProgress

class FeedItemProgressViewHolder(itemView: View) : FeedItemViewHolder(itemView) {
    val progress = itemView.findViewById<ProgressBar>(R.id.progress)
}

fun bindFeedItemProgressViewHolder(
    holder: FeedItemProgressViewHolder,
    item: FeedItemWithProgress,
    color: Int
) {
    bindFeedItemViewHolder(holder, item, color)
    holder.progress.max = item.max
    holder.progress.progress = item.progress
    holder.progress.isIndeterminate = false

    val color = ColorTheme.adjustColorForContrast(ColorTheme.uiBG, ColorTheme.accentColor)

    holder.progress.progressTintList = ColorStateList.valueOf(color)
    holder.progress.progressBackgroundTintList = ColorStateList.valueOf(color)


}