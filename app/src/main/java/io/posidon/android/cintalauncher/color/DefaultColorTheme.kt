package io.posidon.android.cintalauncher.color

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import io.posidon.android.cintalauncher.R
import posidon.android.conveniencelib.Colors

class DefaultColorTheme(
    context: Context,
    override val options: ColorThemeOptions
) : ColorTheme {

    override val accentColor = context.getColor(R.color.accent)

    override val wallColor = 0
    override val wallTitle = titleColorForBG(context, wallColor)
    override val wallDescription = textColorForBG(context, wallColor)
    override val wallHint = hintColorForBG(context, wallColor)

    override val uiBG = context.getColor(if (options.mode == ColorThemeOptions.DayNight.LIGHT) R.color.feed_bg_light else R.color.feed_bg)
    override val uiTitle = titleColorForBG(context, uiBG)
    override val uiDescription = textColorForBG(context, uiBG)
    override val uiHint = hintColorForBG(context, uiBG)

    override val cardBG = context.getColor(R.color.default_card_bg)
    override val cardTitle = context.getColor(R.color.feed_card_text_dark_title)
    override val cardDescription = context.getColor(R.color.feed_card_text_dark_description)
    override val cardHint = context.getColor(R.color.feed_card_text_dark_hint)

    override val appDrawerColor = context.getColor(if (options.mode == ColorThemeOptions.DayNight.LIGHT) R.color.drawer_bg_light else R.color.drawer_bg)

    override val buttonColor = context.getColor(R.color.button_bg)

    override val appDrawerSectionColor = context.getColor(if (options.mode == ColorThemeOptions.DayNight.LIGHT) R.color.feed_card_text_dark_description else R.color.feed_card_text_light_description)
    override val appDrawerItemBase = context.getColor(if (options.mode == ColorThemeOptions.DayNight.LIGHT) R.color.drawer_item_base_light else R.color.drawer_item_base)

    override val scrollBarBG = 0xff000000.toInt()

    override val searchBarBG get() = appDrawerItemBase
    override val searchBarFG = textColorForBG(context, searchBarBG)

    override fun adjustColorForContrast(base: Int, tint: Int): Int {
        return if (Colors.getLuminance(base) > .7f) {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(tint, hsv)
            hsv[2] = hsv[2].coerceAtMost(hsv[1] + .15f)
            hsv[1] = hsv[1].coerceAtLeast(hsv[2] - .5f)
            Color.HSVToColor(hsv)
        } else {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(tint, hsl)
            hsl[2] = hsl[2].coerceAtLeast(.92f - hsl[1] * .2f)
            ColorUtils.HSLToColor(hsl)
        }
    }

    override fun tintAppDrawerItem(color: Int): Int {
        return if (options.isDarkModeColor(appDrawerItemBase))
            Colors.blend(appDrawerItemBase, color, .7f)
        else {
            Colors.blend(appDrawerItemBase, color, .9f)
            val baseLab = DoubleArray(3)
            ColorUtils.colorToLAB(appDrawerItemBase, baseLab)
            val colorLab = DoubleArray(3)
            ColorUtils.colorToLAB(color, colorLab)
            colorLab[0] = colorLab[0].coerceAtLeast(baseLab[0] + 36.0)
            return ColorUtils.LABToColor(colorLab[0], colorLab[1], colorLab[2])
        }
    }
}