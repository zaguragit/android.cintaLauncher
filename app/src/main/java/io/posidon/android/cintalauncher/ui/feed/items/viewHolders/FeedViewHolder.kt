package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.ui.view.SwipeableLayout

abstract class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(SwipeableLayout(itemView)) {
    abstract fun onBind(
        item: FeedItem,
        color: Int
    )
}

inline fun <T: View, R> applyIfNotNull(view: T, value: R, block: (T, R) -> Unit) {
    if (value == null) {
        view.isVisible = false
    } else {
        view.isVisible = true
        block(view, value)
    }
}