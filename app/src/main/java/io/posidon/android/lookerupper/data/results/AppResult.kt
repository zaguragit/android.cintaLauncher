package io.posidon.android.lookerupper.data.results

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.view.View
import io.posidon.android.cintalauncher.data.items.App

class AppResult(
    val app: App
) : SearchResult {

    inline val packageName: String get() = app.packageName
    inline val name: String get() = app.name
    override val title: String get() = app.label
    inline val icon: Drawable get() = app.icon

    override var relevance = Relevance(0f)

    inline fun getColor(): Int = app.getColor()

    override fun open(view: View) {
        app.open(view.context, view)
    }

    inline fun getShortcuts(launcherApps: LauncherApps): List<ShortcutInfo> =
        app.getShortcuts(launcherApps)
}