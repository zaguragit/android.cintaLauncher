package io.posidon.android.cintalauncher.ui.popup.drawerItem

import android.content.Context
import android.content.pm.LauncherApps
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
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
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.vibrate

object ItemLongPress {

    var currentPopup: PopupWindow? = null
    fun makePopupWindow(context: Context, item: LauncherItem, backgroundColor: Int, textColor: Int, onInfo: (View) -> Unit): PopupWindow {
        val content = LayoutInflater.from(context).inflate(R.layout.long_press_item_popup, null)
        if (item is App) {
            val shortcuts = item.getShortcuts(context.getSystemService(LauncherApps::class.java))
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
        window.setBackgroundDrawable(ColorDrawable(0x0))
        window.elevation = content.dp(12)

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

    inline fun onItemLongPress(
        context: Context,
        backgroundColor: Int,
        textColor: Int,
        view: View,
        item: LauncherItem,
        navbarHeight: Int,
    ) {
        if (currentPopup == null) {
            context.vibrate(14)
            val (x, y, gravity) = getPopupLocationFromView(view, navbarHeight)
            val popupWindow = makePopupWindow(context, item, backgroundColor, textColor) {
                item.showProperties(view, backgroundColor, textColor)
            }
            popupWindow.showAtLocation(view, gravity, x, y)
        }
    }

    /**
     * @return Triple(x, y, gravity)
     */
    inline fun getPopupLocationFromView(
        view: View,
        navbarHeight: Int,
    ): Triple<Int, Int, Int> {

        val location = IntArray(2).also {
            view.getLocationOnScreen(it)
        }

        var gravity: Int

        val screenWidth = Device.screenWidth(view.context)
        val screenHeight = Device.screenHeight(view.context)

        val x = if (location[0] > screenWidth / 2) {
            gravity = Gravity.END
            screenWidth - location[0] - view.measuredWidth
        } else {
            gravity = Gravity.START
            location[0]
        }

        val y = if (location[1] < screenHeight / 2) {
            gravity = gravity or Gravity.TOP
            location[1] + view.measuredHeight
        } else {
            gravity = gravity or Gravity.BOTTOM
            screenHeight - location[1] + navbarHeight
        }

        return Triple(x, y, gravity)
    }
}