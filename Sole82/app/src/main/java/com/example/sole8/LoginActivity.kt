package com.example.sole8

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.sole8.models.api.UserLoginRequest
import com.example.sole8.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {

    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = UserPreferences(this)

        val toolbar = findViewById<Toolbar>(R.id.loginToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val emailEdit = findViewById<TextInputEditText>(R.id.emailInput)
        val passEdit = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<TextView>(R.id.registerBtn)
        val loginTitle = findViewById<TextView>(R.id.loginTitle)

        loginTitle.alpha = 0f

        ObjectAnimator.ofFloat(loginTitle, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            start()
        }

        loginBtn.setOnClickListener {
            val email = emailEdit.text?.toString()?.trim() ?: ""
            val password = passEdit.text?.toString()?.trim() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.login_fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            loginUser(email, password)
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        val request = UserLoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.userApi.login(request)

                prefs.saveToken(response.token)
                prefs.saveUserFromApi(response.user)
                prefs.setUserLoggedIn(true)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_welcome_format, response.user.firstName),
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

            } catch (e: Exception) {
                Log.e("LOGIN_ERROR", "Log in Error", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_incorrect),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}