package io.posidon.android.lookerupper.ui.viewHolders

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull
import io.posidon.android.lookerupper.data.results.CompactResult
import io.posidon.android.lookerupper.data.results.SearchResult

class CompactSearchViewHolder(
    itemView: View,
    val activity: Activity,
    val isOnCard: Boolean
) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon)!!
    val text = itemView.findViewById<TextView>(R.id.text)!!
    val subtitle = itemView.findViewById<TextView>(R.id.subtitle)!!

    override fun onBind(result: SearchResult) {
        result as CompactResult
        icon.setImageDrawable(result.icon)
        text.text = result.title
        applyIfNotNull(subtitle, result.subtitle, TextView::setText)
        text.setTextColor(if (isOnCard) ColorTheme.cardTitle else ColorTheme.uiTitle)
        subtitle.setTextColor(if (isOnCard) ColorTheme.cardDescription else ColorTheme.uiDescription)
        itemView.setOnClickListener(result::open)
        itemView.setOnLongClickListener(result.onLongPress?.let { { v -> it(v, activity) } })
    }
}