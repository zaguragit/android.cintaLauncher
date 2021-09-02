package io.posidon.android.lookerupper.data.results

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.ui.popup.drawerItem.ItemLongPress
import posidon.android.conveniencelib.getNavigationBarHeight

class CompactAppResult(
    val app: App
) : CompactResult() {

    inline val packageName: String get() = app.packageName
    inline val name: String get() = app.name
    override val title: String get() = app.label
    override val icon: Drawable get() = app.icon

    override val subtitle get() = null

    override var relevance = Relevance(0f)
    override val onLongPress = { v: View, activity: Activity ->
        val backgroundColor = ColorTheme.tintAppDrawerItem(getColor())
        ItemLongPress.onItemLongPress(
            v,
            backgroundColor,
            ColorTheme.titleColorForBG(v.context, backgroundColor),
            app,
            activity.getNavigationBarHeight(),
            onDragOut = { activity.finish() }
        )
        true
    }

    inline fun getColor(): Int = app.getColor()

    override fun open(view: View) {
        app.open(view.context, view)
    }
}