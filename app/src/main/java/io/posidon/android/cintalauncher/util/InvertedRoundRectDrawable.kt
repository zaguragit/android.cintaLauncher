package io.posidon.android.cintalauncher.util

import android.graphics.*
import android.graphics.drawable.Drawable

class InvertedRoundRectDrawable(
    /**
     * Array of 8 values, 4 pairs of [X,Y] radii
     */
    private val radii: FloatArray,
    /**
     * Border/padding around the transparent area
     */
    private val border: Float,
    color: Int,
    contentColor: Int? = null
) : Drawable() {

    var color = color
        set(value) {
            field = value
            paint.color = value
        }

    var contentColor = contentColor
        set(value) {
            field = value
            paint2.color = value ?: 0
        }

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        this.color = this@InvertedRoundRectDrawable.color
    }

    private var paint2: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        contentColor?.let {
            this.color = it
        }
    }

    private val path = Path()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        path.run {
            reset()
            addRoundRect(
                border,
                border,
                bounds.width().toFloat() - 2 * border,
                bounds.height().toFloat() - 2 * border,
                radii,
                Path.Direction.CW
            )
        }
    }

    override fun draw(canvas: Canvas) {
        path.fillType = Path.FillType.INVERSE_EVEN_ODD

        val s = canvas.save()
        canvas.clipPath(path)
        canvas.drawPaint(paint)

        contentColor?.let {
            canvas.restoreToCount(s)
            path.fillType = Path.FillType.EVEN_ODD
            canvas.clipPath(path)
            canvas.drawPaint(paint2)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}