package io.posidon.android.lookerupper.ui.viewHolders.instantAnswer

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.lookerupper.data.results.InstantAnswerResult
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.ui.viewHolders.SearchViewHolder

class AnswerSearchViewHolder(itemView: View) : SearchViewHolder(itemView) {

    val card = itemView.findViewById<CardView>(R.id.card)!!
    val title = card.findViewById<TextView>(R.id.title)!!
    val description = card.findViewById<TextView>(R.id.description)!!

    val infoBoxAdapter = InfoBoxAdapter()
    val infoBox = itemView.findViewById<RecyclerView>(R.id.info_box)!!.apply {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = infoBoxAdapter
    }

    val actionsContainer = itemView.findViewById<CardView>(R.id.actions_container)!!
    val sourceAction = actionsContainer.findViewById<TextView>(R.id.source)!!
    val searchAction = actionsContainer.findViewById<TextView>(R.id.search)!!
    val actionSeparator = actionsContainer.findViewById<View>(R.id.separator)!!

    override fun onBind(result: SearchResult) {
        result as InstantAnswerResult

        card.setCardBackgroundColor(ColorTheme.cardBG)
        title.setTextColor(ColorTheme.cardTitle)
        description.setTextColor(ColorTheme.cardDescription)

        title.text = result.title
        description.text = result.description
        sourceAction.text = itemView.context.getString(R.string.read_more_at_source, result.sourceName)

        val bg = ColorTheme.actionButtonBG(ColorTheme.accentColor)
        val fg = ColorTheme.actionButtonFG(bg)
        val hint = ColorTheme.hintColorForBG(itemView.context, bg)
        actionsContainer.setCardBackgroundColor(bg)
        sourceAction.setTextColor(fg)
        searchAction.setTextColor(fg)
        actionSeparator.setBackgroundColor(hint)

        sourceAction.setOnClickListener(result::open)
        searchAction.setOnClickListener(result::search)

        if (result.infoTable == null) {
            infoBox.isVisible = false
        } else {
            infoBox.isVisible = true
            infoBoxAdapter.updateEntries(result.infoTable)
        }
    }
}