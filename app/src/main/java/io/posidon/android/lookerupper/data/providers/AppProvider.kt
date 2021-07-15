package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserHandle
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.Relevance
import io.posidon.android.lookerupper.data.results.SearchResult
import io.posidon.android.lookerupper.data.results.ShortcutResult
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class AppProvider(
    searcher: Searcher
) : SearchProvider {

    class AppCollection(size: Int) : AppLoader.AppCollection<AppResult> {
        val list = ArrayList<AppResult>(size)
        val shortcuts = LinkedList<ShortcutResult>()

        override fun add(context: Context, app: AppResult) {
            list += app
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            shortcuts.addAll(app.getShortcuts(launcherApps).map {
                ShortcutResult(
                    it,
                    (it.longLabel ?: it.shortLabel).toString(),
                    launcherApps.getShortcutIconDrawable(it, context.resources.displayMetrics.densityDpi),
                    app
                )
            })
        }

        override fun finalize(context: Context) {
            list.sortWith { a, b ->
                a.title.compareTo(b.title)
            }
            shortcuts.sortWith { a, b ->
                a.title.compareTo(b.title)
            }
        }
    }

    private fun makeAppResult(packageName: String, name: String, profile: UserHandle, label: String, icon: Drawable) = AppResult(App(packageName, name, profile, label, icon))

    val appLoader = AppLoader(::makeAppResult, ::AppCollection)
    var apps = emptyList<AppResult>()
    var shortcuts = emptyList<ShortcutResult>()

    override fun Activity.onCreate() {
        appLoader.async(this) {
            apps = it.list
            shortcuts = it.shortcuts
        }
    }

    override fun getResults(query: SearchQuery): List<SearchResult> {
        val results = LinkedList<SearchResult>()
        apps.forEach {
            val r = FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f
            if (r > .7f) {
                results += it
            }
            it.relevance = Relevance(r.coerceAtLeast(0.98f))
        }
        shortcuts.forEach {
            val l = FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f
            val a = FuzzySearch.tokenSortPartialRatio(query.toString(), it.app.title) / 100f
            val r = (a * a * .5f + l * l).pow(.3f)
            if (r > .8f) {
                it.relevance = Relevance(if (l >= .95) r.coerceAtLeast(0.98f) else r.coerceAtMost(0.9f))
                results += it
            }
        }
        return results
    }
}