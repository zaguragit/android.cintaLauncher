package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import io.posidon.android.cintalauncher.BuildConfig
import io.posidon.android.libduckduckgo.DuckDuckGo
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.InstantAnswerResult

class DuckDuckGoProvider(searcher: Searcher) : AsyncSearchProvider(searcher) {

    override fun Activity.onCreate() {
    }

    override fun loadResults(query: SearchQuery) {
        if (query.length >= 3) {
            val q = query.toString()
            DuckDuckGo.instantAnswer(q, BuildConfig.APPLICATION_ID) {
                update(query, listOf(
                    InstantAnswerResult(
                        query,
                        it.title,
                        it.description,
                        it.sourceName,
                        it.sourceUrl,
                        DuckDuckGo.searchURL(q, BuildConfig.APPLICATION_ID),
                        it.infoTable?.filter { a -> a.dataType == "string" }?.map { a -> a.label + ':' to a.value }?.takeIf(List<*>::isNotEmpty)
                    )
                ))
            }
        }
    }
}