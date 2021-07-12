package io.posidon.android.cintalauncher.ui.feed.home.summary

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.notification.NotificationSummary

class NotificationSummaryViewHolder(
    itemView: View
) : SummaryViewHolder(itemView) {

    val source = itemView.findViewById<TextView>(R.id.source)
    val description = itemView.findViewById<TextView>(R.id.description)
    val icon = itemView.findViewById<ImageView>(R.id.icon)

    override fun onBind(summary: SummaryItem) {
        summary as NotificationSummary
        icon.setImageDrawable(summary.sourceIcon)
        source.text = summary.source
        description.text = summary.description
        itemView.setOnClickListener(summary.onTap)
    }
}