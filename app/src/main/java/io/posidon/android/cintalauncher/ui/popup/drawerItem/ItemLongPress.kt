package io.posidon.android.cintalauncher.ui.popup.drawerItem

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.pm.LauncherApps
import android.content.res.ColorStateList
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.data.items.showProperties
import io.posidon.android.cintalauncher.ui.popup.PopupUtils
import posidon.android.conveniencelib.vibrate

object ItemLongPress {

    var currentPopup: PopupWindow? = null
    fun makePopupWindow(context: Context, item: LauncherItem, backgroundColor: Int, textColor: Int, onInfo: (View) -> Unit): PopupWindow {
        val content = LayoutInflater.from(context).inflate(R.layout.long_press_item_popup, null)
        if (item is App) {
            val shortcuts = item.getStaticShortcuts(context.getSystemService(LauncherApps::class.java))
            if (shortcuts.isNotEmpty()) {
                val recyclerView = content.findViewById<RecyclerView>(R.id.recycler)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ShortcutAdapter(shortcuts, textColor)
            }
        }
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        currentPopup = window
        window.setOnDismissListener {
            currentPopup = null
        }

        val propertiesButton = content.findViewById<View>(R.id.properties_item)
        val propertiesText = propertiesButton.findViewById<TextView>(R.id.properties_text)
        val propertiesIcon = propertiesButton.findViewById<ImageView>(R.id.properties_icon)

        propertiesText.setTextColor(textColor)
        propertiesIcon.imageTintList = ColorStateList.valueOf(textColor)

        content.findViewById<CardView>(R.id.card).setCardBackgroundColor(backgroundColor)

        propertiesButton.setOnClickListener {
            window.dismiss()
            onInfo(it)
        }

        return window
    }

    fun onItemLongPress(
        view: View,
        backgroundColor: Int,
        textColor: Int,
        item: LauncherItem,
        navbarHeight: Int,
        onDragStart: (view: View) -> Unit = {},
        onDragOut: (view: View) -> Unit = {},
        isRemoveHandled: Boolean = false,
    ) {
        if (currentPopup == null) {
            val context = view.context
            context.vibrate(14)
            val (x, y, gravity) = PopupUtils.getPopupLocationFromView(view, navbarHeight)
            val popupWindow = makePopupWindow(context, item, backgroundColor, textColor) {
                item.showProperties(view, backgroundColor, textColor)
            }
            popupWindow.isFocusable = false
            popupWindow.showAtLocation(view, gravity, x, y + (view.resources.getDimension(R.dimen.item_card_margin) * 2).toInt())

            view.setOnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_EXITED -> {
                        currentPopup?.dismiss()
                        onDragOut(v)
                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (!isRemoveHandled) v.visibility = View.INVISIBLE
                    }
                    DragEvent.ACTION_DRAG_ENDED,
                    DragEvent.ACTION_DROP -> {
                        if (!isRemoveHandled) v.visibility = View.VISIBLE
                        currentPopup?.isFocusable = true
                        currentPopup?.update()
                    }
                }
                true
            }

            val shadow = View.DragShadowBuilder(view)
            val clipData = ClipData(
                item.label,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                ClipData.Item(item.toString()))

            view.startDragAndDrop(clipData, shadow, null, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)

            onDragStart(view)
        }
    }
}