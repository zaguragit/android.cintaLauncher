package io.posidon.android.cintalauncher.ui.feed.home

import android.content.Intent
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.providers.summary.NotificationSummariesProvider
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.feed.home.summary.SummaryAdapter
import io.posidon.android.cintalauncher.util.InvertedRoundRectDrawable
import io.posidon.android.lookerupper.ui.SearchActivity
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.getStatusBarHeight

class HomeViewHolder(
    scrollIndicator: ImageView,
    parentView: ViewGroup,
    val launcherActivity: LauncherActivity,
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    val summaryCard = itemView.findViewById<View>(R.id.summary_card)!!
    val summaryAdapter = SummaryAdapter()
    val summaryRecycler = summaryCard.findViewById<RecyclerView>(R.id.summary_recycler)!!.apply {
        layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
        adapter = summaryAdapter
    }
    val recentlyOpenedAdapter = RecentlyOpenedItemsAdapter(launcherActivity)
    val recentlyOpenedRecycler = summaryCard.findViewById<RecyclerView>(R.id.recents_recycler)!!.apply {
        layoutManager = GridLayoutManager(itemView.context, 3, RecyclerView.VERTICAL, false)
        adapter = recentlyOpenedAdapter
    }

    val searchCard = itemView.findViewById<CardView>(R.id.search_bar_container)!!.apply {
        setOnClickListener {
            val context = it.context
            context.startActivity(Intent(context, SearchActivity::class.java))
        }
    }
    val searchIcon = itemView.findViewById<ImageView>(R.id.search_bar_icon)!!
    val searchText = itemView.findViewById<TextView>(R.id.search_bar_text)!!

    init {
        NotificationSummariesProvider.init(itemView.context) {
            itemView.post(::updateRecents)
        }
        itemView.layoutParams.apply {
            height = parentView.measuredHeight - itemView.context.getStatusBarHeight()
        }
        val s = itemView.dp(24).toInt()
        val vertical = itemView.findViewById<LinearLayout>(R.id.vertical)
        vertical.addView(scrollIndicator, 4, LinearLayout.LayoutParams(s, s).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        val r = itemView.context.resources.getDimension(R.dimen.dock_corner_radius)
        itemView.background = InvertedRoundRectDrawable(
            floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), 0f, 0)
    }

    fun updateSummary() {
        val ns = NotificationSummariesProvider.get()
        if (ns.isEmpty()) {
            summaryRecycler.isVisible = false
        } else {
            summaryRecycler.isVisible = true
            summaryAdapter.updateSummaries(ns)
        }
    }

    fun updateRecents() {
        val recent = launcherActivity.suggestionsManager.getLast3(launcherActivity)
        if (recent.isEmpty()) {
            recentlyOpenedRecycler.isVisible = false
        } else {
            recentlyOpenedRecycler.isVisible = true
            recentlyOpenedAdapter.updateItems(recent.toTypedArray())
        }
    }
}

fun bindHomeViewHolder(
    holder: HomeViewHolder
) {
    holder.updateSummary()
    holder.updateRecents()
    (holder.itemView.background as InvertedRoundRectDrawable).color = ColorTheme.uiBG
    holder.recentlyOpenedAdapter.notifyDataSetChanged()
    holder.searchCard.setCardBackgroundColor(ColorTheme.searchBarBG)
    holder.searchIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
    holder.searchText.setTextColor(ColorTheme.searchBarFG)
}