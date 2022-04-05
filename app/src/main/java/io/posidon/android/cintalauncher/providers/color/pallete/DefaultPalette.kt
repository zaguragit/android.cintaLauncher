package io.posidon.android.cintalauncher.providers.color.pallete

import io.posidon.android.cintalauncher.providers.color.pallete.ColorPalette

object DefaultPalette : ColorPalette {
    override val estimatedWallColor: Int = 0xff000000.toInt()

    override val neutralVeryDark: Int = 0xff000000.toInt()
    override val neutralDark: Int = 0xff202630.toInt()
    override val neutralMedium: Int = 0xff77888f.toInt()
    override val neutralLight: Int = 0xffdddddd.toInt()
    override val neutralVeryLight: Int = 0xffffffff.toInt()

    override val primary: Int = 0xff0ee463.toInt()
    override val secondary: Int = 0xff0ee463.toInt()
}