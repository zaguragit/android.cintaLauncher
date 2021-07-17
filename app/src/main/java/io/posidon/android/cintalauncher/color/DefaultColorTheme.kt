package io.posidon.android.cintalauncher.color

import android.content.Context
import android.graphics.Color
import io.posidon.android.cintalauncher.R

class DefaultColorTheme(context: Context) : ColorTheme {
    override val accentColor = context.getColor(R.color.accent)
    override val uiBG = context.getColor(R.color.feed_bg)
    override val feedCardBG = context.getColor(R.color.default_card_bg)
    override val feedCardTitle = context.getColor(R.color.feed_card_text_dark_title)
    override val feedCardDescription = context.getColor(R.color.feed_card_text_dark_description)
    override val appDrawerColor = context.getColor(R.color.drawer_bg)
    override val appDrawerBottomBarColor = context.getColor(R.color.drawer_bottom_bar)
    override val buttonColor = context.getColor(R.color.button_bg)
    override val appDrawerItemBase get() = DEFAULT_DRAWER_ITEM_BASE
    override val appDrawerSectionColor = context.getColor(R.color.feed_card_text_light_description)
    override val searchBarBG get() = appDrawerItemBase
    override val searchBarFG = textColorForBG(context, searchBarBG)

    override fun forCardBackground(color: Int): Int {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(color, hsv)
        hsv[2] = hsv[2].coerceAtMost(hsv[1] + .15f)
        hsv[1] = hsv[1].coerceAtLeast(hsv[2] - .5f)
        return Color.HSVToColor(hsv)
    }

    override fun actionButtonBG(color: Int): Int {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(color, hsv)
        hsv[2] = hsv[2].coerceAtLeast(.96f)
        hsv[1] = hsv[1].coerceAtMost(.18f)
        return Color.HSVToColor(hsv)
    }

    override fun actionButtonFG(color: Int): Int {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(color, hsv)
        hsv[2] = hsv[2].coerceAtMost(.35f)
        hsv[1] = hsv[1].coerceAtLeast(.3f)
        return Color.HSVToColor(hsv)
    }

    companion object {
        const val DEFAULT_DRAWER_ITEM_BASE = -0xdad9d9
    }
}