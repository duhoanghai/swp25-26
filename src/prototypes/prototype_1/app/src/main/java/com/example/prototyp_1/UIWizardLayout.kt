package com.example.prototyp_1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WizardLayout(
    question: Question,
    index: Int,
    totalCount: Int,
    answer: String?,
    onAnswerChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        //Infoblock für Fragen
        Spacer(modifier = Modifier.height(40.dp))
        Text("Frage ${index + 1} von $totalCount", modifier = Modifier.padding(horizontal = 16.dp))
        Text(question.title, modifier = Modifier.padding(horizontal = 16.dp))
        Text(question.explanation, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(40.dp))

        //Frage (je nach Index und assets\Questions_XX.json)
        when (question) {
            is QuestionSlider -> QuestionSliderUI(question, answer, onAnswerChanged)
            is QuestionTime -> QuestionTimeUI(question, answer, onAnswerChanged)
            is QuestionMultipleChoice -> QuestionMultipleChoiceUI(question, answer, onAnswerChanged)
            is QuestionYesNo -> QuestionYesNoUI(question, answer, onAnswerChanged)
            is QuestionDate -> QuestionDateUI(question, answer, onAnswerChanged)
        }

        //Buttons nach unten drücken
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            //Button Zurück
            Button(onClick = onBack, enabled = (index > 0)) {
                Text("Zurück")
            }
            //Abstand zwischen Buttons
            Spacer(modifier = Modifier.width(150.dp))

            //Button Weiter
            Button(onClick = onNext, enabled = true) {
                Text( if(index < totalCount - 1) {"Weiter"} else {"abschließen"})
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
