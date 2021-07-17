package io.posidon.android.lookerupper.ui.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.SearchResult

class AppSearchViewHolder(itemView: View, val navbarHeight: Int) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val text = itemView.findViewById<TextView>(R.id.icon_text)!!
    val card = itemView.findViewById<CardView>(R.id.card)!!

    override fun onBind(result: SearchResult) {
        result as AppResult
        icon.setImageDrawable(result.icon)
        text.text = result.title
        val backgroundColor = ColorTheme.tintAppDrawerItem(result.getColor())
        text.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
        card.setCardBackgroundColor(backgroundColor)
        itemView.setOnClickListener(result::open)
        itemView.setOnLongClickListener {
            ItemLongPress.onItemLongPress(it.context, backgroundColor, ColorTheme.titleColorForBG(itemView.context, backgroundColor), it, result.app, navbarHeight)
            true
        }
    }
}