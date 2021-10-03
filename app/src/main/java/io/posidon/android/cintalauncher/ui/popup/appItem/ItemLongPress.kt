package io.posidon.android.cintalauncher.ui.popup.appItem

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.content.res.ColorStateList
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
    fun makePopupWindow(context: Context, item: LauncherItem, backgroundColor: Int, textColor: Int, extraPopupWindow: PopupWindow?, onInfo: (View) -> Unit): PopupWindow {
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
        window.setOnDismissListener {
            extraPopupWindow?.dismiss()
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

        currentPopup = window

        return window
    }

    fun makeExtraPopupWindow(context: Context, shortcuts: List<ShortcutInfo>, backgroundColor: Int, textColor: Int): PopupWindow {
        val content = LayoutInflater.from(context).inflate(R.layout.long_press_item_popup_extra, null)
        if (shortcuts.isNotEmpty()) {
            val recyclerView = content.findViewById<RecyclerView>(R.id.recycler)
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ShortcutAdapter(shortcuts, textColor)
        }
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)

        content.findViewById<CardView>(R.id.card).setCardBackgroundColor(backgroundColor)

        window.isFocusable = false

        return window
    }

    fun onItemLongPress(
        view: View,
        backgroundColor: Int,
        textColor: Int,
        item: LauncherItem,
        navbarHeight: Int,
    ) {
        currentPopup?.dismiss()
        val context = view.context
        context.vibrate(14)
        val (x, y, gravity) = PopupUtils.getPopupLocationFromView(view, navbarHeight)
        val dynamicShortcuts = (item as? App)?.getDynamicShortcuts(context.getSystemService(LauncherApps::class.java))?.let {
            it.subList(0, it.size.coerceAtMost(5))
        }
        val hasDynamicShortcuts = !dynamicShortcuts.isNullOrEmpty()
        val extraPopupWindow = if (hasDynamicShortcuts) makeExtraPopupWindow(context, dynamicShortcuts!!, backgroundColor, textColor) else null
        val popupWindow = makePopupWindow(context, item, backgroundColor, textColor, extraPopupWindow) {
            item.showProperties(view, backgroundColor, textColor)
        }
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(view, gravity, x, y + (view.resources.getDimension(R.dimen.item_card_margin) * 2).toInt())

        if (hasDynamicShortcuts) popupWindow.contentView.post {
            extraPopupWindow!!.showAtLocation(view, gravity, x, y + popupWindow.contentView.height + (view.resources.getDimension(R.dimen.item_card_margin) * 4).toInt())
        }

        val shadow = View.DragShadowBuilder(view)
        val clipData = ClipData(
            item.label,
            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
            ClipData.Item(item.toString()))

        view.startDragAndDrop(clipData, shadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
    }
}