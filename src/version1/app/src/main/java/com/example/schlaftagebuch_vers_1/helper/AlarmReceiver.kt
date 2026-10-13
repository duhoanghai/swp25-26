package com.example.schlaftagebuch_vers_1.helper

//Für Benachrichtigung

import android.R
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Benachrichtigung bauen
        val notification = NotificationCompat.Builder(context, "schlaftagebuch_alarm_channel")
            .setSmallIcon(R.drawable.ic_lock_idle_alarm) //Standard Icon
            .setContentTitle(context.getString(com.example.schlaftagebuch_vers_1.R.string.alert_message_title))
            .setContentText(context.getString(com.example.schlaftagebuch_vers_1.R.string.alert_message_content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        //Anzeigen der Mittelung. 1 ist die ID, die später auch gelöscht wird
        try {
            notificationManager.notify(1, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}