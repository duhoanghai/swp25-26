package com.example.schlaftagebuch_vers_1.uiScreens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.helper.NotificationHelper
import com.example.schlaftagebuch_vers_1.dataService.DataLoader
import com.example.schlaftagebuch_vers_1.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun Notification() {
    val context = LocalContext.current

    var isNotificationEnabled by remember { mutableStateOf(DataLoader.loadAlarmState(context)) }
    var selectedTime by remember { mutableStateOf(DataLoader.loadAlarmTime(context)) }

    //Dialog ist erstellt
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->

            selectedTime = LocalTime.of(hour, minute)
            DataLoader.saveAlarmTime(context, selectedTime)

            if (isNotificationEnabled) {
                NotificationHelper.scheduleDailyAlarm(context, hour, minute)
            }

        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    //Berechtigung
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isNotificationEnabled = isGranted
            if (isGranted) {
                NotificationHelper.scheduleDailyAlarm(
                    context,
                    selectedTime.hour,
                    selectedTime.minute
                )
            }
        }
    )

    fun toggleAlarm(shouldEnable: Boolean) {
        isNotificationEnabled = shouldEnable
        DataLoader.saveAlarmState(context, shouldEnable)

        if (shouldEnable) {
            if (Build.VERSION.SDK_INT >= 33) {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    isNotificationEnabled = true
                    NotificationHelper.scheduleDailyAlarm(
                        context,
                        selectedTime.hour,
                        selectedTime.minute
                    )
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                isNotificationEnabled = true
                NotificationHelper.scheduleDailyAlarm(
                    context,
                    selectedTime.hour,
                    selectedTime.minute
                )
            }
        } else {
            isNotificationEnabled = false
            NotificationHelper.cancelAlarm(context)
        }
    }

    //UI
    fun openTimePicker() { //2x also Outsourced
        timePickerDialog.updateTime(selectedTime.hour, selectedTime.minute)
        timePickerDialog.show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        //Zeile 1 Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.notification_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + stringResource(R.string.notification_format_clock),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { openTimePicker() }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        //Zeile 2 Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //links Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = { toggleAlarm(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isNotificationEnabled) {
                        stringResource(R.string.notification_switch_on)
                    } else {
                        stringResource(R.string.notification_switch_off)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            //rechts Zeit Ändern btn
            Button(
                onClick = { openTimePicker() },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.notification_btn_change_time))
            }
        }
    }
}