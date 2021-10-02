package io.posidon.android.cintalauncher.ui.feed.items.viewHolders.suggestions

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.LauncherItem
import posidon.android.conveniencelib.getNavigationBarHeight

class SuggestionsAdapter(
    activity: Activity,
) : RecyclerView.Adapter<SuggestionViewHolder>() {

    val navbarHeight = activity.getNavigationBarHeight()

    private var items: List<LauncherItem> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        return SuggestionViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.smart_suggestion, parent, false) as CardView)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, i: Int) {
        val item = items[i]
        holder.onBind(item, navbarHeight)
    }

    fun updateItems(items: List<LauncherItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}