package com.example.dailydoes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.util.Calendar

object AlarmUtils {

    fun scheduleAlarm(context: Context, timeInMillis: Long, id: Int, medicineName: String, dose: String) {
        if (timeInMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("medicine_name", medicineName)
        intent.putExtra("medicine_dose", dose)
        intent.putExtra("notification_id", id)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
             alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {

            Toast.makeText(context, "الرجاء تفعيل التنبيهات", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun getTimeLabel(time: String): String {
        return try {
            val hour = time.split(":")[0].toInt()
            when (hour) {
                in 5..11 -> "(صباحاً)"
                in 12..15 -> "(ظهراً)"
                in 16..19 -> "(مساءً)"
                else -> "(ليلاً)"
            }
        } catch (e: Exception) {
            ""
        }
    }
}
