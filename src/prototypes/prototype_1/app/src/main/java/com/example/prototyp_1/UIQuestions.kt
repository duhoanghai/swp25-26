package com.example.prototyp_1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//Frage UI außerhalb für Übersicht
@Composable
fun QuestionSliderUI(
    question: QuestionSlider,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {

    val sliderValue = answer?.toFloatOrNull() ?: question.sliderMin

    // Diese Column zentriert den Slider und den Text darüber
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(text = "Aktueller Wert: ${"%.1f".format(sliderValue)}")

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                onAnswerChanged(newValue.toString())
            },
            valueRange = question.sliderMin..question.sliderMax,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun QuestionTimeUI(
    question: QuestionTime,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    val parts = answer?.split(":")
    val currentHour = parts?.getOrNull(0)?.toIntOrNull() ?: 12
    val currentMinute = parts?.getOrNull(1)?.toIntOrNull() ?: 0

    // --- HIER DIE KORREKTUR ---
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth() // <-- DIESER MODIFIER FEHLT
    ) {
        Text("Gewählte Zeit: ${"%02d".format(currentHour)}:${"%02d".format(currentMinute)}")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelPicker(
                value = currentHour,
                range = 0..23,
                onValueChange = { newHour ->
                    onAnswerChanged("$newHour:$currentMinute")
                }
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            WheelPicker(
                value = currentMinute,
                range = 0..59,
                onValueChange = { newMinute ->
                    onAnswerChanged("$currentHour:$newMinute")
                }
            )
        }
    }
}

@Composable
fun QuestionMultipleChoiceUI(
    question: QuestionMultipleChoice,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    val selectedOptions = answer?.split(",")?.toSet() ?: emptySet()

    Column {
        Text("Bitte auswählen (Mehrfachnennung möglich):")

        //Pro Antwort eine Zeile
        question.choices.forEach { optionText ->
            Row(verticalAlignment = Alignment.CenterVertically) {

                Checkbox(
                    checked = selectedOptions.contains(optionText),

                    onCheckedChange = { isChecked ->
                        val newSelection = if (isChecked) {
                            selectedOptions + optionText // Dazu
                        } else {
                            selectedOptions - optionText // Weg
                        }
                        onAnswerChanged(newSelection.joinToString(","))
                    }
                )
                Text(text = optionText)
            }
        }
    }
}

@Composable
fun QuestionYesNoUI(
    question: QuestionYesNo,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = (answer == "Ja"),
                onClick = { onAnswerChanged("Ja") }
            )
            Text("Ja")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = (answer == "Nein"),
                onClick = { onAnswerChanged("Nein") }
            )
            Text("Nein")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Erlaubt die Nutzung des neuen DatePickers
@Composable
fun QuestionDateUI(
    question: QuestionDate,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    //PopUp Kalender also Verstecken möglich
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        //Datumsanzeige
        Text(
            text = if (answer.isNullOrEmpty()) "Kein Datum gewählt" else "Datum: $answer",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        //Öffnen des Kalender
        Button(onClick = { showDialog = true }) {
            Text("Kalender öffnen")
        }

        //Fenster an sich
        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false }, // Klick daneben schließt
                confirmButton = {
                    TextButton(
                        onClick = {
                            // LOGIK: Millisekunden -> Datum String
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                // Umwandlung in lesbares Format (z.B. 27.11.2025)
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

                                onAnswerChanged(date.format(formatter))
                            }
                            showDialog = false
                        }
                    ) {
                        Text("Übernehmen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            ) {
                //Kalender Inhalt Logik
                DatePicker(state = datePickerState)
            }
        }
    }
}
