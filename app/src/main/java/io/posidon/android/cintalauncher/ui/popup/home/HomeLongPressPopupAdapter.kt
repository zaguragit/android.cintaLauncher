package io.posidon.android.cintalauncher.ui.popup.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.ui.popup.home.viewHolders.HomePopupItemViewHolder
import io.posidon.android.cintalauncher.ui.popup.home.viewHolders.HomePopupTitleViewHolder
import io.posidon.android.cintalauncher.ui.popup.home.viewHolders.HomePopupViewHolder

class HomeLongPressPopupAdapter : RecyclerView.Adapter<HomePopupViewHolder>() {

    override fun getItemViewType(i: Int): Int {
        return when {
            items[i].isTitle -> 1
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePopupViewHolder {
        return when (viewType) {
            1 -> HomePopupTitleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.long_press_home_popup_title, parent, false))
            else -> HomePopupItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.long_press_home_popup_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: HomePopupViewHolder, i: Int) {
        holder.onBind(items[i])
    }

    override fun getItemCount() = items.size

    private var items: List<HomeLongPressPopupItem> = emptyList()

    fun updateItems(items: List<HomeLongPressPopupItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}
