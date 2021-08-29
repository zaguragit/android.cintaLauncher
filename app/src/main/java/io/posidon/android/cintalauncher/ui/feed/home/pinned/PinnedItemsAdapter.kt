package io.posidon.android.cintalauncher.ui.feed.home.pinned

import android.view.DragEvent
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
                .apply {
                    itemView.setOnDragListener { v, event ->
                        when (event.action) {
                            DragEvent.ACTION_DRAG_ENDED,
                            DragEvent.ACTION_DRAG_EXITED, -> {
                                dropTargetIndex = items.size
                                notifyDataSetChanged()
                            }
                            DragEvent.ACTION_DROP -> {
                                val item = launcherContext.appManager.parseLauncherItem(event.clipData.getItemAt(0).text.toString())!!
                                items.add(dropTargetIndex, item)
                                dropTargetIndex = -1
                                notifyDataSetChanged()
                                updatePins(v)
                            }
                        }
                        true
                    }
                }
            else -> AppViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_drawer_item, parent, false) as CardView, map)
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
            onDragOut = {
                items.removeAt(i)
                notifyItemRemoved(ii)
                updatePins(it)
            },
        )
        holder.itemView.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_LOCATION,
                DragEvent.ACTION_DRAG_ENTERED -> {
                    dropTargetIndex = holder.adapterPosition
                    notifyDataSetChanged()
                }
            }
            true
        }
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

    fun showDropTarget(show: Boolean) {
        if (show != (dropTargetIndex != -1)) {
            if (show) {
                dropTargetIndex = 0
                notifyItemInserted(0)
            } else {
                dropTargetIndex = -1
                notifyItemRemoved(0)
            }
        }
    }
}