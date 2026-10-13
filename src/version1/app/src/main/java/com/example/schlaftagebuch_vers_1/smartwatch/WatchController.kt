package com.example.schlaftagebuch_vers_1.smartwatch

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object WatchController {

    private const val PATH_START = "/start_tracking"
    private const val PATH_STOP = "/stop_tracking"

    fun sendStartCommand(context: Context) {
        sendCommand(context, PATH_START)
    }

    fun sendStopCommand(context: Context) {
        sendCommand(context, PATH_STOP)
    }

    private fun sendCommand(context: Context, path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //verbundene Uhren
                val nodeClient = Wearable.getNodeClient(context)
                val nodes = nodeClient.connectedNodes.await()

                //Messen start
                val messageClient = Wearable.getMessageClient(context)
                for (node in nodes) {
                    messageClient.sendMessage(node.id, path, null).await()
                    println("Befehl gesendet an: ${node.displayName}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}