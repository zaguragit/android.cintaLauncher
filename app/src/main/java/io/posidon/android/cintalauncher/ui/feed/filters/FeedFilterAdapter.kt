package io.posidon.android.cintalauncher.ui.feed.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.providers.feed.FeedFilter
import io.posidon.android.cintalauncher.ui.feed.items.viewHolders.applyIfNotNull

class FeedFilterAdapter(
    val launcherContext: LauncherContext
) : RecyclerView.Adapter<FeedFilterViewHolder>() {

    private var items: MutableList<Pair<FeedFilter, Boolean>> = ArrayList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedFilterViewHolder {
        return FeedFilterViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_filter, parent, false)).apply {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                items[adapterPosition] = item.copy(second = !item.second)
                notifyItemChanged(adapterPosition)
                launcherContext.feed.updateFilters(items.filter { it.second }.map { it.first })
            }
        }
    }

    override fun onBindViewHolder(holder: FeedFilterViewHolder, i: Int) {
        val (item, isEnabled) = items[i]
        holder.text.text = item.name
        applyIfNotNull(holder.icon, item.icon, ImageView::setImageDrawable)
        val bgColor = if (isEnabled) ColorTheme.accentColor else ColorTheme.cardBG
        holder.card.setCardBackgroundColor(bgColor)
        holder.text.setTextColor(ColorTheme.textColorForBG(holder.itemView.context, bgColor))
    }

    fun updateItems(items: List<FeedFilter>) {
        this.items = items.mapTo(ArrayList()) { item -> item to (this.items.find { it.first == item && it.second } != null) }
        notifyDataSetChanged()
    }

    fun updateItems(vararg items: FeedFilter) {
        this.items = items.mapTo(ArrayList()) { item -> item to (this.items.find { it.first == item && it.second } != null) }
        notifyDataSetChanged()
    }
}