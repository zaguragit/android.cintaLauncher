package io.posidon.android.lookerupper.data.results

import android.view.View

interface SearchResult {
    val title: String
    val relevance: Relevance
    fun open(view: View)
}

@JvmInline
value class Relevance(val value: Float) {
    operator fun compareTo(other: Relevance) = value.compareTo(other.value)
}
