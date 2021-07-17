package io.posidon.android.lookerupper.ui.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.data.results.ShortcutResult

class ShortcutSearchViewHolder(itemView: View) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon)!!
    val text = itemView.findViewById<TextView>(R.id.text)!!
    val subtitle = itemView.findViewById<TextView>(R.id.subtitle)!!

    override fun onBind(result: SearchResult) {
        result as ShortcutResult
        icon.setImageDrawable(result.icon)
        text.text = result.title
        subtitle.text = result.app.title
        text.setTextColor(ColorTheme.titleColorForBG(itemView.context, ColorTheme.uiBG))
        subtitle.setTextColor(ColorTheme.textColorForBG(itemView.context, ColorTheme.uiBG))
        itemView.setOnClickListener(result::open)
    }
}