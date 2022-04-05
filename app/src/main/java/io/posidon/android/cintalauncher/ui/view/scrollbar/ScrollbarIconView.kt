package io.posidon.android.cintalauncher.ui.view.scrollbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.providers.color.pallete.ColorPalette
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.storage.ScrollbarControllerSetting
import io.posidon.android.cintalauncher.storage.ScrollbarControllerSetting.scrollbarController
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.drawer.AppDrawer
import io.posidon.android.cintalauncher.ui.view.scrollbar.alphabet.AlphabetScrollbarController
import io.posidon.android.cintalauncher.ui.view.scrollbar.hue.HueScrollbarController
import io.posidon.android.conveniencelib.Device
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.getNavigationBarHeight
import io.posidon.android.conveniencelib.units.toFloatPixels
import io.posidon.android.conveniencelib.units.toPixels
import kotlin.math.abs

@SuppressLint("AppCompatCustomView")
class ScrollbarIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    var appDrawer: AppDrawer? = null

    val scrollBar = Scrollbar(context)

    private var currentWindow: PopupWindow? = null
    @Scrollbar.Orientation
    private var currentOrientation = Scrollbar.HORIZONTAL

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> appDrawer?.open(this)
            MotionEvent.ACTION_MOVE -> {
                val d = abs(e.x / e.y)
                val orientation = if (d > 1f) Scrollbar.HORIZONTAL else Scrollbar.VERTICAL
                if (currentWindow == null) {
                    showPopup(orientation)
                    currentOrientation = orientation
                } else {
                    if ((d > 2f || d < 0.5f) && e.y * e.y + e.x * e.x > width * height && (e.y < 0 || currentOrientation == Scrollbar.VERTICAL) && currentOrientation != orientation) {
                        currentWindow?.dismiss()
                        showPopup(orientation)
                        currentOrientation = orientation
                    }
                    currentWindow?.let { it.contentView.onTouchEvent(makeMotionEventForPopup(it.contentView, e)) }
                }
            }
            MotionEvent.ACTION_UP -> {
                currentWindow?.let { it.contentView.onTouchEvent(makeMotionEventForPopup(it.contentView, e)) }
                currentWindow?.dismiss()
            }
        }
        return true
    }

    private fun makeMotionEventForPopup(contentView: View, e: MotionEvent) = MotionEvent.obtain(
        e.downTime,
        e.eventTime,
        e.action,
        contentView.width - this.width + e.x,
        contentView.height - this.height + e.y,
        e.pressure,
        e.size,
        e.metaState,
        e.xPrecision,
        e.yPrecision,
        e.deviceId,
        e.edgeFlags,
    )


    fun showPopup(@Scrollbar.Orientation orientation: Int) {
        currentWindow?.dismiss()
        scrollBar.orientation = orientation
        scrollBar.controller.updateTheme(context)
        scrollBar.background = ShapeDrawable(RoundRectShape(
            FloatArray(8) { resources.getDimension(R.dimen.search_bar_radius) }, null, null)).apply {
            paint.color = ColorPalette.getCurrent().neutralVeryDark
        }
        val p = 24.dp.toPixels(this)
        when (orientation) {
            Scrollbar.HORIZONTAL -> scrollBar.setPadding(p, 0, p, 0)
            Scrollbar.VERTICAL -> scrollBar.setPadding(0, p, 0, p)
        }
        scrollBar.typeface = ResourcesCompat.getFont(context, R.font.jet_brains_mono)!!
        val location = IntArray(2)
        getLocationOnScreen(location)
        currentWindow = PopupWindow(
            scrollBar,
            when (orientation) {
                Scrollbar.HORIZONTAL -> -Device.screenWidth(context) + (location[0] + this@ScrollbarIconView.width) * 2
                else -> this@ScrollbarIconView.height
            },
            when (orientation) {
                Scrollbar.HORIZONTAL -> this@ScrollbarIconView.width
                else -> Device.screenHeight(context) * 2 / 3
            },
            true
        ).apply {
            setOnDismissListener {
                currentWindow = null
            }

            showAtLocation(
                this@ScrollbarIconView,
                when (orientation) {
                    Scrollbar.HORIZONTAL -> Gravity.TOP
                    else -> Gravity.BOTTOM or Gravity.START
                },
                when (orientation) {
                    Scrollbar.HORIZONTAL -> 0
                    else -> location[0]
                },
                when (orientation) {
                    Scrollbar.HORIZONTAL -> location[1]
                    else -> Device.screenHeight(context) - location[1] - this@ScrollbarIconView.height + (appDrawer?.activity?.getNavigationBarHeight() ?: 0)
                },
            )
        }
    }

    fun reloadController(settings: Settings) {
        when (settings.scrollbarController) {
            ScrollbarControllerSetting.SCROLLBAR_CONTROLLER_BY_HUE -> {
                if (scrollBar.controller !is HueScrollbarController) {
                    scrollBar.controller = HueScrollbarController(scrollBar)
                }
            }
            else -> {
                if (scrollBar.controller !is AlphabetScrollbarController) {
                    scrollBar.controller = AlphabetScrollbarController(scrollBar)
                }
            }
        }
    }
}