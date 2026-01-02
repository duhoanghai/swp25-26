package com.example.schlaftagebuch_vers_1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.schlaftagebuch_vers_1.api.Session

const val firstTime: Boolean = false

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Session.jwt = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJQYXRpZW50L2Rldi1wYXRpZW50LXRlc3QiLCJ1c2VybmFtZSI6InBhdGllbnRfdGVzdCIsInJvbGUiOiJQQVRJRU5UIiwiaWF0IjoxNzY3Mjc2MDg4LCJleHAiOjE3NjczMTkyODh9.row5dlxCZ7tXt6hqEGzvEZGr8HxLQU9_r2ssPcj6lJdeN4VYBr9ynPQDavwxziKJ" // ВРЕМЕННО! ТОЛЬКО ДЛЯ ТЕСТА, ПОКА НЕТ АВТОРИЗАЦИИ!

        setContent {
            val context = LocalContext.current

            //Sprache laden
            var currentLanguage by remember {
                mutableStateOf(DataLoader.getSavedLanguage(context))
            }

            //Fragen laden
            val allQuestions by remember(currentLanguage) {
                val loadedList = DataLoader.getQuestions(context, currentLanguage)
                mutableStateOf(loadedList.sortedBy { it.id })
            }

            val answers = remember { mutableStateMapOf<Int, String>() }

            val visibleQuestions by remember(allQuestions) {
                derivedStateOf {
                    allQuestions.filter { candidate ->
                        //auf Bool Fälle achten
                        val blockers = allQuestions.filterIsInstance<QuestionYesNo>().filter { boolQ ->
                            answers[boolQ.id] == "Nein" //filtert nach falscher Antwort
                        }
                        //Skip Listen
                        val isHidden = blockers.any { it.skipIfNo.contains(candidate.id) }
                        !isHidden
                    }
                }
            }

            val startDestination = if (firstTime) Screen.PrivacyPolicy else Screen.Menu
            var currentScreen by remember { mutableStateOf(startDestination) }
            var questionIndex by remember { mutableIntStateOf(0) }

            //Auflistung der Seiten
            when (currentScreen) {
                Screen.Menu -> {
                    MainMenuScreen(
                        onStartQuestions = {
                            questionIndex = 0
                            answers.clear()
                            currentScreen = Screen.Questions_Protocol
                        },
                        onStartPersonalQuestions = {
                            currentScreen = Screen.PrivacyPolicy
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLang ->
                            DataLoader.saveLanguage(context, newLang)
                            currentLanguage = newLang
                        },
                        onOpenSettings = {
                            currentScreen = Screen.Settings
                        },
                        onExit = {
                            finish()
                        }
                    )
                }

                Screen.PrivacyPolicy -> {
                    PrivacyPolicyScreen(
                        onAccept = {
                            currentScreen = Screen.Registration
                        }
                    )
                }

                Screen.Registration -> {
                    RegistrationScreen(
                        onFinish = {
                            Toast.makeText(context, "Daten aufgenommen (Simuliert)", Toast.LENGTH_SHORT).show()
                            currentScreen = Screen.Menu
                        }
                    )
                }

                Screen.Questions_Protocol -> {
                    if (visibleQuestions.isNotEmpty()) {
                        // Index-Korrektur falls Liste durch Filter schrumpft
                        if (questionIndex >= visibleQuestions.size) {
                            questionIndex = visibleQuestions.size - 1
                        }

                        val currentQuestion = visibleQuestions[questionIndex]
                        val isLast = questionIndex == visibleQuestions.size - 1

                        WizardLayout(
                            question = currentQuestion,
                            answer = answers[currentQuestion.id],

                            onAnswerChanged = { newAnswer ->
                                answers[currentQuestion.id] = newAnswer
                            },

                            onNext = {
                                if (isLast) {
                                    currentScreen = Screen.Summary
                                } else {
                                    questionIndex++
                                }
                            },

                            onBack = {
                                if (questionIndex > 0) {
                                    questionIndex--
                                } else {
                                    currentScreen = Screen.Menu
                                }
                            },

                            isLastQuestion = isLast
                        )
                    } else {
                        androidx.compose.material3.Text("Fehler: Keine Fragen verfügbar.")
                    }
                }

                Screen.Summary -> {
                    SummaryScreen(
                        questions = visibleQuestions, // Nur sichtbare anzeigen
                        answers = answers,
                        onBack = {
                            currentScreen = Screen.Questions_Protocol
                        },
                        onSave = {
                            // Nur Antworten speichern, die zu sichtbaren Fragen gehören
                            val relevantAnswers = answers.filterKeys { id ->
                                visibleQuestions.any { it.id == id }
                            }

                            val answerList = relevantAnswers.map { (id, value) -> Answer(id, value) }
                            DataLoader.saveAnswers(context, answerList)
                            // 2) отправить на бэкенд
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                try {
                                    val req = com.example.schlaftagebuch_vers_1.api.ProtocolSubmissionRequest(
                                        templateKey = "schlaftagebuch_v1",
                                        locale = "de",
                                        filledAt = java.time.Instant.now().toString(),
                                        answers = answers.entries
                                            .sortedBy { it.key }
                                            .map { com.example.schlaftagebuch_vers_1.api.ProtocolAnswerDto(it.key, it.value) }
                                    )

                                    val resp = com.example.schlaftagebuch_vers_1.api.ApiClient.protocolApi.submit(req)

                                    // показать тост надо на Main thread
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Auf Server gespeichert! ID=${resp.submissionId}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }

                                } catch (e: Exception) {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Server-Fehler: ${e.message}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                            Toast.makeText(context, "Gespeichert!", Toast.LENGTH_SHORT).show()
                            currentScreen = Screen.Menu
                        }
                    )
                }

                Screen.Settings -> {
                    androidx.compose.material3.Text("Dieses Gebiet wurde noch nicht erkundet.")
                }

                else -> {}
            }
        }
    }
}