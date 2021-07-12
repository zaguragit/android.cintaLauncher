package io.posidon.android.cintalauncher.data.items

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import io.posidon.android.cintalauncher.storage.Settings
import java.util.*
import kotlin.collections.HashMap

interface LauncherItem {

    val icon: Drawable
    val label: String

    fun getColor(): Int = -0xdad9d9

    /**
     * What to do when the item is clicked
     * [view] The view that was clicked
     */
    fun open(context: Context, view: View?)

    /**
     * The number to show in the notification badge
     * If value is 0, the badge won't be shown
     */
    val notificationCount get() = 0

    /**
     * Text representation of the item, used to save it
     */
    override fun toString(): String

    companion object {
        var last3 = LinkedList<LauncherItem>()
            private set

        fun itemHasOpened(item: LauncherItem) {
            last3.removeAll { it == item }
            last3.addFirst(item)
            while (last3.size > 3) last3.removeLast()
        }

        fun loadSavedRecents(settings: Settings, appsByName: HashMap<String, MutableList<App>>) {
            settings.getStrings("stats:recently_opened")?.let {
                val last3 = LinkedList<LauncherItem>()
                it.forEach {
                    parse(it, appsByName)?.let { it1 -> last3.add(it1) }
                }
                this.last3 = last3
            }
        }

        fun parse(string: String, appsByName: HashMap<String, MutableList<App>>): LauncherItem? {
            return App.parse(string, appsByName)
        }
    }
}

fun LauncherItem.showProperties(view: View, backgroundColor: Int, textColor: Int) {
    if (this is App) {
        view.context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:$packageName")))
    }
}