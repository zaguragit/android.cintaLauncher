package io.posidon.android.cintalauncher.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import io.posidon.android.cintalauncher.color.ColorTheme
import io.posidon.android.cintalauncher.storage.Settings

abstract class SettingsActivity : FragmentActivity() {

    val settings = Settings()

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ColorTheme.onCreate(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        settings.init(applicationContext)
        loadColors()

        init(savedInstanceState)
    }

    abstract fun init(savedInstanceState: Bundle?)

    private fun loadColors() {
        window.decorView.setBackgroundColor(ColorTheme.uiBG)
    }
}