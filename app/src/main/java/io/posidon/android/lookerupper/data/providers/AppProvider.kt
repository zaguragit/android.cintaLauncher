package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.providers.feed.suggestions.SuggestionsManager
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.launcherutils.IconConfig
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class AppProvider(
    val searcher: Searcher
) : SearchProvider {

    class Collection(
        size: Int,
        val settings: Settings
    ) : AppLoader.AppCollection<AppCollection.ExtraIconData> {

        val list = ArrayList<AppResult>(size)
        val staticShortcuts = LinkedList<ShortcutResult>()
        val dynamicShortcuts = LinkedList<ShortcutResult>()

        override fun addApp(
            context: Context,
            packageName: String,
            name: String,
            profile: UserHandle,
            label: String,
            icon: Drawable,
            extra: AppLoader.ExtraAppInfo<AppCollection.ExtraIconData>,
        ) {
            val app = AppResult(AppCollection.createApp(
                packageName,
                name,
                profile,
                label,
                icon,
                extra,
                settings
            ))
            list += app
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            staticShortcuts.addAll(app.getStaticShortcuts(launcherApps).map {
                ShortcutResult(
                    it,
                    (it.longLabel ?: it.shortLabel).toString(),
                    launcherApps.getShortcutIconDrawable(it, context.resources.displayMetrics.densityDpi) ?: ColorDrawable(),
                    app
                )
            })
            dynamicShortcuts.addAll(app.getDynamicShortcuts(launcherApps).map {
                ShortcutResult(
                    it,
                    (it.longLabel ?: it.shortLabel).toString(),
                    launcherApps.getShortcutIconDrawable(it, context.resources.displayMetrics.densityDpi) ?: ColorDrawable(),
                    app
                )
            })
        }

        override fun finalize(context: Context) {
            list.sortWith { a, b ->
                a.title.compareTo(b.title)
            }
        }

        override fun modifyIcon(
            icon: Drawable,
            expandableBackground: Drawable?
        ): Pair<Drawable, AppCollection.ExtraIconData> {
            return AppCollection.modifyIcon(icon, expandableBackground, settings)
        }
    }

    val appLoader = AppLoader { Collection(it, searcher.settings) }
    var apps = emptyList<AppResult>()
    var staticShortcuts = emptyList<ShortcutResult>()
    var dynamicShortcuts = emptyList<ShortcutResult>()

    override fun Activity.onCreate() {
        val iconConfig = IconConfig(
            size = (resources.displayMetrics.density * 128f).toInt(),
            density = resources.configuration.densityDpi,
            packPackages = searcher.settings.getStrings("icon_packs") ?: emptyArray(),
        )

        appLoader.async(this, iconConfig) {
            apps = it.list
            staticShortcuts = it.staticShortcuts
            dynamicShortcuts = it.dynamicShortcuts
        }
    }

    override fun getResults(query: SearchQuery): List<SearchResult> {
        val results = LinkedList<SearchResult>()
        val suggestions = SuggestionsManager.getSuggestions().let { it.subList(0, it.size.coerceAtMost(6)) }
        apps.forEach {
            val i = suggestions.indexOf(it.app)
            val suggestionFactor = if(i == -1) 0f else (suggestions.size - i).toFloat() / suggestions.size
            val r = FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f + suggestionFactor * 0.5f
            if (r > .8f) {
                results += if (results.size < 6) {
                    it.relevance = Relevance(r.times(2f).coerceAtLeast(1f))
                    it
                } else {
                    it.relevance = Relevance(r.coerceAtLeast(0.98f))
                    CompactAppResult(it.app)
                }
            }
        }
        staticShortcuts.forEach {
            val l = FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f
            val a = FuzzySearch.tokenSortPartialRatio(query.toString(), it.app.title) / 100f
            val r = (a * a * .5f + l * l).pow(.2f)
            if (r > .95f) {
                it.relevance = Relevance(l)
                results += it
            }
        }
        dynamicShortcuts.forEach {
            val l = FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f
            val a = FuzzySearch.tokenSortPartialRatio(query.toString(), it.app.title) / 100f
            val r = (a * a * .2f + l * l).pow(.3f)
            if (r > .9f) {
                it.relevance = Relevance(if (l >= .95) r.coerceAtLeast(0.98f) else r.coerceAtMost(0.9f))
                results += it
            }
        }
        return results
    }
}