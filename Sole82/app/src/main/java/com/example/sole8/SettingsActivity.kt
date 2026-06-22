package com.example.sole8

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
class SettingsActivity : BaseDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_settings, findViewById(R.id.content_frame))

        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        attachHeader(R.id.logo)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}
