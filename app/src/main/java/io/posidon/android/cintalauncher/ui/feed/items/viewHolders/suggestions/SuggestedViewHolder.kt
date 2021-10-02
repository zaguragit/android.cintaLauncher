package io.posidon.android.cintalauncher.ui.feed.items.viewHolders.suggestions

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.FeedItemSuggestedApps
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.FeedViewHolder

class SuggestedViewHolder(
    val launcherActivity: LauncherActivity,
    itemView: View,
) : FeedViewHolder(itemView) {

    val separator = itemView.findViewById<View>(R.id.separator)!!

    val adapter = SuggestionsAdapter(launcherActivity)
    val recycler = itemView.findViewById<RecyclerView>(R.id.recents_recycler)!!.apply {
        layoutManager = GridLayoutManager(itemView.context, 3, RecyclerView.VERTICAL, false)
        adapter = this@SuggestedViewHolder.adapter
    }

    override fun onBind(item: FeedItem, color: Int) {
        item as FeedItemSuggestedApps
        separator.setBackgroundColor(ColorTheme.uiHint and 0x00ffffff or 0x24ffffff)
        adapter.updateItems(item.apps)
    }
}