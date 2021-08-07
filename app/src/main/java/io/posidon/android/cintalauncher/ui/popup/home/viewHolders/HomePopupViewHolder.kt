package io.posidon.android.cintalauncher.ui.popup.home.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.ui.popup.home.HomeLongPressPopupItem

abstract class HomePopupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun onBind(item: HomeLongPressPopupItem)
}