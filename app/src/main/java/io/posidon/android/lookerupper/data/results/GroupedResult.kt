package io.posidon.android.lookerupper.data.results

import android.view.View

class GroupedResult(
    val children: List<SearchResult>
) : SearchResult {

    override val title: String
        get() = children[0].title

    override var relevance = Relevance(children.maxOf { it.relevance.value })
    override fun open(view: View) {}
}