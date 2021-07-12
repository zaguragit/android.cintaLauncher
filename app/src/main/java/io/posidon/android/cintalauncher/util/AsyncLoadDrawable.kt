package io.posidon.android.cintalauncher.util

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import kotlin.concurrent.thread

class AsyncLoadDrawable : Drawable(), Drawable.Callback {

    private var mCurrDrawable: Drawable? = ColorDrawable()

    fun setDelegate(delegate: Drawable) {
        mCurrDrawable = delegate
        delegate.bounds = bounds
        if (delegate is Animatable) {
            delegate.callback = this
            delegate.setVisible(true, true)
            (delegate as Animatable).start()
            Log.d("ALD", "Started the bugger")
        }
    }

    fun onLoad(delegate: Drawable) {
        setDelegate(delegate)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        Log.d(TAG, "Asked to draw " + mCurrDrawable + " " + canvas.clipBounds)
        mCurrDrawable!!.draw(canvas)
    }

    override fun onBoundsChange(bounds: Rect) {
        if (mCurrDrawable != null) {
            mCurrDrawable!!.bounds = bounds
        }
    }

    override fun setChangingConfigurations(configs: Int) {
        mCurrDrawable!!.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int = mCurrDrawable!!.changingConfigurations

    override fun setDither(dither: Boolean) = mCurrDrawable!!.setDither(dither)

    override fun setFilterBitmap(filter: Boolean) {
        mCurrDrawable!!.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        mCurrDrawable!!.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mCurrDrawable!!.colorFilter = cf
    }

    override fun setColorFilter(color: Int, mode: PorterDuff.Mode) {
        mCurrDrawable!!.setColorFilter(color, mode)
    }

    override fun clearColorFilter() = mCurrDrawable!!.clearColorFilter()

    override fun isStateful(): Boolean = true

    override fun getIntrinsicWidth(): Int =
        if (mCurrDrawable != null) mCurrDrawable!!.intrinsicWidth else -1

    override fun getIntrinsicHeight(): Int =
        if (mCurrDrawable != null) mCurrDrawable!!.intrinsicHeight else -1

    override fun getMinimumWidth(): Int =
        if (mCurrDrawable != null) mCurrDrawable!!.minimumWidth else 0

    override fun getMinimumHeight(): Int =
        if (mCurrDrawable != null) mCurrDrawable!!.minimumHeight else 0

    override fun onStateChange(state: IntArray): Boolean = if (mCurrDrawable != null) {
        mCurrDrawable!!.setState(state)
    } else false

    override fun onLevelChange(level: Int): Boolean = if (mCurrDrawable != null) {
        mCurrDrawable!!.setLevel(level)
    } else false

    override fun getState(): IntArray = mCurrDrawable!!.state

    override fun getCurrent(): Drawable = mCurrDrawable!!

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        mCurrDrawable!!.setVisible(visible, restart)

    override fun getOpacity(): Int = mCurrDrawable!!.opacity

    override fun getTransparentRegion(): Region? = mCurrDrawable!!.transparentRegion

    override fun getPadding(padding: Rect): Boolean = mCurrDrawable!!.getPadding(padding)

    override fun mutate(): Drawable = mCurrDrawable!!.mutate()

    @Throws(XmlPullParserException::class, IOException::class)
    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet) =
        mCurrDrawable!!.inflate(r, parser, attrs)

    override fun getConstantState(): ConstantState? = mCurrDrawable!!.constantState

    override fun toString(): String = TAG + mCurrDrawable.toString()

    // Callback Methods called from the delegate
    override fun invalidateDrawable(who: Drawable) {
        if (who === mCurrDrawable) {
            Log.d("ALD", "invalidateDrawable who=$who")
            invalidateSelf()
        }
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        if (who === mCurrDrawable) {
            Log.d("ALD", "scheduleDrawable who=$who what=$what when=$`when`")
            scheduleSelf(what, `when`)
        }
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        if (who === mCurrDrawable) {
            Log.d("ALD", "unscheduleDrawable who=$who what=$what")
            unscheduleSelf(what)
        }
    }

    companion object {
        fun load(function: () -> Drawable): AsyncLoadDrawable {
            return AsyncLoadDrawable().also {
                thread(name = "AsyncLoadDrawable loading thread", isDaemon = true) {
                    it.onLoad(function())
                }
            }
        }

        private const val TAG = "ALD"
    }
}