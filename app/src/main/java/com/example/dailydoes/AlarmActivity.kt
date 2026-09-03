package com.example.dailydoes

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val name = intent.getStringExtra("medicine_name") ?: ""
        val dose = intent.getStringExtra("medicine_dose") ?: ""
        
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "صديقي") ?: "صديقي"

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        tvHeader.text = "مرحباً $username،\nوقت الدواء!"
        
        findViewById<TextView>(R.id.tvMedicineName).text = name
        findViewById<TextView>(R.id.tvDose).text = dose

        playSound()
        
        timer = object : CountDownTimer(3600000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() { finish() }
        }.start()

        findViewById<Button>(R.id.btnStopAlarm).setOnClickListener {
            finish()
        }
    }

    private fun playSound() {
        try {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val soundName = prefs.getString("alarm_sound", "sound_soft") ?: "sound_soft"
            var resId = resources.getIdentifier(soundName, "raw", packageName)
            if (resId == 0) {
                resId = resources.getIdentifier("sound_soft", "raw", packageName)
            }

            mediaPlayer = MediaPlayer.create(this, resId)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {

             try {
                val alert = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, alert)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
             } catch (e2: Exception) {

             }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        timer?.cancel()
    }
}
