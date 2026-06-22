package com.example.sole8

import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class FavoritesActivity : BaseDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_favorites, findViewById(R.id.content_frame))

        attachHeader(R.id.logo)

        val toolbar = findViewById<Toolbar>(R.id.favToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        supportFragmentManager.beginTransaction()
            .replace(R.id.favorites_container, FavoritesFragment())
            .commit()

        hideBottomBar()
    }
}
