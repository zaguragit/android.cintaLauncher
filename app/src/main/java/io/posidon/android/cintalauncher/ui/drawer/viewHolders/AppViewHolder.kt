package io.posidon.android.cintalauncher.ui.drawer.viewHolders

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.AppSuggestionsManager
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter.Companion.APP_ITEM
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress

class AppViewHolder(
    val card: CardView
) : RecyclerView.ViewHolder(card) {

    val icon = itemView.findViewById<ImageView>(R.id.icon_image)!!
    val label = itemView.findViewById<TextView>(R.id.icon_text)!!
}

class AppItem(val item: App) : AppDrawerAdapter.DrawerItem {
    override fun getItemViewType() = APP_ITEM
    override val label: String
        get() = item.label
}

fun bindAppViewHolder(
    holder: AppViewHolder,
    item: LauncherItem,
    isDimmed: Boolean,
    suggestionsManager: AppSuggestionsManager,
    navbarHeight: Int,
) {
    holder.card.alpha = if (isDimmed) .3f else 1f
    holder.icon.setImageDrawable(item.icon)
    holder.label.text = item.label
    val backgroundColor = ColorTheme.tintAppDrawerItem(item.getColor())
    holder.label.setTextColor(ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor))
    holder.card.setCardBackgroundColor(backgroundColor)
    holder.itemView.setOnClickListener {
        suggestionsManager.onItemOpened(item)
        item.open(it.context.applicationContext, it)
    }
    holder.itemView.setOnLongClickListener {
        ItemLongPress.onItemLongPress(it.context, backgroundColor, ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor), it, item, navbarHeight)
        true
    }
}