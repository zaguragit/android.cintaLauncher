package io.posidon.android.cintalauncher.ui.feed.home.summary

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.summary.SummaryItem
import io.posidon.android.cintalauncher.data.feed.summary.notification.NotificationSummary
import posidon.android.conveniencelib.dp

class NotificationSummaryViewHolder(
    itemView: View
) : SummaryViewHolder(itemView) {

    val source = itemView.findViewById<TextView>(R.id.source)!!
    val description = itemView.findViewById<TextView>(R.id.description)!!
    val icon = itemView.findViewById<ImageView>(R.id.icon)!!

    override fun onBind(summary: SummaryItem) {
        summary as NotificationSummary
        icon.setImageDrawable(summary.sourceIcon)
        source.text = summary.source
        description.text = summary.description
        itemView.setOnClickListener(summary.onTap)

        val sourceColor = ColorStateList.valueOf(ColorTheme.uiDescription)
        source.setTextColor(sourceColor)
        icon.imageTintList = sourceColor
        description.setTextColor(ColorTheme.uiTitle)

        description.setShadowLayer(itemView.context.dp(5), description.shadowDx, description.shadowDy, ColorTheme.uiBG and 0xffffff or 0x55000000)
        source.setShadowLayer(itemView.context.dp(3), source.shadowDx, source.shadowDy, ColorTheme.uiBG and 0xffffff or 0x33000000)
    }
}