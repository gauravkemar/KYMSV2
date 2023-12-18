package com.kemarport.kyms.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.kemarport.kyms.R
import com.kemarport.kyms.helper.SessionManager

class SplashActivity : AppCompatActivity() {
    private val duration = 1000L
    private lateinit var session: SessionManager
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val actionBar = supportActionBar
        actionBar?.hide()

        session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = session.isAlreadyLoggedIn()
            val isLoggedIn = user["isLoggedIn"]
            Log.d(TAG, "User is logged in: $isLoggedIn")
            //session.saveAdminDetails("10.80.100.160", "6900")
            if (isLoggedIn == true) {
                Intent(this, FragmentHostActivity::class.java).apply {
                    startActivity(this)
                    this@SplashActivity.finish()
                }
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)
                this.finish()
            }
        }, duration)
    }
}