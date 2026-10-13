package com.example.schlaftagebuch_vers_1.helper

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {

    private const val CHANNEL_ID = "schlaftagebuch_alarm_channel"

    //Kanal erstellen
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Erinnerungen"
            val descriptionText = "Erinnerung für das Schlaftagebuch"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    //Tägliche Benachrichtigung
    fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //Berechtigung
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Bitte 'Wecker & Erinnerungen' zulassen", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    setData(Uri.parse("package:${context.packageName}"))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        //Berechnung ob heute oder morgen die Benachrichtigung gesetzt werden soll

        val now = LocalDateTime.now()                 //Aktuelles Datum + Zeit
        val targetTime = LocalTime.of(hour, minute)   //Die Wunsch-Uhrzeit

        //Alarm zur vollen Minute
        var alarmDateTime = now.with(targetTime).withSecond(0).withNano(0)

        //In Vergangenheit?
        if (alarmDateTime.isBefore(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }

        //Systemzeit wegen Zeitzone und Sommer/Winter
        val triggerMillis = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // --- Ende der Berechnung ---

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )

            //Feedback
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            Toast.makeText(context, "Erinnerung für ${targetTime.format(formatter)} Uhr gesetzt", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Fehler beim Setzen des Alarms", Toast.LENGTH_SHORT).show()
        }

    }

    //steuert die Benachrichtigung über Switch
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "Erinnerung deaktiviert", Toast.LENGTH_SHORT).show()
        }
    }
}
