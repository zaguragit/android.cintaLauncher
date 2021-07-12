package io.posidon.android.cintalauncher.ui.color

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.Colors
import kotlin.math.*

interface TintedColorTheme : ColorTheme {

    val appDrawerItemBaseHSL: FloatArray

    override fun forCardBackground(color: Int): Int {
        return if (Colors.getLuminance(feedCardBG) > .7f) {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)
            hsv[2] = hsv[2].coerceAtMost(hsv[1] + .15f)
            hsv[1] = hsv[1].coerceAtLeast(hsv[2] - .5f)
            Color.HSVToColor(hsv)
        } else {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(color, hsl)
            hsl[2] = hsl[2].coerceAtLeast(.92f - hsl[1] * .2f)
            ColorUtils.HSLToColor(hsl)
        }
    }

    override fun actionButtonBG(color: Int): Int {
        val cardHSL = FloatArray(3)
        ColorUtils.colorToHSL(feedCardBG, cardHSL)
        return if (Colors.getLuminance(feedCardBG) > .7f) {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(color, hsl)
            hsl[2] = hsl[2].coerceAtLeast(.5f)
            Colors.blend(feedCardBG, ColorUtils.HSLToColor(hsl), .8f)
        } else {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(color, hsl)
            hsl[2] = hsl[2].coerceAtLeast(cardHSL[2] - .05f)
                .coerceAtMost(cardHSL[2] + .05f)
            ColorUtils.HSLToColor(hsl)
        }
    }

    override fun actionButtonFG(color: Int): Int {
        return if (Colors.getLuminance(feedCardBG) > .7f) {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)
            hsv[2] = hsv[2].coerceAtMost(.35f)
            hsv[1] = hsv[1].coerceAtLeast(.3f)
            Color.HSVToColor(hsv)
        } else {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(color, hsl)
            hsl[2] = hsl[2].coerceAtLeast(1f - hsl[1] * .2f)
            ColorUtils.HSLToColor(hsl)
        }
    }

    override fun tintAppDrawerItem(color: Int): Int {
        val colorHSL = FloatArray(3)
        ColorUtils.colorToHSL(color, colorHSL)
        val h = abs(appDrawerItemBaseHSL[0] - colorHSL[0]) / 360f
        val hueDistance = min(h, 1f - h)
        val newBaseHSL = appDrawerItemBaseHSL.copyOf()
        newBaseHSL[1] *= sqrt(1f - hueDistance * 2f)
        val newBase = ColorUtils.HSLToColor(newBaseHSL)
        return if (Colors.getLuminance(appDrawerColor) > .7f) {
            colorHSL[2] = colorHSL[2].coerceAtLeast(.92f)
            val c = ColorUtils.HSLToColor(colorHSL)
            val r = Colors.red(c) * Colors.red(c) * Colors.red(newBase) / 255 / 255
            val g = Colors.green(c) * Colors.green(c) * Colors.green(newBase) / 255 / 255
            val b = Colors.blue(c) * Colors.blue(c) * Colors.blue(newBase) / 255 / 255
            Colors.argb(255, r, g, b)
        } else {
            Colors.blend(newBase, color, .7f)
        }
    }

    override fun textColorForBG(context: Context, background: Int): Int {
        return tintWithColor(context, if (Colors.getLuminance(background) > .6f)
            R.color.feed_card_text_dark_description
        else R.color.feed_card_text_light_description, background)
    }

    override fun titleColorForBG(context: Context, background: Int): Int {
        return tintWithColor(context, if (Colors.getLuminance(background) > .6f)
            R.color.feed_card_text_dark_title
        else R.color.feed_card_text_light_title, background)
    }

    override fun hintColorForBG(context: Context, background: Int): Int {
        return tintWithColor(context, if (Colors.getLuminance(background) > .6f)
            R.color.feed_card_text_dark_hint
        else R.color.feed_card_text_light_hint, background)
    }

    companion object {
        fun tintWithColor(context: Context, colorRes: Int, color: Int): Int {
            val base = context.getColor(colorRes)
            val tintHSL = FloatArray(3)
            val baseHSL = FloatArray(3)
            ColorUtils.colorToHSL(color, tintHSL)
            ColorUtils.colorToHSL(base, baseHSL)
            tintHSL[2] = baseHSL[2]
            return ColorUtils.HSLToColor(tintHSL) and 0xffffff or (base and 0xff000000.toInt())
        }

        fun tintWithSwatch(
            context: Context,
            colorRes: Int,
            swatch: Palette.Swatch?,
            baseLightness: Float
        ): Int {
            val base = context.getColor(colorRes)
            if (swatch == null) return base
            val hsl = swatch.hsl
            hsl[2] = min(hsl[2], baseLightness)
            return ColorUtils.HSLToColor(hsl) and 0xffffff or (base and 0xff000000.toInt())
        }
    }
}