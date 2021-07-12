package io.posidon.android.cintalauncher.providers.app

import android.content.Context
import io.posidon.android.cintalauncher.BuildConfig
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.launcherutils.AppLoader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AppCollection(appCount: Int) : AppLoader.AppCollection<App> {
    val list = ArrayList<App>(appCount)
    val byName = HashMap<String, MutableList<App>>()
    val sections = LinkedList<List<App>>()

    inline operator fun get(i: Int) = list[i]
    inline val size get() = list.size

    override fun add(context: Context, app: App) {
        if (app.packageName == BuildConfig.APPLICATION_ID &&
            app.name == LauncherActivity::class.java.name)
            return
        list.add(app)
        putInMap(app)
    }

    private fun putInMap(app: App) {
        val list = byName[app.packageName]
        if (list == null) {
            byName[app.packageName] = arrayListOf(app)
            return
        }
        val thisAppI = list.indexOfFirst {
            it.name == app.name && it.userHandle.hashCode() == app.userHandle.hashCode()
        }
        if (thisAppI == -1) {
            list.add(app)
            return
        }
        list[thisAppI] = app
    }

    override fun finalize(context: Context) {
        list.sortWith { o1, o2 ->
            o1.label.compareTo(o2.label, ignoreCase = true)
        }

        var currentChar = list[0].label[0].uppercaseChar()
        var currentSection = LinkedList<App>().also { sections.add(it) }
        for (app in list) {
            if (app.label.startsWith(currentChar, ignoreCase = true)) {
                currentSection.add(app)
            }
            else currentSection = LinkedList<App>().apply {
                add(app)
                sections.add(this)
                currentChar = app.label[0].uppercaseChar()
            }
        }
    }
}