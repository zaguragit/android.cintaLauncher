package io.posidon.android.cintalauncher.ui.drawer

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.*
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer
import java.util.*

class AppDrawerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HighlightSectionIndexer {

    interface DrawerItem {
        val label: String
        fun getItemViewType(): Int
    }

    private var items: Array<DrawerItem> = emptyArray()

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(i: Int) = items[i].getItemViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SECTION_HEADER -> SectionHeaderViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_drawer_section_header, parent, false))
            APP_ITEM -> AppViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_drawer_item, parent, false) as CardView)
            else -> throw RuntimeException("Invalid view holder type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val item = items[i]
        when (holder.itemViewType) {
            SECTION_HEADER -> bindSectionHeaderViewHolder(holder as SectionHeaderViewHolder, item as SectionHeaderItem, highlightI == i)
            APP_ITEM -> bindAppViewHolder(holder as AppViewHolder, (item as AppItem).item, if (highlightI == -1) null else items[highlightI].label[0])
        }
    }

    fun updateAppSections(appSections: List<List<App>>, activity: Activity) {
        val newItems = LinkedList<DrawerItem>()
        for (section in appSections) {
            newItems.add(SectionHeaderItem(section[0].label[0].uppercaseChar().toString()))
            section.mapTo(newItems) { AppItem(it) }
        }
        items = newItems.toTypedArray()
        savedSections = Array(appSections.size) { appSections[it][0].label[0].uppercaseChar() }
        activity.runOnUiThread(::notifyDataSetChanged)
    }

    private var savedSections = emptyArray<Char>()
    override fun getSections(): Array<Char> = savedSections
    override fun getSectionForPosition(i: Int): Int = savedSections.indexOf(items[i].label[0].uppercaseChar())
    override fun getPositionForSection(i: Int): Int {
        return items.indexOfFirst { it.label[0] == savedSections[i] }
    }

    private var highlightI = -1

    override fun highlight(i: Int) {
        val oldI = highlightI
        highlightI = i
        if (oldI != i) {
            notifyDataSetChanged()
        }
    }

    override fun unhighlight() {
        highlightI = -1
        notifyDataSetChanged()
    }

    companion object {
        const val SECTION_HEADER = 0
        const val APP_ITEM = 1
    }
}