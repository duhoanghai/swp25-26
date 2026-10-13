package com.example.schlaftagebuch_vers_1.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R

@Composable
fun LanguageScreen(
    currentLanguageCode: String,
    onLanguageChange: (String) -> Unit,
    onNext: () -> Unit
) {
    //remember entfernt da logisch Knick im Kopf
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.language_select_title),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.language_select_subtitle),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.padding(end = 16.dp)) {
            listOf(
                "DE" to "Deutsch",
                "EN" to "English",
                "RU" to "Русский"
            ).forEach { (code, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(// welche Sprache ist ausgewählt
                        selected = (currentLanguageCode == code),
                        onClick = {
                            onLanguageChange(code)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                            .scale(1.5f)
                    )
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.scale(1.2f))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(0.7f),
            onClick = { onNext() }
        ) {
            Text(
                stringResource(R.string.language_select_btn_next),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SimpleLanguageScreenPreview() {
    LanguageScreen(
        onNext = {},
        currentLanguageCode = "DE",
        onLanguageChange = {}
    )
}