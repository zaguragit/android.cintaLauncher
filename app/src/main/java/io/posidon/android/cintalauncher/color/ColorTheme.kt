package io.posidon.android.cintalauncher.color

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.storage.ColorThemeSetting
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.toBitmap
import kotlin.math.min

private lateinit var colorThemeInstance: ColorTheme

interface ColorTheme {

    val options: ColorThemeOptions

    val accentColor: Int

    val uiBG: Int
    val uiTitle: Int
    val uiDescription: Int
    val uiHint: Int

    val cardBG: Int
    val cardTitle: Int
    val cardDescription: Int
    val cardHint: Int

    val appDrawerColor: Int
    val appDrawerBottomBarColor: Int

    val buttonColor: Int

    val appDrawerSectionColor: Int
    val appDrawerItemBase: Int

    val scrollBarDefaultBG: Int
    val scrollBarTintBG: Int

    val searchBarBG: Int
    val searchBarFG: Int

    fun adjustColorForContrast(base: Int, tint: Int): Int

    fun actionButtonBG(color: Int): Int {
        val cardHSL = FloatArray(3)
        ColorUtils.colorToHSL(cardBG, cardHSL)
        val cardLAB = DoubleArray(3)
        ColorUtils.colorToLAB(cardBG, cardLAB)
        return if (Colors.getLuminance(cardBG) > .7f) {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(color, hsl)
            hsl[2] = hsl[2].coerceAtLeast(.5f)
            Colors.blend(cardBG, ColorUtils.HSLToColor(hsl), .8f)
        } else {
            val lab = doubleArrayOf(0.0, 0.0, 0.0)
            ColorUtils.colorToLAB(color, lab)
            lab[0] = lab[0].coerceAtLeast(cardLAB[0] - .05f)
                .coerceAtMost(cardLAB[0] + .05f)
            ColorUtils.LABToColor(lab[0], lab[1], lab[2])
        }
    }

    @SuppressLint("Range")
    fun actionButtonFG(color: Int): Int {
        return if (Colors.getLuminance(color) > .7f) {
            val lab = doubleArrayOf(0.0, 0.0, 0.0)
            ColorUtils.colorToLAB(color, lab)
            ColorUtils.LABToColor(lab[0] * .5 - 10.0, lab[1].coerceAtMost(75.0), lab[2])
        } else {
            val lab = doubleArrayOf(0.0, 0.0, 0.0)
            ColorUtils.colorToLAB(color, lab)
            ColorUtils.LABToColor(lab[0] * 1.5 + 30 + lab[1].coerceAtLeast(0.0) * .25, lab[1].coerceAtMost(60.0), lab[2])
        }
    }

    fun tintAppDrawerItem(color: Int): Int {
        return Colors.blend(appDrawerItemBase, color, .7f)
    }

    fun textColorForBG(context: Context, background: Int): Int {
        return if (Colors.getLuminance(background) > .6f)
            context.getColor(R.color.feed_card_text_dark_description)
        else context.getColor(R.color.feed_card_text_light_description)
    }

    fun titleColorForBG(context: Context, background: Int): Int {
        return if (Colors.getLuminance(background) > .6f)
            context.getColor(R.color.feed_card_text_dark_title)
        else context.getColor(R.color.feed_card_text_light_title)
    }

    fun hintColorForBG(context: Context, background: Int): Int {
        return if (Colors.getLuminance(background) > .6f)
            context.getColor(R.color.feed_card_text_dark_hint)
        else context.getColor(R.color.feed_card_text_light_hint)
    }

