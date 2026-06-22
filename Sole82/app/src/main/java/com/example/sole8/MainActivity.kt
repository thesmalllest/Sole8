package com.example.sole8

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import java.util.Locale
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.PagerSnapHelper
import android.net.Uri
import com.example.sole8.adapters.ProductAdapter
import androidx.lifecycle.lifecycleScope
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : BaseDrawerActivity() {

    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = settingsPrefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_frame))

        prefs = UserPreferences(this)

        attachHeader(R.id.logo)
        showBottomBar()

        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_catalog -> {
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_settings -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    prefs.clear()
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // --- Анимация заголовка ---
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

        if (recyclerView != null) {
            recyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)

            lifecycleScope.launch {
                try {
                    val products = ApiClient.productsApi.getProducts()

                    val limitedProducts = products.take(4)

                    recyclerView.adapter = ProductAdapter(limitedProducts)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
}
