package com.example.dailydoes

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class


AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "DailyDoes:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L)

        try {
            val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
            val dose = intent.getStringExtra("medicine_dose") ?: ""
            val notificationId = intent.getIntExtra("notification_id", 0)


            val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("medicine_name", medicineName)
                putExtra("medicine_dose", dose)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                fullScreenIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = "medicine_channel_v2"
            

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Medicine Reminders",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for medicine alarms"
                    enableLights(true)
                    enableVibration(true)
                    
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .build()
                        

                    setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                }
                val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }


            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val username = prefs.getString("username", "صديقي") ?: "صديقي"

            val title = "تنبيه الدواء"
            val body = "مرحباً $username، حان وقت شرب: $medicineName \nالجرعة: $dose"

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(fullScreenPendingIntent)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true) 

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
            

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) wakeLock.release()
            }, 3000)

        } catch (e: Exception) {
            e.printStackTrace()
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}
