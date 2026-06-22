package com.example.sole8

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        ApiClient.init(this)

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)  // задержка не блокирует UI!
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}
