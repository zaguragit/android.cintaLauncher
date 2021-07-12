package io.posidon.android.cintalauncher.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.dp
import kotlin.math.roundToInt


open class AlphabetScrollbar : View {

    constructor(c: Context) : super(c) {
        boldTypeface = Typeface.create(paint.typeface, Typeface.BOLD)
    }
    constructor(c: Context, a: AttributeSet?) : this(c, a, 0, 0)
    constructor(c: Context, a: AttributeSet?, da: Int) : this(c, a, da, 0)
    constructor(c: Context, a: AttributeSet?, da: Int, dr: Int) : super(c, a, da, dr) {
        if (a != null) {
            val typedArray = c.obtainStyledAttributes(a, R.styleable.TextAppearance, da, dr)
            val fontFamilyId: Int =
                typedArray.getResourceId(R.styleable.TextAppearance_android_fontFamily, 0)
            if (fontFamilyId > 0) {
                paint.typeface = ResourcesCompat.getFont(context, fontFamilyId)
            }
            typedArray.recycle()
        }
        boldTypeface = Typeface.create(paint.typeface, Typeface.BOLD)
    }

    var orientation: Int = HORIZONTAL

    var recycler: RecyclerView? = null
        set(value) {
            field?.removeOnScrollListener(onScrollListener)
            field = value
            value?.addOnScrollListener(onScrollListener)
        }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            currentScrolledSectionStart = recyclerView.getCurrentPositionStart().let { sectionIndexer?.getSectionForPosition(it) } ?: -1
            currentScrolledSectionEnd = recyclerView.getCurrentPositionEnd().let { sectionIndexer?.getSectionForPosition(it) } ?: -1
            this@AlphabetScrollbar.invalidate()
        }
    }

    var onStartScroll: (View) -> Unit = {}
    var onCancelScroll: (View) -> Unit = {}

    var showVisibleLetter = true
        set(value) {
            field = value
            invalidate()
        }

    var textColor = 0
        set(value) {
            field = value
            paint.color = value
        }

    var highlightColor = 0

    fun updateAdapter() {
        sectionIndexer = recycler?.adapter.let {
            if (it is HighlightSectionIndexer && it.sections.isArrayOf<Char>()) it else null
        }
    }

    fun updateTheme() {
        paint.apply {
            textSize = context.dp(16)
        }
        invalidate()
    }

    private val paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val boldTypeface: Typeface

    private var sectionIndexer: HighlightSectionIndexer? = recycler?.adapter.let {
        if (it is HighlightSectionIndexer && it.sections.isArrayOf<Char>())
            it else null
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (sectionIndexer != null && sectionIndexer!!.sections.isNotEmpty()) {
            val insideHeight = height - paddingTop - paddingBottom
            val insideWidth = width - paddingLeft - paddingRight
            val (a, b) = if (orientation == VERTICAL) {
                insideHeight / sectionIndexer!!.sections.lastIndex.toFloat() to insideWidth / 2f + paddingLeft
            } else {
                insideWidth / sectionIndexer!!.sections.lastIndex.toFloat() to (insideHeight + paint.textSize) / 2f + paddingTop
            }
            for (i in sectionIndexer!!.sections.indices) {
                val (x, y) = if (orientation == VERTICAL) {
                    b to a * i + paddingTop
                } else {
                    a * i + paddingLeft to b
                }
                if (showVisibleLetter && i >= currentScrolledSectionStart && i <= currentScrolledSectionEnd) {
                    val tmp = paint.typeface
                    paint.typeface = boldTypeface
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

    private var currentScrolledSectionStart = -1
    private var currentScrolledSectionEnd = -1

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val i = coordsToIndex(event.x, event.y)
                sectionIndexer?.getPositionForSection(i)?.let {
                    recycler?.scrollToPosition(it)
                    sectionIndexer?.highlight(it)
                }
                invalidate()
            }
            MotionEvent.ACTION_DOWN -> {
                onStartScroll(this)
                parent.requestDisallowInterceptTouchEvent(true)
                updateAdapter()
                val i = coordsToIndex(event.x, event.y)
                sectionIndexer?.getPositionForSection(i)?.let {
                    recycler?.scrollToPosition(it)
                    sectionIndexer?.highlight(it)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                sectionIndexer?.unhighlight()
            }
            MotionEvent.ACTION_CANCEL -> {
                sectionIndexer?.unhighlight()
                onCancelScroll(this)
            }
        }
        return true
    }

    private fun coordsToIndex(x: Float, y: Float): Int {
        if (orientation == VERTICAL) {
            val out = ((y - paddingTop) / (height - paddingTop - paddingBottom) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        } else {
            val out = ((x - paddingLeft) / (width - paddingLeft - paddingRight) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        }
    }

    companion object {
        @IntDef(VERTICAL, HORIZONTAL)
        annotation class Orientation

        const val VERTICAL = 0
        const val HORIZONTAL = 1
    }
}