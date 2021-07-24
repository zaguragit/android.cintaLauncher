package io.posidon.android.cintalauncher.color

import android.app.WallpaperColors
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.Colors
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

    override val feedCardBG = run {
        val dom = (secondary ?: primary).toArgb()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(dom, hsl)
        val l = hsl[2]
        when {
            l < .22f -> {
                //val hsl = (wallpaper.darkVibrantSwatch ?: wallpaper.darkMutedSwatch ?: dom).hsl
                hsl[2] = min(hsl[2], .24f)
                ColorUtils.HSLToColor(hsl)
            }
            else -> {
                //val hsl = (wallpaper.lightVibrantSwatch ?: wallpaper.lightMutedSwatch ?: dom).hsl
                hsl[2] = max(hsl[2], .96f - hsl[1] * .1f)
                ColorUtils.HSLToColor(hsl)
            }
        }
    }

    override val feedCardTitle = titleColorForBG(context, feedCardBG)
    override val feedCardDescription = textColorForBG(context, feedCardBG)

    override val appDrawerColor = run {
        val rgb = primary.toArgb()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(rgb, hsl)
        val lum = Colors.getLuminance(rgb)
        when {
            lum > .7f -> {
                val oldL = hsl[2]
                hsl[2] = oldL.coerceAtLeast(.89f)
                val ld = hsl[2] - oldL
                hsl[1] *= 1f + ld * 1.6f
            }
            lum > .4f -> hsl[2] = hsl[2].coerceAtMost(.26f)
            else -> hsl[2] = hsl[2].coerceAtMost(.09f)
        }
        ColorUtils.HSLToColor(hsl)
    }

    //override val appDrawerBottomBarColor = primary.toArgb() and 0xffffff or (context.getColor(R.color.drawer_bottom_bar) and 0xff000000.toInt())
    //override val appDrawerButtonColor = secondary?.toArgb() ?: baseTheme.appDrawerButtonColor

    override val appDrawerBottomBarColor = run {
        val isLight = Colors.getLuminance(appDrawerColor) > .7f
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(appDrawerColor, hsl)
        hsl[2] *= if (isLight) .8f else 1.2f
        ColorUtils.HSLToColor(hsl) and 0xffffff or 0x88000000.toInt()
    }
    override val buttonColor = run {
        val swatch = tertiary ?: secondary ?: primary
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(swatch.toArgb(), hsl)
        hsl[2] = min(hsl[2], 0.4f)
        ColorUtils.HSLToColor(hsl)
    }

    override val appDrawerSectionColor = textColorForBG(context, appDrawerColor)

    override val appDrawerItemBaseHSL = run {
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
    }
    override val appDrawerItemBase = ColorUtils.HSLToColor(appDrawerItemBaseHSL)

    override val searchBarBG = run {
        appDrawerItemBase
    }

    override val searchBarFG = titleColorForBG(context, searchBarBG)
}