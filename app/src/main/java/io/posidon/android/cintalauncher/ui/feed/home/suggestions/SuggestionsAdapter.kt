package io.posidon.android.cintalauncher.ui.feed.home.suggestions

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.LauncherItem
import posidon.android.conveniencelib.getNavigationBarHeight

class SuggestionsAdapter(
    activity: Activity,
    launcherContext: LauncherContext,
) : RecyclerView.Adapter<SuggestionViewHolder>() {

    val suggestionsManager = launcherContext.appManager.suggestionsManager
    val navbarHeight = activity.getNavigationBarHeight()

    private var items: List<LauncherItem> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        return SuggestionViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_home_suggestion, parent, false) as CardView, map)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, i: Int) {
        val item = items[i]
        bindSuggestionViewHolder(holder, item, suggestionsManager, navbarHeight)
    }

    fun updateItems(items: List<LauncherItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    val map = HashMap<LauncherItem, () -> Unit>()

    fun onScroll() {
        map.forEach {
            it.value()
        }
    }
}