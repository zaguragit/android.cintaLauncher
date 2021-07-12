package io.posidon.android.cintalauncher.storage

val Settings.colorTheme: Int
    get() = get(KEY_COLOR_THEME, COLOR_THEME_DEFAULT)

var Settings.SettingsEditor.colorTheme: Int
    get() = settings["color_theme", COLOR_THEME_DEFAULT]
    set(value) = "color_theme" set value

private const val KEY_COLOR_THEME = "color_theme"

const val COLOR_THEME_PLAIN = 0
const val COLOR_THEME_WALLPAPER_TINT = 1
const val COLOR_THEME_WALLPAPER_TINT_SYSTEM_ASSISTED = 2

const val COLOR_THEME_DEFAULT = COLOR_THEME_WALLPAPER_TINT