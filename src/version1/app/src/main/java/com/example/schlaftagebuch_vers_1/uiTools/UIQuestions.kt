package com.example.schlaftagebuch_vers_1.uiTools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.dataService.QuestionDate
import com.example.schlaftagebuch_vers_1.dataService.QuestionDuration
import com.example.schlaftagebuch_vers_1.dataService.QuestionMultipleChoice
import com.example.schlaftagebuch_vers_1.dataService.QuestionSlider
import com.example.schlaftagebuch_vers_1.dataService.QuestionTime
import com.example.schlaftagebuch_vers_1.dataService.QuestionYesNo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Enthält die UI für die einzelnen Fragetypen inklusive passender Preview
 */

//Frage UI außerhalb für Übersicht
@Composable
fun QuestionSliderUI(
    question: QuestionSlider,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {

    val sliderValue = answer?.toFloatOrNull() ?: question.sliderMin
    val steps = (question.sliderMax - question.sliderMin).toInt() - 1

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Text(text = stringResource(R.string.ui_question_slider_text) + sliderValue.toInt())

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                onAnswerChanged(newValue.toString())
            },
            valueRange = question.sliderMin..question.sliderMax,
            modifier = Modifier.padding(horizontal = 16.dp),
            steps = steps,
            //Farbe simuliert einen leeren Slider, da Füllung und Hintergrund gleiche Farbe haben
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = MaterialTheme.colorScheme.tertiary,
                activeTickColor = MaterialTheme.colorScheme.outline,
                inactiveTickColor = MaterialTheme.colorScheme.outline,
            )
        )
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
        Text(text = stringResource(R.string.ui_question_multiple_choice_text))

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
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.tertiary,
                        checkmarkColor = MaterialTheme.colorScheme.onTertiary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
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
                onClick = { onAnswerChanged("Ja") },
                modifier = Modifier.scale(1.2f),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(stringResource(R.string.ui_question_yesno_yes))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = (answer == "Nein"),
                onClick = { onAnswerChanged("Nein") },
                modifier = Modifier.scale(1.2f),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(stringResource(R.string.ui_question_yesno_no))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTimeUI(
    question: QuestionTime,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    //
    val parts = answer?.split(":")
    val initH = parts?.getOrNull(0)?.toIntOrNull() ?: 12
    val initM = parts?.getOrNull(1)?.toIntOrNull() ?: 0

    //State für das Ziffernblatt (Uhr)
    val timeState = rememberTimePickerState(
        initialHour = initH,
        initialMinute = initM,
        is24Hour = true
    )

    LaunchedEffect(timeState.hour, timeState.minute) {
        onAnswerChanged("%02d:%02d".format(timeState.hour, timeState.minute))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimePicker(
            state = timeState,
            colors = TimePickerDefaults.colors(
                // Zeiger
                selectorColor = MaterialTheme.colorScheme.tertiary,
                // Hintergrund
                clockDialColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                // Zahlen aktiv und inaktiv
                clockDialSelectedContentColor = MaterialTheme.colorScheme.onTertiary,
                clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                // aktive Box (die man gerade auswählt)
                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.surface,
                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onSurface,
                // inaktive Box
                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDurationUI(
    question: QuestionDuration,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    //Initialwerte aus String parsen oder 0
    val parts = answer?.split(":")
    val initH = parts?.getOrNull(0)?.toIntOrNull() ?: 0
    val initM = parts?.getOrNull(1)?.toIntOrNull() ?: 0

    //State für das Text-Eingabefeld
    val timeState = rememberTimePickerState(
        initialHour = initH,
        initialMinute = initM,
        is24Hour = true
    )

    //Überwacht Änderungen im State und speichert sofort
    LaunchedEffect(timeState.hour, timeState.minute) {
        onAnswerChanged("%02d:%02d".format(timeState.hour, timeState.minute))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.ui_question_duration_text))
        //TimeInput ist das Feld für Tastatureingabe
        TimeInput(
            state = timeState,
            colors = TimePickerDefaults.colors(
                // aktive Box (die man gerade auswählt)
                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.surface,
                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onSurface,
                // inaktive Box
                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDateUI(
    question: QuestionDate,
    answer: String?,
    onAnswerChanged: (String) -> Unit
) {
    //"merken" der eingegebenen Antwort
    val initialMillis = remember(answer) {
        if (!answer.isNullOrEmpty()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val date = LocalDate.parse(answer, formatter)
                date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    //Initialisierung des Kalenders
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    //permanentes Speichern
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis
        if (millis != null) {
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            onAnswerChanged(date.format(formatter))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(16.dp))

        DatePicker(
            state = datePickerState,
        )
    }
}



/**
 * Ab hier sind die Preview für die Fragen
 * In der Reihenfolge:
 * Slider, Multiple Choice, Bool, Uhrzeit, Dauer, Datum
 */


@Preview(showBackground = true, name = "1. Slider")
@Composable
fun PreviewQuestionSlider() {
    val dummy = QuestionSlider(
        id = 1,
        title = "Wie gut hast du geschlafen?",
        explanation = "Bewerten Sie den Schlaf auf einer Skala.",
        sliderMin = 1f,
        sliderMax = 10f
    )
    QuestionSliderUI(question = dummy, answer = "7.0", onAnswerChanged = {})
}

@Preview(showBackground = true, name = "2. Multiple Choice")
@Composable
fun PreviewQuestionMultipleChoice() {
    val dummy = QuestionMultipleChoice(
        id = 2,
        title = "Störfaktoren",
        explanation = "Was hat Sie gestört?",
        choices = listOf("Lärm", "Licht", "Temperatur", "Gedanken")
    )
    QuestionMultipleChoiceUI(question = dummy, answer = "Licht,Gedanken", onAnswerChanged = {})
}

@Preview(showBackground = true, name = "3. Ja / Nein")
@Composable
fun PreviewQuestionYesNo() {
    val dummy = QuestionYesNo(
        id = 3,
        title = "Sind Sie nachts aufgewacht?",
        explanation = "Kurze Wachphasen zählen auch.",
        skipIfNo = listOf(4, 5) // Beispiel für Sprunglogik
    )
    QuestionYesNoUI(question = dummy, answer = "Ja", onAnswerChanged = {})
}

@Preview(showBackground = true, name = "4. Uhrzeit (Time)")
@Composable
fun PreviewQuestionTime() {
    val dummy = QuestionTime(
        id = 4,
        title = "Wann ging das Licht aus?",
        explanation = "Uhrzeit eingeben."
    )
    QuestionTimeUI(question = dummy, answer = "22:45", onAnswerChanged = {})
}

@Preview(showBackground = true, name = "5. Dauer (Duration)")
@Composable
fun PreviewQuestionDuration() {
    val dummy = QuestionDuration(
        id = 5,
        title = "Wie lange lagen Sie wach?",
        explanation = "Geschätzte Dauer."
    )
    QuestionDurationUI(question = dummy, answer = "00:30", onAnswerChanged = {})
}

@Preview(showBackground = true, name = "6. Datum")
@Composable
fun PreviewQuestionDate() {
    val dummy = QuestionDate(
        id = 6,
        title = "Datum der Aufzeichnung",
        explanation = "Bitte Datum wählen."
    )
    QuestionDateUI(question = dummy, answer = "18.05.2025", onAnswerChanged = {})
}