package com.example.schlaftagebuch_vers_1

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
fun WizardLayout(
    question: Question,
    answer: String?,
    onAnswerChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isLastQuestion: Boolean
) {
    var showHelpDialog by remember {mutableStateOf(false)} //prüft ob Erklärung angefragt wird

    //Scaffold nutzt ein 3-teiliges System, oben/ bzw. unten verankert und das padding
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { //Zurück Knopf
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button( //weiter Knopf
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLastQuestion) "Fertig" else "Weiter")
                }
            }
        }
    ) { innerPadding ->//Inhalt der Seite
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = question.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {showHelpDialog = true}) {
                    Icon(Icons.Filled.Info, contentDescription = "Erklärung anzeigen")
                }
            }
            when (question) {
                is QuestionSlider -> QuestionSliderUI(question, answer, onAnswerChanged)
                is QuestionTime -> QuestionTimeUI(question, answer, onAnswerChanged)
                is QuestionDuration -> QuestionDurationUI(question, answer, onAnswerChanged)
                is QuestionMultipleChoice -> QuestionMultipleChoiceUI(question, answer, onAnswerChanged)
                is QuestionYesNo -> QuestionYesNoUI(question, answer, onAnswerChanged)
                is QuestionDate -> QuestionDateUI(question, answer, onAnswerChanged)
            }



        }

        if (showHelpDialog) { //Extra Fenster für die Erklärung
            AlertDialog(
                onDismissRequest = {showHelpDialog = false},
                title = {
                    Text(text = "Erklärung")
                },
                text = {
                    Text(text = question.explanation)
                },
                confirmButton = {
                    TextButton(
                        onClick = {showHelpDialog = false}
                    ) {
                        Text("Verstanden")
                    }
                }
            )
        }

    }
}