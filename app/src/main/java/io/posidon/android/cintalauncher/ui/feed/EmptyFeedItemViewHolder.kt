package io.posidon.android.cintalauncher.ui.feed

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme

class EmptyFeedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val text = itemView.findViewById<TextView>(R.id.text)
}

fun bindEmptyFeedItemViewHolder(
    holder: EmptyFeedItemViewHolder
) {
    holder.text.text = holder.itemView.context.getString(R.string.no_feed_items)
    holder.text.setTextColor(ColorTheme.uiDescription)
    holder.itemView.setBackgroundColor(ColorTheme.uiBG)
}