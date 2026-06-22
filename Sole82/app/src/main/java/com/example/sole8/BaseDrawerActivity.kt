package com.example.sole8

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.view.View

open class BaseDrawerActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_drawer)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        setupMenu()
        setupBottomBarClicks()
    }

    protected fun setupMenu() {
        val prefs = UserPreferences(this)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.menu_catalog -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }

                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.menu_account -> {
                    drawerLayout.closeDrawers()

                    if (prefs.isUserLoggedIn()) {
                        // Opening profile
                        startActivity(Intent(this, UserActivity::class.java))
                    } else {
                        // Open Login
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }

                R.id.menu_logout -> {
                    prefs.clear()
                    drawerLayout.closeDrawers()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        updateAccountTitle(prefs)
    }

    protected fun attachHeader(logoId: Int) {
        val logo = findViewById<ImageView>(logoId)
        logo?.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            val username = data?.getStringExtra("username")
            if (username != null) {
                val accountItem = navigationView.menu.findItem(R.id.menu_account)
                accountItem.title = username
            }
        }
    }

    private fun updateAccountTitle(prefs: UserPreferences) {
        val accountItem = navigationView.menu.findItem(R.id.menu_account)
        if (prefs.isUserLoggedIn()) {
            val username = prefs.getUser().username
            accountItem.title = username
        } else {
            accountItem.title = getString(R.string.account_button)
        }
    }

    override fun onResume() {
        super.onResume()
        updateAccountTitle(UserPreferences(this))
    }
    companion object {
        private const val LOGIN_REQUEST_CODE = 1001
    }

    protected fun showBottomBar() {
        val prefs = UserPreferences(this)
        val bar = findViewById<View>(R.id.bottomBar)

        bar?.visibility = if (prefs.isUserLoggedIn()) View.VISIBLE else View.GONE
    }

    protected fun hideBottomBar() {
        findViewById<View>(R.id.bottomBar)?.visibility = View.GONE
    }

    protected fun setupBottomBarClicks() {
        val favBtn = findViewById<View>(R.id.bottomFav)
        val cartBtn = findViewById<View>(R.id.bottomCart)

        favBtn?.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        cartBtn?.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

}
