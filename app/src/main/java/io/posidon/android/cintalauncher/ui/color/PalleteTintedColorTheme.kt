package io.posidon.android.cintalauncher.ui.color

import android.content.Context
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.ui.color.TintedColorTheme.Companion.tintWithSwatch
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

    override val feedBG = tintWithSwatch(context, R.color.feed_bg, run {
        val dm = wallpaper.darkMutedSwatch
        val dv = wallpaper.darkVibrantSwatch
        val dom = wallpaper.dominantSwatch
        var y = dv
        if (y == null || dm != null && dm.population > y.population) y = dm
        if (y == null || dom != null && dom.population > y.population) y = dom
        y
    }, 0.1f)

    override val feedCardBG = run {
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

    override val feedCardTitle = titleColorForBG(context, feedCardBG)
    override val feedCardDescription = textColorForBG(context, feedCardBG)

    override val appDrawerColor = run {
        val swatch = wallpaper.dominantSwatch ?: return@run context.getColor(R.color.drawer_bg)
        val rgb = swatch.rgb
        val hsl = swatch.hsl
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

    override val appDrawerBottomBarColor = run {
        val isLight = Colors.getLuminance(appDrawerColor) > .7f
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(appDrawerColor, hsl)
        hsl[2] *= if (isLight) .8f else 1.2f
        ColorUtils.HSLToColor(hsl) and 0xffffff or 0x88000000.toInt()
    }
    override val appDrawerButtonColor = run {
        val swatch = wallpaper.vibrantSwatch ?: wallpaper.dominantSwatch ?: return@run context.getColor(R.color.button_bg)
        val hsl = swatch.hsl
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
            val v = wallpaper.vibrantSwatch ?: wallpaper.dominantSwatch
            val s = v?.hsl?.get(1) ?: .5f
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