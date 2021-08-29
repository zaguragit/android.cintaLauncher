package io.posidon.android.cintalauncher.ui.feed.home.pinned

import android.graphics.drawable.BitmapDrawable
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.ui.acrylicBlur
import io.posidon.android.cintalauncher.ui.view.SeeThoughView

class DropTargetViewHolder(
    val card: CardView,
    val map: HashMap<LauncherItem, () -> Unit>,
) : RecyclerView.ViewHolder(card) {

    val blurBG = itemView.findViewById<SeeThoughView>(R.id.blur_bg)!!
}

fun bindDropTargetViewHolder(
    holder: DropTargetViewHolder,
) {
    holder.blurBG.drawable = BitmapDrawable(holder.itemView.resources, acrylicBlur?.smoothBlur)

    val backgroundColor = ColorTheme.appDrawerItemBase
    holder.card.setCardBackgroundColor(backgroundColor)
}