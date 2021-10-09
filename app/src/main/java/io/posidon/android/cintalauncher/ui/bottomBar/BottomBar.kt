package io.posidon.android.cintalauncher.ui.bottomBar

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.content.res.ColorStateList
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.cintalauncher.ui.pinned.PinnedItemsAdapter
import io.posidon.android.cintalauncher.ui.view.SeeThoughView
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import io.posidon.android.cintalauncher.ui.view.scrollbar.ScrollbarIconView
import io.posidon.android.lookerupper.ui.SearchActivity

class BottomBar(val activity: LauncherActivity) {

    val scrollBar: Scrollbar get() = appDrawerIcon.scrollBar

    val view = activity.findViewById<CardView>(R.id.search_bar_container)!!.apply {
        setOnClickListener {
            val context = it.context
            context.startActivity(
                Intent(
                    context,
                    SearchActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        setOnDragListener(::onDrag)
    }
    private val searchIcon = view.findViewById<ImageView>(R.id.search_bar_icon)!!
    val appDrawerIcon = view.findViewById<ScrollbarIconView>(R.id.app_drawer_icon)!!.apply {
        appDrawer = activity.appDrawer
    }
    val appDrawerCloseIconContainer = activity.findViewById<CardView>(R.id.back_button_container)!!
    @SuppressLint("ClickableViewAccessibility")
    val appDrawerCloseIcon = appDrawerCloseIconContainer.findViewById<ImageView>(R.id.back_button)!!.apply {
        setOnClickListener(activity.appDrawer::close)
    }
    val blurBG = view.findViewById<SeeThoughView>(R.id.search_bar_blur_bg)!!

    val pinnedAdapter = PinnedItemsAdapter(activity, activity.launcherContext)
    val pinnedRecycler = view.findViewById<RecyclerView>(R.id.pinned_recycler).apply {
        layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        adapter = pinnedAdapter
    }

    fun updateColorTheme() {
        view.setCardBackgroundColor(ColorTheme.searchBarBG)
        appDrawerCloseIconContainer.setCardBackgroundColor(ColorTheme.searchBarBG)
        searchIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
        appDrawerCloseIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
        appDrawerIcon.imageTintList = ColorStateList.valueOf(ColorTheme.searchBarFG)
    }

    fun onAppsLoaded() {
        updatePinned()
    }

    fun showDropTarget(i: Int) {
        if (i != -1) pinnedRecycler.isVisible = true
        pinnedAdapter.showDropTarget(i)
    }

    fun getPinnedItemIndex(x: Float, y: Float): Int {
        val i = ((x - (view.width - pinnedRecycler.width) / 2) * pinnedAdapter.itemCount / pinnedRecycler.width).toInt().coerceAtLeast(-1)
        return if (i >= pinnedAdapter.itemCount) -1 else i
    }

    fun onDrop(v: View, i: Int, clipData: ClipData) {
        pinnedAdapter.onDrop(v, i, clipData)
    }

    fun updatePinned() {
        pinnedAdapter.updateItems(activity.launcherContext.appManager.pinnedItems)
    }

    fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED,
            DragEvent.ACTION_DRAG_ENTERED,
            DragEvent.ACTION_DRAG_LOCATION -> {
                val i = getPinnedItemIndex(event.x, event.y)
                val pinnedItems = activity.launcherContext.appManager.pinnedItems
                showDropTarget(if (i == -1) pinnedItems.size else i)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                updatePinned()
                showDropTarget(-1)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                showDropTarget(-1)
            }
            DragEvent.ACTION_DROP -> {
                val i = getPinnedItemIndex(event.x, event.y)
                if (i == -1)
                    return false
                onDrop(v, i, event.clipData)
            }
        }
        return true
    }
}