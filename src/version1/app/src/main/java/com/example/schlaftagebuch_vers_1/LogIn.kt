package com.example.schlaftagebuch_vers_1

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(
    onAccept: () -> Unit
) {
    var isAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Datenschutz", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // Platzhalter-Text
        Text("Hier steht der Datenschutztext (wird später geladen)...")

        Spacer(modifier = Modifier.height(24.dp))

        // Checkbox-Zeile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = { isAccepted = it }
            )
            Text("Ich akzeptiere die Datenschutzrichtlinien")
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Weiter-Button (Nur aktiv, wenn Haken gesetzt)
        Button(
            onClick = onAccept,
            enabled = isAccepted // <--- Die Sperre
        ) {
            Text("Weiter")
        }
    }
}

// Screen 2: Anmeldung (Minimum: 3 Felder + Weiter)
@Composable
fun RegistrationScreen(
    onFinish: () -> Unit
) {
    // Leere Variablen, nur damit man tippen kann
    var name by remember { mutableStateOf("") }
    var vorname by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Anmeldung", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = vorname, onValueChange = { vorname = it }, label = { Text("Vorname") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onFinish) {
            Text("Weiter")
        }
    }
}