package io.posidon.android.cintalauncher.providers.app

import android.content.Context
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RectShape
import android.os.UserHandle
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.BuildConfig
import io.posidon.android.cintalauncher.data.items.App
import io.posidon.android.cintalauncher.storage.DoReshapeAdaptiveIconsSetting.doReshapeAdaptiveIcons
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.LauncherActivity
import io.posidon.android.launcherutils.AppLoader
import posidon.android.conveniencelib.toBitmap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AppCollection(appCount: Int) : AppLoader.AppCollection<App> {
    val list = ArrayList<App>(appCount)
    val byName = HashMap<String, MutableList<App>>()

    val sections = LinkedList<List<App>>()

    inline operator fun get(i: Int) = list[i]
    inline val size get() = list.size

    override fun add(context: Context, app: App) {
        if (app.packageName == BuildConfig.APPLICATION_ID &&
            app.name == LauncherActivity::class.java.name
        )
            return
        list.add(app)
        putInMap(app)
    }

    private fun putInMap(app: App) {
        val list = byName[app.packageName]
        if (list == null) {
            byName[app.packageName] = arrayListOf(app)
            return
        }
        val thisAppI = list.indexOfFirst {
            it.name == app.name && it.userHandle.hashCode() == app.userHandle.hashCode()
        }
        if (thisAppI == -1) {
            list.add(app)
            return
        }
        list[thisAppI] = app
    }

    override fun finalize(context: Context) {
        list.sortWith { o1, o2 ->
            o1.label.compareTo(o2.label, ignoreCase = true)
        }
    }

    companion object {
        private fun scale(fg: Drawable): Drawable {
            return InsetDrawable(
                fg,
                -1 / 3f
            )
        }

        fun createApp(
            packageName: String,
            name: String,
            profile: UserHandle,
            label: String,
            icon: Drawable,
            extra: AppLoader.ExtraAppInfo,
            settings: Settings
        ): App {
            var color = 0
            var hsl = FloatArray(3)
            var icon = icon
            var banner = extra.banner
            var background = extra.background

            if (background != null) {
                banner = null
                val palette = Palette.from(background.toBitmap(32, 32)).generate()
                val d = palette.dominantSwatch
                hsl = d?.hsl ?: hsl
                color = d?.rgb ?: color
            }
            else if (settings.doReshapeAdaptiveIcons && icon is AdaptiveIconDrawable) {
                val b = icon.background
                icon = when (b) {
                    is ColorDrawable -> {
                        color = b.color
                        ColorUtils.colorToHSL(color, hsl)
                        background = background ?: b
                        scale(icon.foreground)
                    }
                    is ShapeDrawable -> {
                        color = b.paint.color
                        ColorUtils.colorToHSL(color, hsl)
                        background = background ?: b.apply {
                            shape = RectShape()
                        }
                        scale(icon.foreground)
                    }
                    is GradientDrawable -> {
                        color = b.color?.defaultColor ?: Palette.from(b.toBitmap(16, 16)).generate().getDominantColor(0)
                        ColorUtils.colorToHSL(color, hsl)
                        background = background ?: b.apply {
                            cornerRadius = 0f
                        }
                        scale(icon.foreground)
                    }
                    else -> {
                        if (b != null) {
                            val palette = Palette.from(b.toBitmap()).generate()
                            val d = palette.dominantSwatch
                            hsl = d?.hsl ?: hsl
                            color = run {
                                val def = 0
                                var c = (d ?: return@run def).rgb
                                if (hsl[1] < .1f) {
                                    c = palette.getVibrantColor(c)
                                }
                                c
                            }
                        }
                        icon
                    }
                }
            }

            if (color == 0) {
                val palette = Palette.from(icon.toBitmap()).generate()
                val d = palette.dominantSwatch
                hsl = d?.hsl ?: hsl
                color = run {
                    var c = d?.rgb ?: return@run color
                    if (hsl[1] < .1f) {
                        c = palette.getVibrantColor(c)
                    }
                    c
                }
            }

            color = color and 0xffffff or 0xff000000.toInt()

            return App(
                packageName,
                name,
                profile,
                label,
                icon,
                background ?: banner,
                if (background != null) .7f else .1f,
                hsl,
                color
            )
        }
    }
}