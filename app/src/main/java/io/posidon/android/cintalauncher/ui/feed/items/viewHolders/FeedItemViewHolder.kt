package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.ui.color.ColorTheme
import io.posidon.android.cintalauncher.ui.feed.items.ActionsAdapter
import posidon.android.conveniencelib.dp

open class FeedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val source = itemView.findViewById<TextView>(R.id.source)
    val title = itemView.findViewById<TextView>(R.id.title)
    val description = itemView.findViewById<TextView>(R.id.description)
    val icon = itemView.findViewById<ImageView>(R.id.icon)
    val card = itemView.findViewById<CardView>(R.id.card)
    val actions = itemView.findViewById<RecyclerView>(R.id.actions_recycler).apply {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }
    val actionButtonShape = run {
        val r = itemView.dp(128)
        RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
    }
}

fun bindFeedItemViewHolder(
    holder: FeedItemViewHolder,
    item: FeedItem,
    color: Int
) {
    holder.title.text = item.title
    applyIfNotNull(holder.icon, item.sourceIcon, ImageView::setImageDrawable)
    applyIfNotNull(holder.source, item.source, TextView::setText)
    applyIfNotNull(holder.description, item.description, TextView::setText)
    holder.itemView.setOnClickListener(item::onTap)
    holder.icon.imageTintList = if (item.shouldTintIcon) ColorStateList.valueOf(color) else null
    if (item.actions.isEmpty()) {
        holder.actions.isVisible = false
    } else {
        holder.actions.isVisible = true
        val actionsBG = ShapeDrawable(holder.actionButtonShape.clone()).apply {
            paint.color = ColorTheme.actionButtonBG(color)
        }
        holder.actions.adapter = ActionsAdapter(item.actions, ColorTheme.actionButtonFG(item.color.let { if (it == 0) holder.itemView.context.getColor(R.color.accent) else it }), actionsBG)
    }
    styleFeedItemViewHolder(holder, color)
}

fun styleFeedItemViewHolder(
    holder: FeedItemViewHolder,
    color: Int,
) {
    holder.source.setTextColor(color)
    holder.itemView.setBackgroundColor(ColorTheme.feedBG)
    holder.card.setCardBackgroundColor(ColorTheme.feedCardBG)
    holder.title.setTextColor(ColorTheme.feedCardTitle)
    holder.description.setTextColor(ColorTheme.feedCardDescription)
}

inline fun <T: View, R> applyIfNotNull(view: T, value: R, block: (T, R) -> Unit) {
    if (value == null) {
        view.isVisible = false
    } else {
        view.isVisible = true
        block(view, value)
    }
}