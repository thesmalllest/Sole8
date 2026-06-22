package com.example.sole8

import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class CartActivity : BaseDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_cart, findViewById(R.id.content_frame))

        attachHeader(R.id.logo)

        val toolbar = findViewById<Toolbar>(R.id.cartToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        supportFragmentManager.beginTransaction()
            .replace(R.id.cart_container, CartFragment())
            .commit()

        hideBottomBar()
    }
}
