package io.posidon.android.lookerupper.ui.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.cintalauncher.R
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.data.results.SimpleResult

class SimpleSearchViewHolder(itemView: View) : SearchViewHolder(itemView) {

    val icon = itemView.findViewById<ImageView>(R.id.icon)!!
    val text = itemView.findViewById<TextView>(R.id.text)!!

    override fun onBind(result: SearchResult) {
        result as SimpleResult
        icon.setImageDrawable(result.icon)
        text.text = result.title
        itemView.setOnClickListener(result::open)
    }
}