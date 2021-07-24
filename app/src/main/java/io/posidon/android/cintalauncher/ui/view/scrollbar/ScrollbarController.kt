package io.posidon.android.cintalauncher.ui.view.scrollbar

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

abstract class ScrollbarController(val scrollbar: AlphabetScrollbar) {
    abstract fun draw(canvas: Canvas)
    abstract fun onTouchEvent(event: MotionEvent): Boolean

    var recycler: RecyclerView? = null
        set(value) {
            field?.removeOnScrollListener(onScrollListener)
            field = value
            value?.addOnScrollListener(onScrollListener)
        }

    abstract val onScrollListener: RecyclerView.OnScrollListener

    fun destroy() {
        recycler?.removeOnScrollListener(onScrollListener)
    }


    var showSelection = true
        set(value) {
            field = value
            scrollbar.invalidate()
        }

    abstract fun updateAdapter()
    abstract fun updateTheme(context: Context)
}