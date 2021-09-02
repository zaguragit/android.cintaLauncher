package io.posidon.android.lookerupper.data.results

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.palette.graphics.Palette
import posidon.android.conveniencelib.toBitmap

class ShortcutResult(
    val shortcutInfo: ShortcutInfo,
    override val title: String,
    override val icon: Drawable,
    val app: AppResult
) : CompactResult() {

    var showSubtitle = true

    override val subtitle get() = if (showSubtitle) app.title else null
    override var relevance = Relevance(0f)
    override val onLongPress = null

    private val _color = run {
        val palette = Palette.from(icon.toBitmap()).generate()
        val def = -0xdad9d9
        var color = palette.getDominantColor(def)
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        if (hsv[1] < .1f) {
            color = palette.getVibrantColor(def)
        }
        color
    }

    fun getColor(): Int = _color

    override fun open(view: View) {
        try {
            val launcherApps = view.context.getSystemService(LauncherApps::class.java)
            launcherApps.startShortcut(shortcutInfo, null, null)
        } catch (e: Exception) { e.printStackTrace() }
    }
}