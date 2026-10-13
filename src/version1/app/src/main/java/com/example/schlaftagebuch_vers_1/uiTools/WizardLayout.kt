package com.example.schlaftagebuch_vers_1.uiTools

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.dataService.Question
import com.example.schlaftagebuch_vers_1.dataService.QuestionDate
import com.example.schlaftagebuch_vers_1.dataService.QuestionDuration
import com.example.schlaftagebuch_vers_1.dataService.QuestionMultipleChoice
import com.example.schlaftagebuch_vers_1.dataService.QuestionSlider
import com.example.schlaftagebuch_vers_1.dataService.QuestionTime
import com.example.schlaftagebuch_vers_1.dataService.QuestionYesNo


/**
 * Enthält Grundlegende UI für die Fragen
 * Bei Nichtbeantwortung werden Standardwerte gespeichert
 * Unten sind Preview für die verschiedenen Fragen
 */

@OptIn(ExperimentalMaterial3Api::class) // Nötig für TopAppBar
@Composable
fun WizardLayout(
    question: Question,
    answer: String?,
    onAnswerChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isLastQuestion: Boolean,
    stepTitle: String
) {
    // Zurück Geste (Dreieck bzw Pfeil am unteren Bildschirmrand oder nach innen wischen)
    BackHandler(enabled = true) {
        onBack()
    }

    var showHelpDialog by remember { mutableStateOf(false) }

    // Scroll-State für kleine Bildschirme, damit Tastatur nichts verdeckt
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        // TopBar statt manueller Row
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text( // Frage Fortschritt
                        text = stepTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.questions_protocol_back)
                        )
                    }
                },
                actions = {
                    // Info Icon oben rechts in die Leiste integriert -> sieht aufgeräumter aus
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            // Outlined wirkt oft etwas dezenter als Filled
                            imageVector = Icons.Outlined.QuestionMark,
                            contentDescription = stringResource(R.string.questions_protocol_explanation_icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },

        // BottomBar ähnlich
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) // Standard Padding
                        .navigationBarsPadding(), // Respektiert die Android Gesten-Leiste unten
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (answer.isNullOrBlank()) {
                                val defaultValue = getDefaultValueForQuestion(question)
                                onAnswerChanged(defaultValue)
                            }
                            onNext()
                                  },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // Angenehme Touch-Höhe
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = if (isLastQuestion)
                                stringResource(R.string.questions_protocol_btn_finish)
                            else
                                stringResource(R.string.questions_protocol_btn_next),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // Inhalt der Seite
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState) // Scrollbar machen
                .padding(horizontal = 24.dp), // Seitlicher Abstand für den Inhalt
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Frage-Titel Bereich
            Text(
                text = question.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center, // Zentrierte Frage wirkt oft besser im Wizard
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Eingabefelder
            Box( //volle Breite
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (question) {
                    is QuestionSlider -> QuestionSliderUI(question, answer, onAnswerChanged)
                    is QuestionTime -> QuestionTimeUI(question, answer, onAnswerChanged)
                    is QuestionDuration -> QuestionDurationUI(question, answer, onAnswerChanged)
                    is QuestionMultipleChoice -> QuestionMultipleChoiceUI(
                        question,
                        answer,
                        onAnswerChanged
                    )

                    is QuestionYesNo -> QuestionYesNoUI(question, answer, onAnswerChanged)
                    is QuestionDate -> QuestionDateUI(question, answer, onAnswerChanged)
                }
            }

            // Platzhalter unten, damit man auch das letzte Element gut sieht beim Scrollen
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Erklärung Dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.questions_protocol_explanation_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text(stringResource(R.string.questions_protocol_explanation_close))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

// Hilfsfunktion für Standardwert. Wird gespeichert wenn nichts beantwortet
fun getDefaultValueForQuestion(question: Question): String {
    return when (question) {
        is QuestionSlider -> question.sliderMin.toString() //Anfang des Sliders (Spawnposition
        is QuestionTime -> "00:00" // Keine Daten
        is QuestionDuration -> "00:00" // keine Dauer
        is QuestionYesNo -> "" // keine Antwort ausgewählt
        is QuestionMultipleChoice -> "" // keine Antwort ausgewählt
        is QuestionDate -> java.time.LocalDate.now().toString() // Heute
    }
}

/**
 * Ab hier sind die Preview für die Fragen
 * In der Reihenfolge:
 * Slider, Multiple Choice, Bool, Uhrzeit, Dauer, Datum
 */

@Preview(showBackground = true, name = "1. Slider")
@Composable
fun PreviewWizardSlider() {
    val dummy = QuestionSlider(
        id = 1,
        title = "Wie erholt fühlen Sie sich?",
        explanation = "Bewerten Sie Ihre Erholung.",
        sliderMin = 1f,
        sliderMax = 10f
    )
    WizardLayout(
        question = dummy,
        answer = "6.0",
        onAnswerChanged = {},
        onNext = {},
        onBack = {},
        isLastQuestion = false,
        stepTitle = "Frage 1 von 5"
    )
}

@Preview(showBackground = true, name = "2. Multiple Choice")
@Composable
fun PreviewWizardMultipleChoice() {
    val dummy = QuestionMultipleChoice(
        id = 2,
        title = "Was hat Sie gestört?",
        explanation = "Mehrfachauswahl möglich.",
        choices = listOf("Lärm", "Licht", "Gedanken", "Temperatur")
    )
    WizardLayout(
        question = dummy,
        answer = "Lärm,Licht",
        onAnswerChanged = {},
        onNext = {},
        onBack = {},
        isLastQuestion = false,
        stepTitle = "Frage 2 von 5"
    )
}

@Preview(showBackground = true, name = "3. Ja / Nein")
@Composable
fun PreviewWizardYesNo() {
    val dummy = QuestionYesNo(
        id = 3,
        title = "Sind Sie nachts aufgewacht?",
        explanation = "Kurze Wachphasen zählen auch.",
        skipIfNo = emptyList()
    )
    WizardLayout(
        question = dummy,
        answer = "Ja",
        onAnswerChanged = {},
        onNext = {},
        onBack = {},
        isLastQuestion = false,
        stepTitle = "Frage 3 von 5"
    )
}

@Preview(showBackground = true, name = "4. Uhrzeit")
@Composable
fun PreviewWizardTime() {
    val dummy = QuestionTime(
        id = 4,
        title = "Wann ging das Licht aus?",
        explanation = "Bitte genaue Uhrzeit eingeben."
    )
    WizardLayout(
        question = dummy,
        answer = "22:30",
        onAnswerChanged = {},
        onNext = {},
        onBack = {},
        isLastQuestion = false,
        stepTitle = "Frage 4 von 5"
    )
}

@Preview(showBackground = true, name = "5. Dauer")
@Composable
fun PreviewWizardDuration() {
    val dummy = QuestionDuration(
        id = 5,
        title = "Wie lange lagen Sie wach?",
        explanation = "Geschätzte Dauer in Stunden und Minuten."
    )
    WizardLayout(
        question = dummy,
        answer = "00:45",
        onAnswerChanged = {},
        onNext = {},
        onBack = {},
        isLastQuestion = true, // Zeigt "Fertigstellen" Button
        stepTitle = "Frage 5 von 5"
    )
}