package io.posidon.android.cintalauncher.color

import android.app.WallpaperColors
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.Colors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


@RequiresApi(Build.VERSION_CODES.O_MR1)
class AndroidOMR1WallColorTheme(
    context: Context,
    colors: WallpaperColors
): TintedColorTheme {

    private val primary = colors.primaryColor
    private val secondary = colors.secondaryColor
    private val tertiary = colors.tertiaryColor

    override val accentColor = (tertiary ?: secondary ?: primary).toArgb()
    override val uiBG = primary.toArgb() and 0xffffff or (context.getColor(R.color.feed_bg) and 0xff000000.toInt())
    override val uiTitle = titleColorForBG(context, uiBG)
    override val uiDescription = textColorForBG(context, uiBG)
    override val uiHint = hintColorForBG(context, uiBG)

    override val cardBG = run {
        val dom = (secondary ?: primary).toArgb()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(dom, hsl)
        val l = hsl[2]
        when {
            l < .22f -> {
                hsl[2] = min(hsl[2], .24f)
                ColorUtils.HSLToColor(hsl)
            }
            else -> {
                hsl[2] = max(hsl[2], .96f - hsl[1] * .1f)
                ColorUtils.HSLToColor(hsl)
            }
        }
    }

    override val cardTitle = titleColorForBG(context, cardBG)
    override val cardDescription = textColorForBG(context, cardBG)
    override val cardHint = hintColorForBG(context, cardBG)

    override val appDrawerColor = run {
        val rgb = primary.toArgb()
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(rgb, lab)
        val lum = Colors.getLuminance(rgb)
        when {
            lum > .6f -> {
                val oldL = lab[0]
                lab[0] = lab[0].coerceAtLeast(89.0)
                val ld = lab[0] - oldL
                lab[1] *= 1.0 + ld / 100 * 1.6
                lab[2] *= 1.0 + ld / 100 * 1.6
            }
            else -> {
                lab[0] = lab[0].coerceAtMost(5.0 - abs(lab[1]) / 128 + lab[2].coerceAtMost(0.0) / 128)
                lab[1] = (lab[1] / 3.0).coerceAtLeast(-50.0).coerceAtMost(50.0)
                lab[2] = (lab[2] / 3.0).coerceAtLeast(-45.0).coerceAtMost(70.0)
            }
        }
        ColorUtils.LABToColor(lab[0], lab[1], lab[2])
    }

    override val appDrawerBottomBarColor = run {
        val isLight = Colors.getLuminance(appDrawerColor) > .7f
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(appDrawerColor, lab)
        lab[0] *= if (isLight) .8 else 1.2
        ColorUtils.LABToColor(lab[0], lab[1], lab[2]) and 0xffffff or 0x88000000.toInt()
    }

    override val buttonColor = run {
        val swatch = tertiary ?: secondary ?: primary
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(swatch.toArgb(), hsl)
        hsl[2] = min(hsl[2], 0.4f)
        ColorUtils.HSLToColor(hsl)
    }

    override val appDrawerSectionColor = textColorForBG(context, appDrawerColor)

    override val appDrawerItemBase = ColorUtils.HSLToColor(run {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(appDrawerColor, hsl)
        if (Colors.getLuminance(appDrawerColor) > .6f) {
            hsl[1] *= hsl[1].pow(.15f) * 8.6f
            hsl[2] = hsl[2].coerceAtLeast(.96f)
        } else {
            val baseHSL = FloatArray(3)
            val v = secondary ?: primary
            ColorUtils.colorToHSL(v.toArgb(), baseHSL)
            val s = baseHSL[1]
            hsl[2] = 0.36f - s * 0.3f
            if (s >= 0.5f) {
                hsl[1] = 0.36f - s * 0.1f
            }
        }
        hsl
    })

    override val scrollBarDefaultBG = run {
        val rgb = primary.toArgb()
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(rgb, lab)
        val drawerLab = DoubleArray(3)
        ColorUtils.colorToLAB(appDrawerColor, lab)
        lab[0] = lab[0].coerceAtMost(drawerLab[0] - 5.0)
        ColorUtils.LABToColor(lab[0], lab[1], lab[2])
    }

    override val scrollBarTintBG = scrollBarDefaultBG and 0x00ffffff or 0xcc000000.toInt()

    override val searchBarBG = appDrawerItemBase

    override val searchBarFG = titleColorForBG(context, searchBarBG)
}