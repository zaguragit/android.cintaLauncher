package io.posidon.android.cintalauncher.ui.feed.home.pinned

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.AppViewHolder
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.bindAppViewHolder
import posidon.android.conveniencelib.getNavigationBarHeight

class PinnedItemsAdapter(
    launcherActivity: LauncherActivity,
    val launcherContext: LauncherContext,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val suggestionsManager = launcherContext.appManager.suggestionsManager
    val navbarHeight = launcherActivity.getNavigationBarHeight()

    private var dropTargetIndex = -1

    private var items: MutableList<LauncherItem> = ArrayList()

    override fun getItemCount(): Int = iToAdapterPosition(items.size)

    override fun getItemViewType(i: Int): Int {
        return if (dropTargetIndex == i) 1 else 0
    }

    fun adapterPositionToI(position: Int): Int {
        return when {
            dropTargetIndex == -1 -> position
            dropTargetIndex <= position -> position - 1
            else -> position
        }
    }

    fun iToAdapterPosition(i: Int): Int {
        return when {
            dropTargetIndex == -1 -> i
            dropTargetIndex <= i -> i + 1
            else -> i
        }
    }

    private var dropTarget: DropTargetViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> DropTargetViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_home_drop_target, parent, false) as CardView, map)
                .also { dropTarget = it }
            else -> AppViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_card, parent, false) as CardView, map)
        }
    }

    private fun updatePins(v: View) {
        launcherContext.appManager.setPinned(v.context, ArrayList(items))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, ii: Int) {
        if (ii == dropTargetIndex) {
            holder as DropTargetViewHolder
            bindDropTargetViewHolder(holder)
            return
        }
        val i = adapterPositionToI(ii)
        val item = items[i]
        holder as AppViewHolder
        bindAppViewHolder(
            holder,
            item,
            false,
            suggestionsManager,
            navbarHeight,
            onDragStart = {
                items.removeAt(i)
                dropTargetIndex = ii
                notifyItemChanged(ii)
                updatePins(it)
            },
        )
    }

    fun updateItems(items: List<LauncherItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    val map = HashMap<LauncherItem, () -> Unit>()

    fun onScroll() {
        map.forEach {
            it.value()
        }
    }

    fun showDropTarget(i: Int) {
        if (i != dropTargetIndex) {
            when {
                i == -1 -> {
                    val old = dropTargetIndex
                    dropTargetIndex = -1
                    notifyItemRemoved(old)
                }
                dropTargetIndex == -1 -> {
                    dropTargetIndex = i
                    notifyItemInserted(i)
                }
                else -> {
                    val old = dropTargetIndex
                    dropTargetIndex = i
                    notifyItemMoved(old, i)
                }
            }
        }
    }

    fun onDrop(v: View, i: Int, clipData: ClipData) {
        val item = launcherContext.appManager.parseLauncherItem(clipData.getItemAt(0).text.toString())!!
        items.add(i, item)
        dropTargetIndex = -1
        notifyDataSetChanged()
        updatePins(v)
    }
}