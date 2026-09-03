
package com.example.dailydoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val tvAbout = findViewById<TextView>(R.id.tvAbout)

        btnStart.setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            if (prefs.getBoolean("sound_done", false)) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, SoundActivity::class.java))
            }
        }

        tvAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}

