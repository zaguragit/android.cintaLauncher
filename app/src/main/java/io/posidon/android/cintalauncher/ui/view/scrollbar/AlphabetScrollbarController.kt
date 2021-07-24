package io.posidon.android.cintalauncher.ui.view.scrollbar

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.ui.view.HighlightSectionIndexer
import posidon.android.conveniencelib.dp
import kotlin.math.roundToInt

class AlphabetScrollbarController(
    scrollbar: AlphabetScrollbar
) : ScrollbarController(scrollbar) {

    private val paint by scrollbar::paint

    var sectionIndexer: HighlightSectionIndexer? = recycler?.adapter.let {
        if (it is HighlightSectionIndexer && it.sections.isArrayOf<Char>())
            it else null
    }

    var textColor = 0
        set(value) {
            field = value
            paint.color = value
        }

    var highlightColor = 0

    override fun draw(canvas: Canvas) {
        if (sectionIndexer != null && sectionIndexer!!.sections.isNotEmpty()) {
            val insideHeight = scrollbar.height - scrollbar.paddingTop - scrollbar.paddingBottom
            val insideWidth = scrollbar.width - scrollbar.paddingLeft - scrollbar.paddingRight
            val (a, b) = if (scrollbar.orientation == AlphabetScrollbar.VERTICAL) {
                insideHeight / sectionIndexer!!.sections.lastIndex.toFloat() to insideWidth / 2f + scrollbar.paddingLeft
            } else {
                insideWidth / sectionIndexer!!.sections.lastIndex.toFloat() to (insideHeight + paint.textSize) / 2f + scrollbar.paddingTop
            }
            for (i in sectionIndexer!!.sections.indices) {
                val (x, y) = if (scrollbar.orientation == AlphabetScrollbar.VERTICAL) {
                    b to a * i + scrollbar.paddingTop
                } else {
                    a * i + scrollbar.paddingLeft to b
                }
                if (showSelection && i >= currentScrolledSectionStart && i <= currentScrolledSectionEnd) {
                    val tmp = paint.typeface
                    paint.typeface = scrollbar.boldTypeface
                    paint.color = highlightColor
                    canvas.drawText(sectionIndexer!!.sections[i].toString(), x, y, paint)
                    paint.color = textColor
                    paint.typeface = tmp
                } else canvas.drawText(sectionIndexer!!.sections[i].toString(), x, y, paint)
            }
        }
    }

    private fun RecyclerView.getCurrentPositionStart() : Int {
        return (this.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()
    }

    private fun RecyclerView.getCurrentPositionEnd() : Int {
        return (this.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
    }

    var currentScrolledSectionStart = -1
        private set
    var currentScrolledSectionEnd = -1
        private set

    override val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            currentScrolledSectionStart = recyclerView.getCurrentPositionStart().let { sectionIndexer?.getSectionForPosition(it) } ?: -1
            currentScrolledSectionEnd = recyclerView.getCurrentPositionEnd().let { sectionIndexer?.getSectionForPosition(it) } ?: -1
            scrollbar.invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val i = coordsToIndex(event.x, event.y)
                sectionIndexer?.getPositionForSection(i)?.let {
                    recycler?.scrollToPosition(it)
                    sectionIndexer?.highlight(it)
                }
                scrollbar.invalidate()
            }
            MotionEvent.ACTION_DOWN -> {
                scrollbar.onStartScroll(scrollbar)
                scrollbar.parent.requestDisallowInterceptTouchEvent(true)
                updateAdapter()
                val i = coordsToIndex(event.x, event.y)
                sectionIndexer?.getPositionForSection(i)?.let {
                    recycler?.scrollToPosition(it)
                    sectionIndexer?.highlight(it)
                }
                scrollbar.invalidate()
            }
            MotionEvent.ACTION_UP -> {
                sectionIndexer?.unhighlight()
            }
            MotionEvent.ACTION_CANCEL -> {
                sectionIndexer?.unhighlight()
                scrollbar.onCancelScroll(scrollbar)
            }
        }
        return true
    }

    override fun updateAdapter() {
        sectionIndexer = recycler?.adapter.let {
            if (it is HighlightSectionIndexer && it.sections.isArrayOf<Char>()) it else null
        }
    }

    override fun updateTheme(context: Context) {
        paint.apply {
            textSize = context.dp(16)
        }
        scrollbar.invalidate()
        textColor = 0x88ffffff.toInt()
        highlightColor = ColorTheme.accentColor
    }

    fun coordsToIndex(x: Float, y: Float): Int {
        if (scrollbar.orientation == AlphabetScrollbar.VERTICAL) {
            val out = ((y - scrollbar.paddingTop) / (scrollbar.height - scrollbar.paddingTop - scrollbar.paddingBottom) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        } else {
            val out = ((x - scrollbar.paddingLeft) / (scrollbar.width - scrollbar.paddingLeft - scrollbar.paddingRight) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        }
    }
}