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
        swipeableLayout.reset()
        title.text = item.title
        applyIfNotNull(description, item.description, TextView::setText)
        applyIfNotNull(icon, item.sourceIcon, ImageView::setImageDrawable)
        applyIfNotNull(source, item.source, TextView::setText)
        itemView.setOnClickListener(item::onTap)
        icon.imageTintList = if (item.shouldTintIcon) ColorStateList.valueOf(color) else null
        if (item.actions.isEmpty()) {
            actionsContainer.isVisible = false
        } else {
            actionsContainer.isVisible = true
            val bg = ColorTheme.actionButtonBG(item.color.let { if (it == 0) ColorTheme.accentColor else it })
            actionsContainer.setCardBackgroundColor(bg)
            val fg = ColorTheme.titleColorForBG(itemView.context, bg)
            actions.adapter = ActionsAdapter(item.actions, fg)
            separatorDrawable.setColor(ColorTheme.hintColorForBG(itemView.context, bg))
        }
        if (item.instant == Instant.MAX) {
            time.isVisible = false
        } else {
            time.isVisible = true
            time.text = item.formatTimeAgo(itemView.resources)
        }
        swipeableLayout.onSwipeAway = item::onDismiss
        swipeableLayout.isSwipeable = item.isDismissible
        source.setTextColor(color)
        title.setTextColor(ColorTheme.uiTitle)
        description.setTextColor(ColorTheme.uiDescription)
        time.setTextColor(ColorTheme.uiDescription)
        val bg = (ColorTheme.uiBG and 0xff000000.toInt()) or (ColorTheme.accentColor and 0x00ffffff)
        swipeableLayout.setSwipeColor(bg)
        swipeableLayout.setIconColor(ColorTheme.titleColorForBG(itemView.context, bg))
        separator.setBackgroundColor(ColorTheme.uiHint and 0x00ffffff or 0x24ffffff)
    }
}