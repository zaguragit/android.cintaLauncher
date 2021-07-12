package io.posidon.android.cintalauncher.ui.intro

import io.posidon.android.cintalauncher.R

class SplashFragment : FragmentWithNext(R.layout.intro_splash) {
    override fun next(activity: IntroActivity) {
        activity.setFragment(PermissionsFragment())
    }
}