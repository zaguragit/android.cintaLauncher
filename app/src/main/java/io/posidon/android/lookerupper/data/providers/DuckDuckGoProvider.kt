package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import android.content.Context
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import io.posidon.android.launcherutils.AppLoader
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.AppResult
import io.posidon.android.lookerupper.data.results.InstantAnswerResult
import io.posidon.android.lookerupper.data.results.Relevance
import io.posidon.android.lookerupper.data.results.SearchResult
import posidon.android.loader.DuckInstantAnswer
import java.util.*
import kotlin.collections.ArrayList

class DuckDuckGoProvider(searcher: Searcher) : AsyncSearchProvider(searcher) {

    override fun Activity.onCreate() {
    }

    override fun loadResults(query: SearchQuery) {
        if (query.length >= 3) {
            DuckInstantAnswer.load(query.toString(), "cintalauncher") {
                println(it.title + "\n" + it.sourceUrl)
                update(query, listOf(
                    InstantAnswerResult(
                        query,
                        it.title,
                        it.description,
                        it.sourceName,
                        it.sourceUrl,
                        it.searchUrl
                    )
                ))
            }
        }
    }
}