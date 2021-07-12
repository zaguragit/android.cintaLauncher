package io.posidon.android.cintalauncher.ui.feed.home.summary

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem

abstract class SummaryViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    abstract fun onBind(summary: SummaryItem)
}