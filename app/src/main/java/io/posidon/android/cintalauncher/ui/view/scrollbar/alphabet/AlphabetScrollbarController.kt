package io.posidon.android.cintalauncher.ui.view.scrollbar.alphabet

import android.content.Context
import android.graphics.Canvas
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.drawer.viewHolders.SectionHeaderItem
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import io.posidon.android.cintalauncher.ui.view.scrollbar.ScrollbarController
import posidon.android.conveniencelib.dp
import java.util.*

class AlphabetScrollbarController(
    scrollbar: Scrollbar
) : ScrollbarController(scrollbar) {

    private val paint by scrollbar::paint

    var textColor = 0x88ffffff.toInt()
        set(value) {
            field = value
            paint.color = value
        }

    init {
        paint.color = textColor
    }

    var highlightColor = 0

    override fun draw(canvas: Canvas) {
        if (this.indexer.sections.isNotEmpty()) {
            val insideHeight = scrollbar.height - scrollbar.paddingTop - scrollbar.paddingBottom
            val insideWidth = scrollbar.width - scrollbar.paddingLeft - scrollbar.paddingRight
            val (a, b) = if (scrollbar.orientation == Scrollbar.VERTICAL) {
                insideHeight / this.indexer.sections.lastIndex.toFloat() to insideWidth / 2f + scrollbar.paddingLeft
            } else {
                insideWidth / this.indexer.sections.lastIndex.toFloat() to (insideHeight + paint.textSize) / 2f + scrollbar.paddingTop
            }
            for (i in this.indexer.sections.indices) {
                val (x, y) = if (scrollbar.orientation == Scrollbar.VERTICAL) {
                    b to a * i + scrollbar.paddingTop
                } else {
                    a * i + scrollbar.paddingLeft to b
                }
                if (showSelection && i >= scrollbar.currentScrolledSectionStart && i <= scrollbar.currentScrolledSectionEnd) {
                    val tmp = paint.typeface
                    paint.typeface = scrollbar.boldTypeface
                    paint.color = highlightColor
                    canvas.drawText(this.indexer.sections[i].toString(), x, y, paint)
                    paint.color = textColor
                    paint.typeface = tmp
                } else canvas.drawText(this.indexer.sections[i].toString(), x, y, paint)
            }
        }
    }

    override fun updateTheme(context: Context) {
        paint.apply {
            textSize = context.dp(16)
        }
        highlightColor = ColorTheme.accentColor
        scrollbar.invalidate()
    }

    override fun loadSections(apps: AppCollection) {
        var currentChar = apps.list[0].label[0].uppercaseChar()
        var currentSection = LinkedList<App>().also { apps.sections.add(it) }
        for (app in apps.list) {
            if (app.label.startsWith(currentChar, ignoreCase = true)) {
                currentSection.add(app)
            }
            else currentSection = LinkedList<App>().apply {
                add(app)
                apps.sections.add(this)
                currentChar = app.label[0].uppercaseChar()
            }
        }
    }

    override fun createSectionHeaderItem(
        items: LinkedList<AppDrawerAdapter.DrawerItem>,
        section: List<App>
    ) {
        items.add(SectionHeaderItem(section[0].label[0].uppercaseChar().toString()))
    }

    override val indexer = AlphabetSectionIndexer()

    override fun updateAdapterIndexer(adapter: AppDrawerAdapter, appSections: List<List<App>>) {
        indexer.updateSections(adapter, appSections)
        adapter.indexer = this.indexer
    }
}