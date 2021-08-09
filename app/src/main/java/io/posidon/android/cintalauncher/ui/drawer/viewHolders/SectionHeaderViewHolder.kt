package io.posidon.android.cintalauncher.ui.drawer.viewHolders

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter.Companion.SECTION_HEADER
import io.posidon.android.cintalauncher.ui.popup.drawer.DrawerLongPressPopup
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer
import posidon.android.conveniencelib.getNavigationBarHeight

class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView = itemView.findViewById<TextView>(R.id.text)!!

    val highlightDrawable = HighlightSectionIndexer.createHighlightDrawable(
        itemView.context,
        0
    )
}

class SectionHeaderItem(override val label: String) : AppDrawerAdapter.DrawerItem {
    override fun getItemViewType() = SECTION_HEADER
}

@SuppressLint("ClickableViewAccessibility")
fun bindSectionHeaderViewHolder(
    holder: SectionHeaderViewHolder,
    item: SectionHeaderItem,
    isHighlighted: Boolean,
    launcherActivity: LauncherActivity
) {
    holder.itemView.background = if (isHighlighted) holder.highlightDrawable else null
    holder.textView.text = item.label
    holder.textView.setTextColor(ColorTheme.appDrawerSectionColor)
    holder.highlightDrawable.paint.color = ColorTheme.accentColor and 0xffffff or 0x55000000

    var x = 0f
    var y = 0f
    holder.itemView.setOnTouchListener { _, e ->
        when (e.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                x = e.rawX
                y = e.rawY
            }
        }
        false
    }
    holder.itemView.setOnLongClickListener {
        DrawerLongPressPopup.show(holder.itemView, x, y, launcherActivity.getNavigationBarHeight(), launcherActivity.settings, launcherActivity::reloadScrollbarController, launcherActivity::loadApps)
        true
    }
}