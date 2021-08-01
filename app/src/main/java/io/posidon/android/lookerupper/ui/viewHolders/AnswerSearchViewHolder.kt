package io.posidon.android.lookerupper.ui.viewHolders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.lookerupper.data.results.InstantAnswerResult
import io.posidon.android.lookerupper.data.results.SearchResult
import posidon.android.conveniencelib.dp

class AnswerSearchViewHolder(itemView: View) : SearchViewHolder(itemView) {

    val card = itemView.findViewById<CardView>(R.id.card)!!
    val icon = card.findViewById<ImageView>(R.id.icon)!!.apply {
        setImageResource(R.drawable.ic_info)
    }
    val title = card.findViewById<TextView>(R.id.title)!!
    val description = card.findViewById<TextView>(R.id.description)!!
    val source = card.findViewById<TextView>(R.id.source)!!

    val actionContainer = itemView.findViewById<View>(R.id.action_container)!!
    val actionIcon = actionContainer.findViewById<View>(R.id.action_icon)!!.apply {
        isVisible = false
    }
    val actionText = actionContainer.findViewById<TextView>(R.id.action_text)!!

    override fun onBind(result: SearchResult) {
        result as InstantAnswerResult
        //icon.setImageDrawable(result.icon)

        source.setTextColor(ColorTheme.cardDescription)
        card.setCardBackgroundColor(ColorTheme.cardBG)
        title.setTextColor(ColorTheme.cardTitle)
        description.setTextColor(ColorTheme.cardDescription)
        icon.imageTintList = ColorStateList.valueOf(ColorTheme.cardDescription)

        title.text = result.title
        description.text = result.description
        source.text = result.sourceName

        itemView.setOnClickListener(result::open)

        actionContainer.setOnClickListener(result::search)
        actionText.setText(R.string.search_in_duckduckgo)

        actionContainer.background = generateActionButtonBG(itemView.context, ColorTheme.cardBG)
        actionText.setTextColor(ColorTheme.cardDescription)
    }

    fun generateActionButtonBG(context: Context, color: Int): Drawable {
        return ShapeDrawable(run {
            val r = context.dp(128)
            RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
        }).apply {
            paint.color = ColorTheme.actionButtonBG(color)
        }
    }
}