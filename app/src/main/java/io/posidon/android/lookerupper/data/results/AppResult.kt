package io.posidon.android.lookerupper.data.results

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.view.View

class AppResult(
    val packageName: String,
    val name: String,
    val userHandle: UserHandle,
    title: String,
    icon: Drawable
) : SimpleResult(title, icon) {
    override var relevance = Relevance(0f)

    override fun open(view: View) {
        try {
            (view.context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).startMainActivity(
                ComponentName(packageName, name), userHandle, null,
                ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle())
        } catch (e: Exception) { e.printStackTrace() }
    }
}