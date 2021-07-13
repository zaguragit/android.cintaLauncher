package io.posidon.android.lookerupper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.ContactResult
import io.posidon.android.lookerupper.data.results.InstantAnswerResult
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.ui.viewHolders.AnswerSearchViewHolder
import io.posidon.android.lookerupper.ui.viewHolders.AppSearchViewHolder
import io.posidon.android.lookerupper.ui.viewHolders.ContactSearchViewHolder
import io.posidon.android.lookerupper.ui.viewHolders.SearchViewHolder

class SearchAdapter : RecyclerView.Adapter<SearchViewHolder>() {
    private var results = emptyList<SearchResult>()

    override fun getItemViewType(i: Int): Int {
        return when (results[i]) {
            is AppResult -> RESULT_APP
            is ContactResult -> RESULT_CONTACT
            is InstantAnswerResult -> RESULT_ANSWER
            else -> throw Exception("Invalid search result")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return when (viewType) {
            RESULT_APP -> AppSearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_drawer_item, parent, false))
            RESULT_CONTACT -> ContactSearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_result_contact, parent, false))
            RESULT_ANSWER -> AnswerSearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_result_answer, parent, false))
            else -> throw Exception("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: SearchViewHolder, i: Int) {
        holder.onBind(results[i])
    }

    override fun getItemCount(): Int = results.size

    fun update(results: List<SearchResult>) {
        this.results = results
        notifyDataSetChanged()
    }

    companion object {
        const val RESULT_APP = 0
        const val RESULT_CONTACT = 1
        const val RESULT_ANSWER = 2
    }
}
