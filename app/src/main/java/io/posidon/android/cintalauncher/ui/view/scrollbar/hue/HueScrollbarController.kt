package io.posidon.android.cintalauncher.ui.view.scrollbar.hue

import android.content.Context
import android.graphics.Canvas
import androidx.core.graphics.ColorUtils
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.providers.app.AppCollection
import io.posidon.android.cintalauncher.ui.drawer.AppDrawerAdapter
import io.posidon.android.cintalauncher.ui.view.scrollbar.Scrollbar
import io.posidon.android.cintalauncher.ui.view.scrollbar.ScrollbarController
import java.util.*

class HueScrollbarController(
    scrollbar: Scrollbar
) : ScrollbarController(scrollbar) {

    private val paint by scrollbar::paint

    override fun draw(canvas: Canvas) {
        val insideHeight = scrollbar.height - scrollbar.paddingTop - scrollbar.paddingBottom.toFloat()
        val insideWidth = scrollbar.width - scrollbar.paddingLeft - scrollbar.paddingRight.toFloat()
        val (a, b) = if (scrollbar.orientation == Scrollbar.VERTICAL) {
            insideHeight / indexer.sections.lastIndex.toFloat() to insideWidth / 2f + scrollbar.paddingLeft
        } else {
            insideWidth / indexer.sections.lastIndex.toFloat() to insideHeight / 2f + scrollbar.paddingTop
        }
        for (i in indexer.sections.indices) {
            val (x, y) = if (scrollbar.orientation == Scrollbar.VERTICAL) {
                b to a * i + scrollbar.paddingTop
            } else {
                a * i + scrollbar.paddingLeft to b
            }
            val isHighlighted = showSelection && i >= scrollbar.currentScrolledSectionStart && i <= scrollbar.currentScrolledSectionEnd
            val hsl = floatArrayOf(indexer.sections[i], 1f, if (isHighlighted) 0.4f else 0.3f)
            paint.color = ColorUtils.HSLToColor(hsl)
            val r = if (isHighlighted) insideHeight / 3f * 2f else insideHeight / 3f
            canvas.drawCircle(x, y, r, paint)
        }
    }

    override fun updateTheme(context: Context) {
        scrollbar.invalidate()
    }

    var step = 0f
    override fun loadSections(apps: AppCollection) {
        apps.list.sortBy {
            it.hsl[0]
        }
        var startHue = apps.list[0].hsl[0]
        step = (apps.list.last().hsl[0] - startHue) / 32f
        var section = LinkedList<App>().also { apps.sections.add(it) }
        for (app in apps.list) {
            val currentHue = app.hsl[0]
            if (currentHue - startHue < step) {
                section.add(app)
            }
            else section = LinkedList<App>().apply {
                add(app)
                apps.sections.add(this)
                startHue = currentHue
            }
        }
    }

    override fun createSectionHeaderItem(
        items: LinkedList<AppDrawerAdapter.DrawerItem>,
        section: List<App>
    ) {}

    override val indexer = HueSectionIndexer(this)

    override fun updateAdapterIndexer(adapter: AppDrawerAdapter, appSections: List<List<App>>) {
        indexer.updateSections(adapter, appSections)
        adapter.indexer = this.indexer
    }
}