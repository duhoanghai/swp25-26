package com.example.schlaftagebuch_vers_1.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.File
import java.io.FileOutputStream

class SensorRecordingService : Service(), SensorEventListener {

    //Konfiguration Hartcodiert
    private val SAMPLING_RATE_HZ = 2
    private val MIN_TIME_BETWEEN_SAMPLES_MS = 1000 / SAMPLING_RATE_HZ // 500ms
    private val FLUSH_INTERVAL_MS = 30 * 60 * 1000L // 30 Minuten

    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null
    private var fileOutputStream: FileOutputStream? = null
    private lateinit var currentFile: File

    // Status-Variablen
    private var lastSavedTimestamp: Long = 0
    private var lastFlushTimestamp: Long = 0

    override fun onCreate() {
        super.onCreate()

        // 1. Datei anlegen
        try {
            val fileName = "sleep_data_${System.currentTimeMillis()}.csv"
            currentFile = File(filesDir, fileName)
            fileOutputStream = FileOutputStream(currentFile, true)
            // Header schreiben
            fileOutputStream?.write("Timestamp,SensorType,Value1,Value2,Value3\n".toByteArray())
            lastFlushTimestamp = System.currentTimeMillis()
        } catch (e: Exception) { e.printStackTrace() }

        // 2. Notification (Pflicht)
        val channelId = "sleep_tracking_channel"
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(channelId, "Sleep Tracking", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Schlaftagebuch")
            .setContentText("Aufzeichnung aktiv (2Hz)")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(1, notification)

        // 3. WakeLock (12h Max)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Schlaftagebuch::Recording")
        wakeLock?.acquire(12 * 60 * 60 * 1000L)

        // 4. Sensoren
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Wir nehmen SENSOR_DELAY_NORMAL (ca 5Hz) und filtern manuell auf 2Hz runter
        registerSensor(Sensor.TYPE_HEART_RATE)
        registerSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun registerSensor(type: Int) {
        sensorManager.getDefaultSensor(type)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val now = System.currentTimeMillis()
            val isHeartRate = it.sensor.type == Sensor.TYPE_HEART_RATE

            if (isHeartRate || (now - lastSavedTimestamp >= MIN_TIME_BETWEEN_SAMPLES_MS)) {

                val typeStr = if (isHeartRate) "HR" else "ACC"
                val v1 = it.values.getOrElse(0) { 0f }

                // Nullen bei HR ignorieren
                if (isHeartRate && v1 == 0f) return

                val v2 = it.values.getOrElse(1) { 0f }
                val v3 = it.values.getOrElse(2) { 0f }

                val line = "$now,$typeStr,$v1,$v2,$v3\n"

                try {
                    fileOutputStream?.write(line.toByteArray())
                } catch (e: Exception) { /* Ignorieren */ }

                if (!isHeartRate) {
                    lastSavedTimestamp = now
                }
            }
        }
    }

    private fun checkPeriodicFlush(now: Long) {
        if (now - lastFlushTimestamp > FLUSH_INTERVAL_MS) {
            try {
                println("30 min Flush: Speichere Daten...")
                fileOutputStream?.flush() // Daten auf die "Festplatte" zwingen

                // Versuch zu senden (Try-Catch wie gewünscht)
                sendFileToPhone(isFinal = false)

                lastFlushTimestamp = now
            } catch (e: Exception) {
                println("Fehler beim Zwischenspeichern/Senden: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        wakeLock?.release()

        try {
            fileOutputStream?.flush()
            fileOutputStream?.close()
        } catch (e: Exception) {}

        //Finales senden
        sendFileToPhone(isFinal = true)
    }

    private fun sendFileToPhone(isFinal: Boolean) {
        if (!::currentFile.isInitialized || !currentFile.exists()) return

        try {
            val asset = com.google.android.gms.wearable.Asset.createFromUri(Uri.fromFile(currentFile))
            val putDataMapRequest = PutDataMapRequest.create("/sleep_data_file")
            putDataMapRequest.dataMap.putAsset("file", asset)
            // Timestamp sorgt dafür, dass das Event immer neu getriggert wird
            putDataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())
            putDataMapRequest.dataMap.putBoolean("isFinal", isFinal)

            val request = putDataMapRequest.asPutDataRequest()
            request.setUrgent() // Versucht es sofort zu senden

            Wearable.getDataClient(this).putDataItem(request)
                .addOnSuccessListener { println("Sync erfolgreich angestoßen") }
            // Kein Failure Listener hier nötig, da "Try" Block drüber liegt
        } catch (e: Exception) {
            println("Sync fehlgeschlagen (Uhr nicht verbunden?): ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
}