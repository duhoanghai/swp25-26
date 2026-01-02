package com.example.prototyp_1

//Hauptfunktionen gebündelt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import com.example.prototyp_1.fhir.FhirMapper






//Weil 1. Prototyp hier "variable" Sprache
//Ist global und unveränderbar, Sprachabfrage in der App kommt später
//Questions_DE.json, Questions_EN.json
const val QUESTION_FILE_NAME: String = "Questions_DE.json"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Fragen laden und sortieren nach ID
        val rawQuestions = DataLoader.getQuestions(this)
        val questions = rawQuestions.sortedBy { it.id }

        setContent {
            val answers = remember { mutableStateMapOf<Int, String>() }
            var questionIndex by remember { mutableIntStateOf(0) }

            // --- KORREKTUR 1: Context hier abrufen ---
            val context = LocalContext.current

            //Hauptbedingung: Fragen sind vorhanden
            if (questions.isNotEmpty()) {
                //Nebenbedingung: Fragen sin durch
                if (questionIndex < questions.size) {
                    val currentQuestion = questions[questionIndex]

                    // Der Aufruf wird hier um die fehlenden Parameter ergänzt
                    WizardLayout(
                        question = currentQuestion,
                        index = questionIndex,
                        totalCount = questions.size,
                        onNext = {questionIndex++},
                        onBack = {
                            //nur Index verringern wenn nicht am Anfang
                            if (questionIndex > 0) questionIndex--
                        },
                        // Fehlender Parameter 'answer' hinzugefügt
                        answer = answers[currentQuestion.id],
                        // Fehlender Parameter 'onAnswerChanged' hinzugefügt
                        onAnswerChanged = { newAnswer ->
                            answers[currentQuestion.id] = newAnswer
                        }
                    )
                } else {
                    SummaryScreen(
                        questions = questions,
                        answers = answers,
                        onBack = { questionIndex-- }, // Zurück zur letzten Frage
                        onSave = {
                            // --- KORREKTUR 2: Die bereits abgerufene 'context'-Variable verwenden ---

                            //Speichern
                            val answerList = answers.map { (id, value) -> Answer(id, value) }
                            DataLoader.saveAnswers(context, answerList)

                            // 2. FHIR-Mapping
                            val patientId = "100"// hardcodiert

                            val fhirObservations = questions.mapNotNull { q ->
                                val raw = answers[q.id] ?: return@mapNotNull null
                                FhirMapper.mapToFhir(q, raw, patientId)
                            }

                            // 3. Lokales Speichern der FHIR-Daten (als Backup)
                            DataLoader.saveFhirObservations(context, fhirObservations)





                            //Feedback
                            android.widget.Toast.makeText(context, "Datei gespeichert!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    )
                }

            } else {
                Text("Keine Fragen vorhanden.")
            }
        }

    }

}







