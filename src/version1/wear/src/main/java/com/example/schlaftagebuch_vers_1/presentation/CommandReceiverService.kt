package com.example.schlaftagebuch_vers_1.presentation

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class CommandReceiverService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        // Prüfen, welcher Befehl vom Handy kommt
        when (messageEvent.path) {
            "/start_tracking" -> {
                println("Start-Befehl empfangen!")
                // Startet den eigentlichen Aufnahme-Service
                val intent = Intent(this, SensorRecordingService::class.java)
                startForegroundService(intent)

                // Status der Aufnahme
                val broadcast = Intent("com.example.schlaftagebuch.STATUS_UPDATE")
                broadcast.putExtra("status", "running")
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
            }
            "/stop_tracking" -> {
                println("Stopp-Befehl empfangen!")
                // Stoppt den Aufnahme-Service
                val intent = Intent(this, SensorRecordingService::class.java)
                stopService(intent)

                //Status der Aufnahme
                val broadcast = Intent("com.example.schlaftagebuch.STATUS_UPDATE")
                broadcast.putExtra("status", "stopped")
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
            }
        }
    }
}