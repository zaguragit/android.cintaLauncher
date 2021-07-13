package io.posidon.android.lookerupper.data.results

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.view.View
import androidx.palette.graphics.Palette
import posidon.android.conveniencelib.toBitmap

class AppResult(
    val packageName: String,
    val name: String,
    val userHandle: UserHandle,
    title: String,
    icon: Drawable
) : SimpleResult(title, icon) {
    override var relevance = Relevance(0f)

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
            (view.context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).startMainActivity(
                ComponentName(packageName, name), userHandle, null,
                ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle())
        } catch (e: Exception) { e.printStackTrace() }
    }
}