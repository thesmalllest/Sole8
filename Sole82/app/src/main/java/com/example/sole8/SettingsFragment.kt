package com.example.sole8

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppSettings.PREFS_NAME
        setPreferencesFromResource(R.xml.settings_prefs, rootKey)

        val themePref = findPreference<ListPreference>(AppSettings.KEY_THEME)
        val languagePref = findPreference<ListPreference>(AppSettings.KEY_LANGUAGE)

        themePref?.value = AppSettings.getTheme(requireContext())
        languagePref?.value = AppSettings.getLanguage(requireContext())

        themePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        languagePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String

            AppSettings.saveTheme(requireContext(), theme)

            when (theme) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            true
        }

        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val language = newValue as String

            AppSettings.saveLanguage(requireContext(), language)
            AppSettings.restartApp(requireActivity())

            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(
            androidx.preference.R.id.recycler_view
        )

        recyclerView?.setPadding(0, 0, 0, 0)
        recyclerView?.adapter?.notifyDataSetChanged()
    }
}