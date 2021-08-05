package io.posidon.android.cintalauncher.color

import android.content.Context
import androidx.core.graphics.ColorUtils
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.Colors
import kotlin.math.abs

interface TintedColorTheme : ColorTheme {

    override fun adjustColorForContrast(base: Int, tint: Int): Int {
        return if (Colors.getLuminance(base) > .7f) {
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(tint, lab)
            lab[0] = lab[0].coerceAtMost(20.0)
            ColorUtils.LABToColor(lab[0], lab[1], lab[2])
        } else {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(tint, hsl)
            hsl[2] = hsl[2].coerceAtLeast(.92f - hsl[1] * .2f)
            ColorUtils.HSLToColor(hsl)
        }
    }

    override fun tintAppDrawerItem(color: Int): Int {
        val baseLab = DoubleArray(3)
        ColorUtils.colorToLAB(appDrawerItemBase, baseLab)
        baseLab[0] = if (Colors.getLuminance(appDrawerItemBase) > .7f) {
            baseLab[0]
        } else {
            (baseLab[0] + 10).coerceAtLeast(20.0)
        }
        val colorLab = DoubleArray(3)
        ColorUtils.colorToLAB(color, colorLab)
        val accentLab = DoubleArray(3)
        ColorUtils.colorToLAB(accentColor, accentLab)
        val r = splitTint(baseLab, colorLab, accentLab)
        return ColorUtils.LABToColor(r[0], r[1], r[2])
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

        fun splitTint(base: Int, color: Int, accent: Int): Int {
            val baseLab = DoubleArray(3)
            ColorUtils.colorToLAB(base, baseLab)
            val colorLab = DoubleArray(3)
            ColorUtils.colorToLAB(color, colorLab)
            val accentLab = DoubleArray(3)
            ColorUtils.colorToLAB(accent, accentLab)
            val r = splitTint(baseLab, colorLab, accentLab)
            return ColorUtils.LABToColor(r[0], r[1], r[2])
        }

        fun splitTint(base: DoubleArray, color: DoubleArray, accent: DoubleArray): DoubleArray {
            val az = (accent[1] + 128) / 256
            val bz = (accent[2] + 128) / 256

            val ab = (base[1] + 128) / 256
            val bb = (base[2] + 128) / 256

            val ac = (color[1] + 128) / 256
            val bc = (color[2] + 128) / 256

            val azz = ((1 - abs(az - ac)) * abs(ab - ac)) * 2
            val bzz = ((1 - abs(bz - bc)) * abs(bb - bc)) * 2

            base[1] = accent[1] * azz + base[1] * (1 - azz)
            base[2] = accent[2] * bzz + base[2] * (1 - bzz)

            val oldL = base[0]
            ColorUtils.blendLAB(base, color, 0.4, base)
            base[0] = oldL

            return base
        }
    }
}