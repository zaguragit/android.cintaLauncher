package io.posidon.android.lookerupper.data.providers

import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.Searcher
import io.posidon.android.lookerupper.data.results.SearchResult

abstract class AsyncSearchProvider(
    val searcher: Searcher
) : SearchProvider {

    val lastResults = HashMap<SearchQuery, List<SearchResult>>()

    override fun getResults(query: SearchQuery): List<SearchResult> {
        return lastResults.getOrElse(query) {
            loadResults(query)
            emptyList()
        }
    }

    abstract fun loadResults(query: SearchQuery)

    fun update(query: SearchQuery, results: List<SearchResult>) {
        lastResults[query] = results
        searcher.query(query.text)
    }
}