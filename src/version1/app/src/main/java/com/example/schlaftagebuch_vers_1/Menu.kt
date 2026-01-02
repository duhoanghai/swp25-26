package com.example.schlaftagebuch_vers_1


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onStartQuestions: () -> Unit,           //Schlafprotokoll
    onStartPersonalQuestions: () -> Unit,   //Personenbezogene Fragen
    onOpenSettings: () -> Unit,             //Einstellungen
    onLanguageChange: (String) -> Unit,
    currentLanguage: String,
    onExit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(), // Füllt den ganzen Bildschirm
        verticalArrangement = Arrangement.Center, // Alles vertikal zentrieren
        horizontalAlignment = Alignment.CenterHorizontally // Alles horizontal zentrieren
    ) {

        Text(
            text = "Hauptmenü",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp)) // Großer Abstand

        Button( //Fragen
            onClick = onStartQuestions,
            modifier = Modifier.fillMaxWidth(0.7f) // Button nimmt 70% der Breite ein
        ) {
            Text("Fragebogen starten")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button( //Personenbezogene Daten
            onClick = onStartPersonalQuestions,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Persönliche Fragen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button( //Einstellungen
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Einstellungen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth(0.7f)) { //Sprache
            Button(
                onClick = { menuExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sprache: $currentLanguage")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Deutsch") },
                    onClick = {
                        onLanguageChange("DE")
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        onLanguageChange("EN")
                        menuExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button( //Ende
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Beenden")
        }

    }
}