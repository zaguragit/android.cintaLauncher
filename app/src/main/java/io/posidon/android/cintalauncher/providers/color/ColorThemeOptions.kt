package io.posidon.android.cintalauncher.providers.color

import io.posidon.android.cintalauncher.providers.color.pallete.ColorPalette
import io.posidon.android.cintalauncher.providers.color.theme.ColorTheme
import io.posidon.android.cintalauncher.providers.color.theme.DarkColorTheme
import io.posidon.android.cintalauncher.providers.color.theme.LightColorTheme

data class ColorThemeOptions(
    val mode: DayNight
) {
    enum class DayNight {
        AUTO,
        DARK,
        LIGHT,
    }

    fun createColorTheme(palette: ColorPalette): ColorTheme {
        return if (mode == DayNight.LIGHT) LightColorTheme(palette)
        else DarkColorTheme(palette)
    }

    override fun toString() = "${javaClass.simpleName} { mode: $mode }"
}