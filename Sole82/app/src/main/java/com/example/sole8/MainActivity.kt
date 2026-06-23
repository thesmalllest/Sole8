package com.example.sole8

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.ProductAdapter
import com.example.sole8.network.ApiClient
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : BaseDrawerActivity() {

    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_frame))

        prefs = UserPreferences(this)

        attachHeader(R.id.logo)
        showBottomBar()
        setupMainMenu()

        val heroTitle = findViewById<TextView>(R.id.heroTitle)
        val startColor = ContextCompat.getColor(this, R.color.accent)
        val endColor = ContextCompat.getColor(this, R.color.white)

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener { animator ->
                val animatedColor = animator.animatedValue as Int
                heroTitle.setTextColor(animatedColor)

                val fraction = animator.animatedFraction

                if (fraction > 0.5f) {
                    val glowStrength = (fraction * 40).coerceAtMost(40f)

                    heroTitle.setShadowLayer(
                        glowStrength,
                        0f,
                        0f,
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                } else {
                    heroTitle.setShadowLayer(
                        0f,
                        0f,
                        0f,
                        ContextCompat.getColor(this@MainActivity, android.R.color.transparent)
                    )
                }
            }
        }

        colorAnimator.start()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProducts)

        recyclerView?.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(it)

            lifecycleScope.launch {
                try {
                    val products = ApiClient.productsApi.getProducts()
                    val limitedProducts = products.take(4)

                    it.adapter = ProductAdapter(limitedProducts)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val viewAll = findViewById<TextView>(R.id.viewAll)

        viewAll.setOnClickListener {
            startActivity(Intent(this, CatalogActivity::class.java))
        }

        val phoneText = findViewById<TextView>(R.id.footerPhone)

        phoneText.setOnClickListener {
            val phone = getString(R.string.contact_phone)
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        }

        val addressText = findViewById<TextView>(R.id.footerAddress)

        addressText.setOnClickListener {
            val address = Uri.encode(getString(R.string.store_address))
            val uri = Uri.parse("geo:0,0?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun setupMainMenu() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.menu_catalog -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }

                R.id.menu_settings -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.menu_account -> {
                    drawerLayout.closeDrawers()

                    if (prefs.isUserLoggedIn()) {
                        startActivity(Intent(this, UserActivity::class.java))
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }

                    true
                }

                R.id.menu_logout -> {
                    prefs.clear()
                    drawerLayout.closeDrawers()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }
    }

}