package com.example.dailydoes

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class SoundActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var selectedSound = "sound_soft"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupSounds)
        val btnNext = findViewById<Button>(R.id.btnNextSound)


        btnBack.setOnClickListener {
            finish()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            selectedSound = when (checkedId) {
                R.id.rbSoft -> "sound_soft"
                R.id.rbAlarm -> "sound_alarm"
                R.id.rbNature -> "sound_nature"
                R.id.rbStrong -> "sound_strong"
                else -> "sound_soft"
            }

            mediaPlayer?.release()


            var resId = resources.getIdentifier(selectedSound, "raw", packageName)
            if (resId == 0) resId = resources.getIdentifier("sound_soft", "raw", packageName)

            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
            }
        }

        btnNext.setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

            prefs.edit()
                .putString("alarm_sound", selectedSound)
                .putBoolean("sound_done", true)
                .apply()


            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
