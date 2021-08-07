package io.posidon.android.cintalauncher.ui.popup.home.viewHolders

import android.view.View
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull
import io.posidon.android.cintalauncher.ui.popup.home.HomeLongPressPopupItem

class HomePopupTitleViewHolder(itemView: View) : HomePopupViewHolder(itemView) {

    val text = itemView.findViewById<TextView>(R.id.text)
    val description = itemView.findViewById<TextView>(R.id.description)

    override fun onBind(item: HomeLongPressPopupItem) {
        text.text = item.text
        description.text = item.description

        text.setTextColor(ColorTheme.cardTitle)

        itemView.setOnClickListener(item.onClick)

        applyIfNotNull(description, item.description) { view, value ->
            view.text = value
            description.setTextColor(ColorTheme.cardDescription)
        }
    }
}