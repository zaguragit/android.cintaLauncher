package io.posidon.android.cintalauncher.ui.feed.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.AppViewHolder
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.bindAppViewHolder
import posidon.android.conveniencelib.getNavigationBarHeight

class RecentlyOpenedItemsAdapter(
    launcherActivity: LauncherActivity,
) : RecyclerView.Adapter<AppViewHolder>() {

    val suggestionsManager = launcherActivity.suggestionsManager
    val navbarHeight = launcherActivity.getNavigationBarHeight()

    private var items: List<LauncherItem> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.app_drawer_item, parent, false) as CardView)
    }

    override fun onBindViewHolder(holder: AppViewHolder, i: Int) {
        val item = items[i]
        bindAppViewHolder(holder, item, null, suggestionsManager, navbarHeight)
    }

    fun updateItems(items: List<LauncherItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}