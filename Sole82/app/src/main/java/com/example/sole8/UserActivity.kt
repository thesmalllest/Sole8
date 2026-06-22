package com.example.sole8

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val toolbar = findViewById<Toolbar>(R.id.detailsToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.userFragmentContainer, UserHubFragment())
            .commit()
    }
}
