package io.posidon.android.cintalauncher.data.items

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.*
import android.os.Process
import android.os.UserHandle
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import io.posidon.android.cintalauncher.data.feed.items.FeedItem
import io.posidon.android.cintalauncher.providers.notification.NotificationService
import io.posidon.android.cintalauncher.storage.DoReshapeAdaptiveIconsSetting.doReshapeAdaptiveIcons
import io.posidon.android.cintalauncher.storage.Settings
import posidon.android.conveniencelib.isInstalled
import posidon.android.conveniencelib.toBitmap
import java.util.*

class App(
    val packageName: String,
    val name: String,
    val userHandle: UserHandle = Process.myUserHandle(),
    override val label: String,
    icon: Drawable,
    settings: Settings
) : LauncherItem {

    inline fun getNotifications(): List<FeedItem> =
        NotificationService.notifications.filter { it.meta?.sourcePackageName == packageName }

    override fun open(context: Context, view: View?) {
        try {
            context.getSystemService(LauncherApps::class.java).startMainActivity(ComponentName(packageName, name), userHandle, view?.clipBounds,
                ActivityOptions.makeScaleUpAnimation(view, 0, 0, view?.measuredWidth ?: 0, view?.measuredHeight ?: 0).toBundle())
        } catch (e: Exception) { e.printStackTrace() }
    }

    val hsl: FloatArray
    private val _color: Int
    override val icon: Drawable

    private fun scale(fg: Drawable): Drawable {
        return InsetDrawable(
            fg,
            -1 / 3f
        )
    }

    init {
        var _color = 0
        var hsl = FloatArray(3)

        if (settings.doReshapeAdaptiveIcons && icon is AdaptiveIconDrawable) {
            val b = icon.background
            this.icon = when (b) {
                is ColorDrawable -> {
                    _color = b.color
                    ColorUtils.colorToHSL(_color, hsl)
                    scale(icon.foreground)
                }
                is ShapeDrawable -> {
                    _color = b.paint.color
                    ColorUtils.colorToHSL(_color, hsl)
                    scale(icon.foreground)
                }
                is GradientDrawable -> {
                    _color = b.color?.defaultColor?.also { ColorUtils.colorToHSL(it, hsl) } ?: _color
                    scale(icon.foreground)
                }
                else -> {
                    if (b != null) {
                        val palette = Palette.from(b.toBitmap()).generate()
                        val d = palette.dominantSwatch
                        hsl = d?.hsl ?: hsl
                        _color = run {
                            val def = super.getColor()
                            var color = (d ?: return@run def).rgb
                            if (hsl[1] < .1f) {
                                color = palette.getVibrantColor(color)
                            }
                            color
                        }
                    }
                    icon
                }
            }
        } else this.icon = icon

        if (_color == 0) {
            val palette = Palette.from(icon.toBitmap()).generate()
            val d = palette.dominantSwatch
            hsl = d?.hsl ?: hsl
            _color = run {
                val def = super.getColor()
                var color = (d ?: return@run def).rgb
                if (hsl[1] < .1f) {
                    color = palette.getVibrantColor(color)
                }
                color
            }
        }
        this.hsl = hsl
        this._color = _color
    }

    override fun getColor(): Int = _color

    override fun toString() = "$packageName/$name/${userHandle.hashCode()}"

    fun getShortcuts(launcherApps: LauncherApps): List<ShortcutInfo> {
        val shortcutQuery = ShortcutQuery()
        shortcutQuery.setQueryFlags(ShortcutQuery.FLAG_MATCH_DYNAMIC or ShortcutQuery.FLAG_MATCH_MANIFEST or ShortcutQuery.FLAG_MATCH_PINNED)
        shortcutQuery.setPackage(packageName)
        return try {
            launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle())!!
        } catch (e: Exception) { emptyList() }
    }

    inline fun isInstalled(packageManager: PackageManager) = packageManager.isInstalled(packageName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as App
        if (packageName != other.packageName) return false
        if (name != other.name) return false
        if (userHandle != other.userHandle) return false
        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + userHandle.hashCode()
        return result
    }


    companion object {
        fun parse(string: String, appsByName: HashMap<String, MutableList<App>>): App? {
            val (packageName, name, u) = string.split('/')
            val userHandle = u.toInt()
            return appsByName[packageName]?.find {
                it.name == name &&
                it.userHandle.hashCode() == userHandle
            }
        }
    }
}