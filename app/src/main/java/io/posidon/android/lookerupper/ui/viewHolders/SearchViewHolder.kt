package io.posidon.android.lookerupper.ui.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.lookerupper.data.results.SearchResult

abstract class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun onBind(result: SearchResult)
}