package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import android.content.Context
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.Relevance
import io.posidon.android.lookerupper.data.results.SearchResult
import java.util.*
import kotlin.collections.ArrayList

class AppProvider(
    searcher: Searcher
) : SearchProvider {

    class AppCollection(size: Int) : AppLoader.AppCollection<AppResult> {
        val list = ArrayList<AppResult>(size)

        override fun add(context: Context, app: AppResult) {
            list += app
        }

        override fun finalize(context: Context) {
            list.sortWith { a, b ->
                a.title.compareTo(b.title)
            }
        }
    }

    val appLoader = AppLoader(::AppResult, ::AppCollection)
    var apps = emptyList<AppResult>()

    override fun Activity.onCreate() {
        appLoader.async(this) {
            apps = it.list
        }
    }

    override fun getResults(query: SearchQuery): List<SearchResult> {
        val results = LinkedList<SearchResult>()
        apps.forEach {
            it.relevance = Relevance(FuzzySearch.tokenSortPartialRatio(query.toString(), it.title) / 100f)
            if (it.relevance.value > .7f) {
                results += it
            }
            it.relevance = Relevance(it.relevance.value + .3f)
        }
        return results
    }
}