    companion object : ColorTheme {
        var isInitialized = false
            private set

        fun onCreate(colorThemeOptions: ColorThemeOptions, activity: Activity) {
            if (!isInitialized) {
                colorThemeInstance = DefaultColorTheme(activity, colorThemeOptions)
                isInitialized = true
            }
        }
        
        fun <A : Activity> loadWallColorTheme(activity: A, colorThemeOptions: ColorThemeOptions, onFinished: (A) -> Unit) {
            if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                if (colorThemeInstance !is DefaultColorTheme || colorThemeInstance.options != colorThemeOptions) {
                    colorThemeInstance = DefaultColorTheme(activity, colorThemeOptions)
                    activity.runOnUiThread {
                        onFinished(activity)
                    }
                }
                return
            }
            val wp = activity.getSystemService(WallpaperManager::class.java)
            val d = wp.fastDrawable
            val wall = d.toBitmap(
                min(d.intrinsicWidth, Device.screenWidth(activity) / 4),
                min(d.intrinsicHeight, Device.screenHeight(activity) / 4)
            )
            val pallete = Palette.from(wall).generate()
            val newColorTheme = PalleteTintedColorTheme(pallete, activity, colorThemeOptions)
            if (newColorTheme != colorThemeInstance) {
                colorThemeInstance = newColorTheme
                activity.runOnUiThread {
                    onFinished(activity)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        fun <A : Activity> loadSystemWallColorTheme(
            activity: A,
            colorThemeOptions: ColorThemeOptions,
            onFinished: (A) -> Unit,
            colors: WallpaperColors
        ) {
            colorThemeInstance = AndroidOMR1WallColorTheme(activity, colors, colorThemeOptions)
            activity.runOnUiThread {
                onFinished(activity)
            }
        }

        private fun <A : Activity> loadDefaultColorTheme(activity: A, colorThemeOptions: ColorThemeOptions, onFinished: (A) -> Unit) {
            if (colorThemeInstance !is DefaultColorTheme || colorThemeInstance.options != colorThemeOptions) {
                colorThemeInstance = DefaultColorTheme(activity, colorThemeOptions)
                activity.runOnUiThread {
                    onFinished(activity)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        fun <A : Activity> onColorsChanged(
            activity: A,
            colorTheme: Int,
            colorThemeOptions: ColorThemeOptions,
            onFinished: (A) -> Unit,
            colors: () -> WallpaperColors?,
        ) {
            when (colorTheme) {
                ColorThemeSetting.COLOR_THEME_WALLPAPER_TINT -> loadWallColorTheme(activity, colorThemeOptions, onFinished)
                ColorThemeSetting.COLOR_THEME_WALLPAPER_TINT_SYSTEM_ASSISTED -> colors()?.let {
                    loadSystemWallColorTheme(activity, colorThemeOptions, onFinished, it)
                    println(it)
                } ?: loadDefaultColorTheme(activity, colorThemeOptions, onFinished)
                else -> loadDefaultColorTheme(activity, colorThemeOptions, onFinished)
            }
        }
        
        fun <A : Activity> onResumePreOMR1(
            activity: A,
            colorTheme: Int,
            colorThemeOptions: ColorThemeOptions,
            onFinished: (A) -> Unit,
        ) {
            when (colorTheme) {
                ColorThemeSetting.COLOR_THEME_WALLPAPER_TINT_SYSTEM_ASSISTED,
                ColorThemeSetting.COLOR_THEME_WALLPAPER_TINT -> loadWallColorTheme(activity, colorThemeOptions, onFinished)
                else -> loadDefaultColorTheme(activity, colorThemeOptions, onFinished)
            }
        }

        override val options: ColorThemeOptions
            get() = colorThemeInstance.options
        override val accentColor: Int
            get() = colorThemeInstance.accentColor
        override val uiBG: Int
            get() = colorThemeInstance.uiBG
        override val uiTitle: Int
            get() = colorThemeInstance.uiTitle
        override val uiDescription: Int
            get() = colorThemeInstance.uiDescription
        override val uiHint: Int
            get() = colorThemeInstance.uiHint
        override val cardBG: Int
            get() = colorThemeInstance.cardBG
        override val cardTitle: Int
            get() = colorThemeInstance.cardTitle
        override val cardDescription: Int
            get() = colorThemeInstance.cardDescription
        override val cardHint: Int
            get() = colorThemeInstance.cardHint
        override val appDrawerColor: Int
            get() = colorThemeInstance.appDrawerColor
        override val appDrawerBottomBarColor: Int
            get() = colorThemeInstance.appDrawerBottomBarColor
        override val buttonColor: Int
            get() = colorThemeInstance.buttonColor
        override val appDrawerSectionColor: Int
            get() = colorThemeInstance.appDrawerSectionColor
        override val appDrawerItemBase: Int
            get() = colorThemeInstance.appDrawerItemBase
        override val scrollBarDefaultBG: Int
            get() = colorThemeInstance.scrollBarDefaultBG
        override val scrollBarTintBG: Int
            get() = colorThemeInstance.scrollBarTintBG
        override val searchBarBG: Int
            get() = colorThemeInstance.searchBarBG
        override val searchBarFG: Int
            get() = colorThemeInstance.searchBarFG

        override fun adjustColorForContrast(base: Int, tint: Int): Int =
            colorThemeInstance.adjustColorForContrast(base, tint)

        override fun actionButtonBG(color: Int): Int =
            colorThemeInstance.actionButtonBG(color)

        override fun actionButtonFG(color: Int): Int =
            colorThemeInstance.actionButtonFG(color)

        override fun tintAppDrawerItem(color: Int): Int =
            colorThemeInstance.tintAppDrawerItem(color)

        override fun textColorForBG(context: Context, background: Int): Int =
            colorThemeInstance.textColorForBG(context, background)

        override fun titleColorForBG(context: Context, background: Int): Int =
            colorThemeInstance.titleColorForBG(context, background)

        override fun hintColorForBG(context: Context, background: Int): Int =
            colorThemeInstance.hintColorForBG(context, background)
    }
}