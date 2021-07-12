package io.posidon.android.cintalauncher.ui.intro

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.ui.LauncherActivity

class QuickSettingsFragment : FragmentWithNext(R.layout.intro_settings) {
    override fun next(activity: IntroActivity) {
        val home = ComponentName(requireContext(), LauncherActivity::class.java)
        requireContext().packageManager.setComponentEnabledSetting(home, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        val intro = ComponentName(requireContext(), IntroActivity::class.java)
        requireContext().packageManager.setComponentEnabledSetting(intro, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        startActivity(Intent(requireContext(), LauncherActivity::class.java))
    }
}