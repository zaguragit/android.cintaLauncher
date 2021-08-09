package io.posidon.android.cintalauncher.ui.popup.listPopup.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.ui.popup.listPopup.ListPopupItem

abstract class ListPopupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun onBind(item: ListPopupItem)
}