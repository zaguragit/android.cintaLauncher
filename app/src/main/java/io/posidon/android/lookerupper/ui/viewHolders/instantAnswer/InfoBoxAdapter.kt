package io.posidon.android.lookerupper.ui.viewHolders.instantAnswer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme

class InfoBoxAdapter : RecyclerView.Adapter<InfoboxEntryViewHolder>() {

    private var entries: List<Pair<String, String>> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InfoboxEntryViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_answer_info_box_entry, parent, false))

    override fun onBindViewHolder(holder: InfoboxEntryViewHolder, i: Int) {
        val e = entries[i]
        holder.label.text = e.first
        holder.value.text = e.second

        holder.label.setTextColor(ColorTheme.cardTitle)
        holder.value.setTextColor(ColorTheme.cardDescription)
        holder.separator.setBackgroundColor(ColorTheme.hintColorForBG(holder.itemView.context, ColorTheme.cardBG))
    }

    override fun getItemCount() = entries.size

    fun updateEntries(entries: List<Pair<String, String>>) {
        this.entries = entries
        notifyDataSetChanged()
    }
}
