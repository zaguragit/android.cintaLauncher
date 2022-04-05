package io.posidon.android.cintalauncher.ui.pinned

import android.app.Activity
import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.LauncherContext
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.ui.pinned.viewHolders.DropTargetViewHolder
import io.posidon.android.cintalauncher.ui.pinned.viewHolders.PinnedViewHolder
import io.posidon.android.cintalauncher.ui.pinned.viewHolders.bindDropTargetViewHolder
import io.posidon.android.cintalauncher.ui.pinned.viewHolders.bindPinnedViewHolder
import io.posidon.android.conveniencelib.getNavigationBarHeight

class PinnedItemsAdapter(
    launcherActivity: Activity,
    val launcherContext: LauncherContext,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> DropTargetViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.pinned_drop_target, parent, false) as ImageView)
            else -> PinnedViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.pinned_item, parent, false) as ImageView)
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
        val item = items[adapterPositionToI(ii)]
        holder as PinnedViewHolder
        bindPinnedViewHolder(
            holder,
            item,
            navbarHeight,
            onDragStart = {
                val i = adapterPositionToI(holder.adapterPosition)
                items.removeAt(i)
                dropTargetIndex = holder.adapterPosition
                notifyItemChanged(holder.adapterPosition)
                updatePins(it)
            },
        )
    }

    fun updateItems(items: List<LauncherItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
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
        if (i != dropTargetIndex) throw IllegalStateException("PinnedItemsAdapter -> i = $i, dropTargetIndex = $dropTargetIndex")
        val item = launcherContext.appManager.parseLauncherItem(clipData.getItemAt(0).text.toString())!!
        items.add(i, item)
        dropTargetIndex = -1
        notifyDataSetChanged()
        updatePins(v)
    }
}