package io.posidon.android.cintalauncher.color

import android.content.Context
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.color.TintedColorTheme.Companion.tintWithSwatch
import posidon.android.conveniencelib.Colors
import kotlin.math.*

class PalleteTintedColorTheme(
    wallpaper: Palette,
    context: Context
) : TintedColorTheme {

    override val accentColor = run {
        val d = wallpaper.vibrantSwatch ?: wallpaper.lightVibrantSwatch ?: wallpaper.dominantSwatch
        val hsl = d?.hsl ?: return@run context.getColor(R.color.accent)
        hsl[2] = hsl[2].coerceAtLeast(.5f)
        ColorUtils.HSLToColor(hsl)
    }

    override val uiBG = tintWithSwatch(context, R.color.feed_bg, run {
        val dm = wallpaper.darkMutedSwatch
        val lm = wallpaper.lightMutedSwatch
        val dv = wallpaper.darkVibrantSwatch
        val lv = wallpaper.lightVibrantSwatch
        val dom = wallpaper.dominantSwatch
        var y = dv
        if (y == null || dm != null && dm.population > y.population) y = dm
        if (y == null || lm != null && lm.population > y.population) y = lm
        if (y == null || lv != null && lv.population > y.population) y = lv
        if (y == null || dom != null && dom.population > y.population) y = dom
        y
    }, 0.1f)

    override val uiTitle = titleColorForBG(context, uiBG)
    override val uiDescription = textColorForBG(context, uiBG)
    override val uiHint = hintColorForBG(context, uiBG)

    override val cardBG = run {
        val dom = wallpaper.dominantSwatch ?: wallpaper.vibrantSwatch ?: return@run context.getColor(R.color.default_card_bg)
        val domHsl = dom.hsl
        val l = domHsl[2]
        when {
            l < .22f -> {
                val hsl = (wallpaper.darkVibrantSwatch ?: wallpaper.darkMutedSwatch ?: dom).hsl
                hsl[2] = min(hsl[2], .24f)
                ColorUtils.HSLToColor(hsl)
            }
            else -> {
                val hsl = (wallpaper.lightVibrantSwatch ?: wallpaper.lightMutedSwatch ?: dom).hsl
                hsl[2] = max(hsl[2], .96f - hsl[1] * .1f)
                ColorUtils.HSLToColor(hsl)
            }
        }
    }

    override val cardTitle = titleColorForBG(context, cardBG)
    override val cardDescription = textColorForBG(context, cardBG)

    override val appDrawerColor = run {
        val swatch = wallpaper.dominantSwatch ?: return@run context.getColor(R.color.drawer_bg)
        val rgb = swatch.rgb
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
            else -> lab[0] = lab[0].coerceAtMost(10.0)
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
        val swatch = wallpaper.vibrantSwatch ?: wallpaper.dominantSwatch ?: return@run context.getColor(R.color.button_bg)
        val hsl = swatch.hsl
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
            val v = wallpaper.vibrantSwatch ?: wallpaper.dominantSwatch
            val s = v?.hsl?.get(1) ?: .5f
            hsl[2] = 0.36f - s * 0.3f
            if (s >= 0.5f) {
                hsl[1] = 0.36f - s * 0.1f
            }
        }
        hsl
    })

    override val searchBarBG = appDrawerItemBase

    override val searchBarFG = titleColorForBG(context, searchBarBG)
}