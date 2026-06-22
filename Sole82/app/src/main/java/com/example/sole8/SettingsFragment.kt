package com.example.sole8

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_prefs, rootKey)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Тема приложения
        val themePref = findPreference<ListPreference>("theme")
        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            prefs.edit().putString("theme", theme).apply()
            when (theme) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            true
        }

        // Применяем сохранённую тему при открытии
        when (prefs.getString("theme", "light")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        // Язык приложения
        findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
            val lang = newValue as String
            prefs.edit().putString("language", lang).apply()
            setLocale(lang)
            Toast.makeText(requireContext(), "Language set to $lang", Toast.LENGTH_SHORT).show()
            requireActivity().recreate()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(androidx.preference.R.id.recycler_view)
        recyclerView?.setPadding(0, 0, 0, 0)
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = requireContext().resources
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
