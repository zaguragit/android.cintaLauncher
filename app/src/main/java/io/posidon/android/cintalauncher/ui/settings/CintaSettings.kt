package io.posidon.android.cintalauncher.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import io.posidon.android.cintalauncher.R
import io.posidon.android.cintalauncher.storage.Settings
import io.posidon.android.cintalauncher.ui.color.ColorTheme
import io.posidon.android.cintalauncher.util.StackTraceActivity

class CintaSettings : FragmentActivity() {

    val settings = Settings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        StackTraceActivity.init(applicationContext)
        settings.init(applicationContext)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment(settings))
                .commit()
        }
        ColorTheme.onCreate(this)
        loadColors()
    }

    private fun loadColors() {
        window.decorView.setBackgroundColor(ColorTheme.feedBG)
    }

    class SettingsFragment(val settings: Settings) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = DataStore(requireContext(), settings)
            setPreferencesFromResource(R.xml.settings_main, rootKey)
        }

        class DataStore(
            val context: Context,
            val settings: Settings
        ) : PreferenceDataStore() {

            override fun putString(key: String, value: String?) =
                settings.edit(context) {
                    key set value
                }

            override fun putInt(key: String, value: Int) =
                settings.edit(context) {
                    key set value
                }

            override fun getString(key: String, defValue: String?) =
                settings.getString(key) ?: defValue

            override fun getInt(key: String, defValue: Int) =
                settings.getIntOr(key) { defValue }
        }
    }
}