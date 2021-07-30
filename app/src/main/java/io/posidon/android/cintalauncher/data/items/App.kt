package io.posidon.android.cintalauncher.data.items

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import android.view.View
import androidx.palette.graphics.Palette
import posidon.android.conveniencelib.isInstalled
import posidon.android.conveniencelib.toBitmap
import java.util.*

class App(
    val packageName: String,
    val name: String,
    val userHandle: UserHandle = Process.myUserHandle(),
    override val label: String,
    override val icon: Drawable
) : LauncherItem {

    override var notificationCount = 0

    override fun open(context: Context, view: View?) {
        try {
            context.getSystemService(LauncherApps::class.java).startMainActivity(ComponentName(packageName, name), userHandle, view?.clipBounds,
                ActivityOptions.makeScaleUpAnimation(view, 0, 0, view?.measuredWidth ?: 0, view?.measuredHeight ?: 0).toBundle())
        } catch (e: Exception) { e.printStackTrace() }
    }

    val hsl: FloatArray
    private val _color: Int

    init {
        val palette = Palette.from(icon.toBitmap()).generate()
        val d = palette.dominantSwatch
        hsl = d?.hsl ?: FloatArray(3)
        _color = run {
            val def = super.getColor()
            var color = (d ?: return@run def).rgb
            if (hsl[1] < .1f) {
                color = palette.getVibrantColor(color)
            }
            color
        }
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