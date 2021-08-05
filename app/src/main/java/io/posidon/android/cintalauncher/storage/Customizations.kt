package io.posidon.android.cintalauncher.storage

val Settings.colorTheme: Int
    get() = get(KEY_COLOR_THEME, COLOR_THEME_DEFAULT)

var Settings.SettingsEditor.colorTheme: Int
    get() = settings[KEY_COLOR_THEME, COLOR_THEME_DEFAULT]
    set(value) = KEY_COLOR_THEME set value

private const val KEY_COLOR_THEME = "color_theme"

const val COLOR_THEME_PLAIN = 0
const val COLOR_THEME_WALLPAPER_TINT = 1
const val COLOR_THEME_WALLPAPER_TINT_SYSTEM_ASSISTED = 2

const val COLOR_THEME_DEFAULT = COLOR_THEME_WALLPAPER_TINT


val Settings.scrollbarController: Int
    get() = get(KEY_SCROLLBAR_CONTROLLER, SCROLLBAR_CONTROLLER_DEFAULT)

var Settings.SettingsEditor.scrollbarController: Int
    get() = settings[KEY_SCROLLBAR_CONTROLLER, SCROLLBAR_CONTROLLER_DEFAULT]
    set(value) = KEY_SCROLLBAR_CONTROLLER set value

private const val KEY_SCROLLBAR_CONTROLLER = "scrollbar_controller"

const val SCROLLBAR_CONTROLLER_ALPHABETIC = 0
const val SCROLLBAR_CONTROLLER_BY_HUE = 1

const val SCROLLBAR_CONTROLLER_DEFAULT = SCROLLBAR_CONTROLLER_ALPHABETIC