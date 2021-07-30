package io.posidon.android.cintalauncher.ui.view.scrollbar.alphabet

import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer

class AlphabetSectionIndexer : HighlightSectionIndexer {
    private var savedSections = emptyArray<Char>()
    private var adapter: AppDrawerAdapter? = null

    override fun getSections(): Array<Char> = savedSections
    override fun getSectionForPosition(i: Int): Int = adapter?.let { savedSections.indexOf(it.items[i].label[0].uppercaseChar()) } ?: 0
    override fun getPositionForSection(i: Int): Int {
        return adapter?.items?.indexOfFirst { it.label[0] == savedSections[i] } ?: 0
    }

    fun updateSections(adapter: AppDrawerAdapter, appSections: List<List<App>>) {
        this.adapter = adapter
        savedSections = Array(appSections.size) { appSections[it][0].label[0].uppercaseChar() }
    }

    private var highlightI = -1
    override fun getHighlightI() = highlightI

    override fun highlight(i: Int) {
        val oldI = highlightI
        highlightI = i
        if (oldI != i) {
            adapter?.notifyDataSetChanged()
        }
    }

    override fun unhighlight() {
        highlightI = -1
        adapter?.notifyDataSetChanged()
    }

    override fun isDimmed(app: App): Boolean =
        highlightI != -1 && adapter != null && adapter!!.items[highlightI].label[0] != app.label[0].uppercaseChar()
}