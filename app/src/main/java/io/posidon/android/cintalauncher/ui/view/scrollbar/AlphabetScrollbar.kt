package io.posidon.android.cintalauncher.ui.view.scrollbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import io.posidon.android.cintalauncher.R

open class AlphabetScrollbar : View {

    var controller: ScrollbarController = AlphabetScrollbarController(this)

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

    @Orientation
    var orientation: Int = HORIZONTAL

    var onStartScroll: (View) -> Unit = {}
    var onCancelScroll: (View) -> Unit = {}

    val paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    val boldTypeface: Typeface

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        controller.draw(canvas)
    }

    override fun performClick(): Boolean {
        onStartScroll(this)
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return controller.onTouchEvent(event)
    }

    companion object {
        @IntDef(VERTICAL, HORIZONTAL)
        annotation class Orientation

        const val VERTICAL = 0
        const val HORIZONTAL = 1
    }
}