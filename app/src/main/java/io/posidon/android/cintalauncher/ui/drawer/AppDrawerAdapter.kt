package io.posidon.android.cintalauncher.ui.drawer

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.*
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer
import io.posidon.android.cintalauncher.ui.view.scrollbar.ScrollbarController
import posidon.android.conveniencelib.getNavigationBarHeight
import java.util.*

class AppDrawerAdapter(
    val launcherActivity: LauncherActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var indexer: HighlightSectionIndexer? = null

    interface DrawerItem {
        val label: String
        fun getItemViewType(): Int
    }

    var items: Array<DrawerItem> = emptyArray()

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(i: Int) = items[i].getItemViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SECTION_HEADER -> SectionHeaderViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_drawer_section_header, parent, false))
            APP_ITEM -> AppViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.app_card, parent, false) as CardView, map)
            else -> throw RuntimeException("Invalid view holder type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val item = items[i]
        when (holder.itemViewType) {
            SECTION_HEADER -> bindSectionHeaderViewHolder(
                holder as SectionHeaderViewHolder,
                item as SectionHeaderItem,
                indexer?.getHighlightI() == i,
                launcherActivity
            )
            APP_ITEM -> bindAppViewHolder(
                holder as AppViewHolder,
                (item as AppItem).item,
                indexer?.isDimmed(item.item) ?: false,
                launcherActivity.getNavigationBarHeight(),
                onDragOut = { launcherActivity.appDrawer.close(it) }
            )
        }
    }

    fun updateAppSections(
        appSections: List<List<App>>,
        activity: Activity,
        controller: ScrollbarController
    ) {
        val newItems = LinkedList<DrawerItem>()
        for (section in appSections) {
            controller.createSectionHeaderItem(newItems, section)
            section.mapTo(newItems) { AppItem(it) }
        }
        items = newItems.toTypedArray()
        controller.updateAdapterIndexer(this, appSections)
        activity.runOnUiThread(::notifyDataSetChanged)
    }

    val map = HashMap<LauncherItem, () -> Unit>()

    fun onScroll() {
        map.forEach {
            it.value()
        }
    }

    companion object {
        const val SECTION_HEADER = 0
        const val APP_ITEM = 1
    }
}