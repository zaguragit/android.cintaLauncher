package io.posidon.android.cintalauncher.ui.view.scrollbar

import android.content.Context
import android.graphics.Canvas
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer
import java.util.*

abstract class ScrollbarController(val scrollbar: Scrollbar) {
    abstract fun draw(canvas: Canvas)

    abstract val indexer: HighlightSectionIndexer

    abstract fun updateTheme(context: Context)
    abstract fun loadSections(apps: AppCollection)
    abstract fun createSectionHeaderItem(
        items: LinkedList<AppDrawerAdapter.DrawerItem>,
        section: List<App>
    )

    abstract fun updateAdapterIndexer(adapter: AppDrawerAdapter, appSections: List<List<App>>)
}