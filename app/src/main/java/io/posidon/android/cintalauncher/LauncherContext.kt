package io.posidon.android.cintalauncher

import android.content.Context
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.data.items.LauncherItem
import io.posidon.android.cintalauncher.providers.Feed
import io.posidon.android.cintalauncher.providers.SuggestionsManager
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.launcherutils.AppLoader

class LauncherContext {

    val feed = Feed()

    val settings = Settings()

    val appManager = AppManager()

    inner class AppManager {

        val suggestionsManager = SuggestionsManager()

        val pinnedItems: List<LauncherItem> get() = _pinnedItems

        fun <T : Context> loadApps(context: T, onEnd: T.(apps: AppCollection) -> Unit) {
            appLoader.async(
                context,
                settings.getStrings("icon_packs") ?: emptyArray()
            ) { apps: AppCollection ->
                appsByName = apps.byName
                _pinnedItems = settings.getStrings(PINNED_KEY)?.mapNotNull { LauncherItem.parse(it, appsByName) }?.toMutableList() ?: ArrayList()
                suggestionsManager.onAppsLoaded(this, context, settings)
                onEnd(context, apps)
            }
        }

        fun parseLauncherItem(string: String): LauncherItem? {
            return App.parse(string, appsByName)
        }

        fun getAppByPackage(packageName: String): LauncherItem? = appsByName[packageName]?.first()

        fun pinItem(context: Context, launcherItem: LauncherItem, i: Int) {
            _pinnedItems.add(i, launcherItem)
            settings.edit(context) {
                val s = launcherItem.toString()
                PINNED_KEY set (settings.getStrings(PINNED_KEY)
                    ?.toMutableList()
                    ?.apply { add(i, s) }
                    ?.toTypedArray()
                    ?: arrayOf(s))
            }
        }

        fun unpinItem(context: Context, i: Int) {
            _pinnedItems.removeAt(i)
            settings.edit(context) {
                PINNED_KEY set (settings.getStrings(PINNED_KEY)
                    ?.toMutableList()
                    ?.apply { removeAt(i) }
                    ?.toTypedArray()
                    ?: throw IllegalStateException("Can't unpin an item when no items are pinned"))
            }
        }

        fun setPinned(context: Context, pinned: List<LauncherItem>) {
            _pinnedItems = pinned.toMutableList()
            settings.edit(context) {
                PINNED_KEY set pinned.map(LauncherItem::toString).toTypedArray()
            }
        }

        private val appLoader = AppLoader({ packageName, name, profile, label, icon, extra ->
            App(
                packageName,
                name,
                profile,
                label,
                icon,
                extra.banner,
                settings
            )
        }, ::AppCollection)

        private var appsByName = HashMap<String, MutableList<App>>()

        private var _pinnedItems: MutableList<LauncherItem> = ArrayList()
    }

    companion object {
        private const val PINNED_KEY = "pinned_items"
    }
}