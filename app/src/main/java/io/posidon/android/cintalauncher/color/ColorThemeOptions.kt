package io.posidon.android.cintalauncher.color

import posidon.android.conveniencelib.Colors

data class ColorThemeOptions(
    val mode: DayNight
) {
    enum class DayNight {
        AUTO,
        DARK,
        LIGHT,
    }

    inline fun isDarkModeColor(argb: Int): Boolean {
        return when (mode) {
            DayNight.AUTO -> Colors.getLuminance(argb) < .6f
            DayNight.DARK -> true
            DayNight.LIGHT -> false
        }
    }

    inline fun isDarkModeCardColor(argb: Int): Boolean {
        return when (mode) {
            DayNight.AUTO -> Colors.getLuminance(argb) < .3f
            DayNight.DARK -> true
            DayNight.LIGHT -> false
        }
    }
}