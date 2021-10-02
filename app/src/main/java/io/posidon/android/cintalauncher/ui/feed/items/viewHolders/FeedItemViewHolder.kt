package io.posidon.android.cintalauncher.ui.feed.items.viewHolders

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.data.feed.items.formatTimeAgo
import io.posidon.android.cintalauncher.ui.feed.items.ActionsAdapter
import io.posidon.android.cintalauncher.ui.view.SwipeableLayout
import io.posidon.android.cintalauncher.ui.view.recycler.DividerItemDecorator
import posidon.android.conveniencelib.dp
import java.time.Instant

open class FeedItemViewHolder(itemView: View) : FeedViewHolder(SwipeableLayout(itemView)) {
    val swipeableLayout = this.itemView as SwipeableLayout
    val container = itemView.findViewById<View>(R.id.container)!!
    val source = itemView.findViewById<TextView>(R.id.source)!!
    val separator = itemView.findViewById<View>(R.id.separator)!!
    val time = itemView.findViewById<TextView>(R.id.time)!!
    val title = itemView.findViewById<TextView>(R.id.title)!!
    val description = itemView.findViewById<TextView>(R.id.description)!!
    val icon = itemView.findViewById<ImageView>(R.id.icon)!!
    val actionsContainer = itemView.findViewById<CardView>(R.id.actions_container)!!
    val separatorDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setSize(itemView.dp(1).toInt(), 0)
    }
    val actions = actionsContainer.findViewById<RecyclerView>(R.id.actions_recycler)!!.apply {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        addItemDecoration(DividerItemDecorator(itemView.context, DividerItemDecoration.HORIZONTAL, separatorDrawable))
        setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    override fun onBind(
        item: FeedItem,
        color: Int
    ) {
        val holder = this
        holder.swipeableLayout.reset()
        holder.title.text = item.title
        applyIfNotNull(holder.description, item.description, TextView::setText)
        applyIfNotNull(holder.icon, item.sourceIcon, ImageView::setImageDrawable)
        applyIfNotNull(holder.source, item.source, TextView::setText)
        holder.itemView.setOnClickListener(item::onTap)
        holder.icon.imageTintList = if (item.shouldTintIcon) ColorStateList.valueOf(color) else null
        if (item.actions.isEmpty()) {
            holder.actionsContainer.isVisible = false
        } else {
            holder.actionsContainer.isVisible = true
            val bg = ColorTheme.actionButtonBG(item.color.let { if (it == 0) ColorTheme.accentColor else it })
            holder.actionsContainer.setCardBackgroundColor(bg)
            val fg = ColorTheme.actionButtonFG(bg)
            holder.actions.adapter = ActionsAdapter(item.actions, fg)
            holder.separatorDrawable.setColor(ColorTheme.hintColorForBG(holder.itemView.context, bg))
        }
        if (item.instant == Instant.MAX) {
            holder.time.isVisible = false
        } else {
            holder.time.isVisible = true
            holder.time.text = item.formatTimeAgo(holder.itemView.resources)
        }
        holder.swipeableLayout.onSwipeAway = item::onDismiss
        holder.swipeableLayout.isSwipeable = item.isDismissible
        holder.source.setTextColor(color)
        holder.title.setTextColor(ColorTheme.uiTitle)
        holder.description.setTextColor(ColorTheme.uiDescription)
        holder.time.setTextColor(ColorTheme.uiDescription)
        val bg = (ColorTheme.uiBG and 0xff000000.toInt()) or (ColorTheme.accentColor and 0x00ffffff)
        holder.swipeableLayout.setSwipeColor(bg)
        holder.swipeableLayout.setIconColor(ColorTheme.titleColorForBG(holder.itemView.context, bg))
        holder.separator.setBackgroundColor(ColorTheme.uiHint and 0x00ffffff or 0x24ffffff)
    }
}