package io.posidon.android.cintalauncher.ui.feed.home.suggestions

import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.popup.appItem.ItemLongPress
import io.posidon.android.cintalauncher.ui.view.SeeThoughView

class SuggestionViewHolder(
    val card: CardView,
    val map: HashMap<LauncherItem, () -> Unit>
) : RecyclerView.ViewHolder(card) {
    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!
    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!
}

fun bindSuggestionViewHolder(
    holder: SuggestionViewHolder,
    item: LauncherItem,
    navbarHeight: Int,
    onDragOut: (view: View) -> Unit = {},
) {
    holder.blurBG.drawable = BitmapDrawable(holder.itemView.resources, acrylicBlur?.insaneBlur)
    holder.map[item] = holder.blurBG::invalidate

    val backgroundColor = ColorTheme.tintAppDrawerItem(item.getColor())
    holder.card.setCardBackgroundColor(backgroundColor)
    holder.label.text = item.label
    holder.label.setTextColor(ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor))
    holder.icon.setImageDrawable(item.icon)

    holder.itemView.setOnClickListener {
        SuggestionsManager.onItemOpened(it.context, item)
        item.open(it.context.applicationContext, it)
    }
    holder.itemView.setOnLongClickListener {
        ItemLongPress.onItemLongPress(
            it,
            backgroundColor,
            ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor),
            item,
            navbarHeight,
            onDragOut = onDragOut
        )
        true
    }
}