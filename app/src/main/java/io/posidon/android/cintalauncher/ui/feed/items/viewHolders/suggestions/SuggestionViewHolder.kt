package io.posidon.android.cintalauncher.ui.feed.items.viewHolders.suggestions

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.feed.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.appItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.SeeThoughView

class SuggestionViewHolder(
    val card: CardView
) : RecyclerView.ViewHolder(card) {
    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!
    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!

    fun onBind(
        item: LauncherItem,
        navbarHeight: Int,
    ) {
        blurBG.drawable = BitmapDrawable(itemView.resources, acrylicBlur?.insaneBlur)

        val backgroundColor = ColorTheme.tintAppDrawerItem(item.getColor())
        card.setCardBackgroundColor(backgroundColor)
        label.text = item.label
        label.setTextColor(ColorTheme.titleColorForBG(itemView.context, backgroundColor))
        icon.setImageDrawable(item.icon)

        itemView.setOnClickListener {
            SuggestionsManager.onItemOpened(it.context, item)
            item.open(it.context.applicationContext, it)
        }
        itemView.setOnLongClickListener {
            ItemLongPress.onItemLongPress(
                it,
                backgroundColor,
                ColorTheme.titleColorForBG(itemView.context, backgroundColor),
                item,
                navbarHeight,
            )
            true
        }
    }
}