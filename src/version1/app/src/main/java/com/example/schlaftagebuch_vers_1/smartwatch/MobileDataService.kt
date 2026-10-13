package com.example.schlaftagebuch_vers_1.smartwatch

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class MobileDataReceiverService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/sleep_data_file") {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val asset = dataMapItem.dataMap.getAsset("file")

                    if (asset != null) {
                        saveFileFromAsset(asset)
                    }
                }
            }
        }
    }

    private fun saveFileFromAsset(asset: Asset) {
        try {
            // 1. Asset in InputStream umwandeln (blockierender Aufruf ist hier ok, da Service im Hintergrund)
            val assetInputStream: InputStream? = Tasks.await(
                Wearable.getDataClient(this).getFdForAsset(asset)
            ).inputStream

            if (assetInputStream == null) {
                Log.e("MobileReceiver", "Input Stream war null")
                return
            }

            // 2. Zieldatei auf dem Handy erstellen
            val fileName = "received_sleep_data_${System.currentTimeMillis()}.csv"
            val targetFile = File(this.filesDir, fileName)

            // 3. Daten schreiben
            FileOutputStream(targetFile).use { output ->
                assetInputStream.use { input ->
                    input.copyTo(output)
                }
            }

            Log.d("MobileReceiver", "Datei erfolgreich gespeichert: ${targetFile.absolutePath}")

            // OPTIONAL: Broadcast senden, damit die UI weiß, dass Daten da sind
            // (könnte man nutzen, um einen Toast im Screen anzuzeigen)
            val intent = Intent("com.example.schlaftagebuch.DATA_RECEIVED")
            intent.putExtra("filepath", targetFile.absolutePath)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        } catch (e: Exception) {
            Log.e("MobileReceiver", "Fehler beim Speichern der Datei", e)
        }
    }
}