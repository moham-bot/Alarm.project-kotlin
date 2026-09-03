package com.example.dailydoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val cardSound = findViewById<LinearLayout>(R.id.cardSound)
        val cardAbout = findViewById<LinearLayout>(R.id.cardAbout)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        btnBack.setColorFilter(Color.parseColor("#5C6BC0"))

        btnBack.setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        tvUserName.text = prefs.getString("name", "مستخدم")

        cardSound.setOnClickListener {
            startActivity(Intent(this, SoundActivity::class.java))
        }

        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
