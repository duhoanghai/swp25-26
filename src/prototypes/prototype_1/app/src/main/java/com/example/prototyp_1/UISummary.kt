package com.example.prototyp_1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//Speicherseite
@Composable
fun SummaryScreen(
    questions: List<Question>,
    answers: Map<Int, String>,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Zusammenfassung",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            questions.forEach { q ->
                Text(text = "${q.title}:", style = MaterialTheme.typography.labelLarge)
                Text(text = answers[q.id] ?: "---", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons unten
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) {
                Text("Zurück")
            }

            Button(onClick = onSave) {
                Text("Jetzt Speichern")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }




}