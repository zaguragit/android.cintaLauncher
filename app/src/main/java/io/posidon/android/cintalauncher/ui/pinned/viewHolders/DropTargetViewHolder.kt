package io.posidon.android.cintalauncher.ui.pinned.viewHolders

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme

class DropTargetViewHolder(
    val icon: ImageView,
) : RecyclerView.ViewHolder(icon)

fun bindDropTargetViewHolder(
    holder: DropTargetViewHolder,
) {
    holder.icon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
}