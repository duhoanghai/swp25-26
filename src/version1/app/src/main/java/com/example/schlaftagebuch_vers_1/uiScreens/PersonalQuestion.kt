package com.example.schlaftagebuch_vers_1.uiScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.dataService.DataLoader
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper

@Composable
fun QuestionsPersonalScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    //Navigation und Speichern, ähnlich wie Menü
    fun onSaveAndContinue() {
        DataLoader.setFirstTimeCompleted(context)
        TranslationHelper(R.string.questions_personal_toast_saved)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.questions_personal_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(64.dp))

        //AlarmUI (groß, deshalb eigene Datei)
        Notification()

        Spacer(modifier = Modifier.height(32.dp))

        //speichern
        Button(
            onClick = {
                onSaveAndContinue()
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.questions_protocol_btn_next),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
