package com.example.schlaftagebuch_vers_1.uiScreens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.smartwatch.WatchController
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper

@Composable
fun SleepControlScreen(
    onBack: () -> Unit
) {
    // Zurück Geste (Dreieck bzw Pfeil am unteren Bildschirmrand oder nach innen wischen)
    BackHandler(enabled = true) {
        onBack()
    }

    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.sleep_control_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Status
        Text(
            text = if (isRecording) stringResource(R.string.sleep_control_content_recording) else stringResource(R.string.sleep_control_content_ready),
            color = if (isRecording) Color.Green else Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        // START
        Button(
            onClick = {
                WatchController.sendStartCommand(context)
                isRecording = true
                TranslationHelper.toast(R.string.sleep_control_toast_start)
            },
            enabled = !isRecording,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(stringResource(R.string.sleep_control_btn_start))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STOP
        Button(
            onClick = {
                WatchController.sendStopCommand(context)
                isRecording = false
                TranslationHelper.toast(R.string.sleep_control_toast_finish)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.sleep_control_btn_finish))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ZURÜCK
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sleep_control_btn_back))
        }
    }
}