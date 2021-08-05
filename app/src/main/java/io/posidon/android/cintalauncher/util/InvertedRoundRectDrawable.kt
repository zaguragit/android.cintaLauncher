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
    outerColor: Int,
    innerColor: Int? = null
) : Drawable() {

    var outerColor = outerColor
        set(value) {
            field = value
            outerPaint.color = value
        }

    var innerColor = innerColor
        set(value) {
            field = value
            innerPaint.color = value ?: 0
        }

    val outerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        this.color = this@InvertedRoundRectDrawable.outerColor
    }

    val innerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        innerColor?.let {
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
        canvas.drawPaint(outerPaint)

        innerColor?.let {
            canvas.restoreToCount(s)
            path.fillType = Path.FillType.EVEN_ODD
            canvas.clipPath(path)
            canvas.drawPaint(innerPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        outerPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        outerPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}