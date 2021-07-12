package io.posidon.android.lookerupper.data.providers

import android.app.Activity
import io.posidon.android.lookerupper.data.SearchQuery
import io.posidon.android.lookerupper.data.results.SearchResult

interface SearchProvider {

    fun Activity.onCreate()
    fun getResults(query: SearchQuery): List<SearchResult>
}