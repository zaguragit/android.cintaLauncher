package io.posidon.android.cintalauncher.ui.pinned.viewHolders

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.feed.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.ui.popup.appItem.ItemLongPress

class PinnedViewHolder(
    val icon: ImageView
) : RecyclerView.ViewHolder(icon) {
}

fun bindPinnedViewHolder(
    holder: PinnedViewHolder,
    item: LauncherItem,
    navbarHeight: Int,
    onDragOut: (view: View) -> Unit = {},
    onDragStart: (view: View) -> Unit = {},
) {
    holder.itemView.setOnDragListener(null)

    holder.icon.setImageDrawable(item.icon)
    //holder.icon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)

    holder.itemView.setOnClickListener {
        SuggestionsManager.onItemOpened(it.context, item)
        item.open(it.context.applicationContext, it)
    }
    holder.itemView.setOnLongClickListener {
        val backgroundColor = ColorTheme.tintAppDrawerItem(item.getColor())
        ItemLongPress.onItemLongPress(
            it,
            backgroundColor,
            ColorTheme.titleColorForBG(holder.itemView.context, backgroundColor),
            item,
            navbarHeight,
            onDragOut = onDragOut,
            onDragStart = onDragStart,
            isRemoveHandled = true,
        )
        true
    }
}