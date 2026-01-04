package com.example.schlaftagebuch_vers_1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolAnswerDto
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolSubmissionRequest
import java.time.Instant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ВАЖНО: убрали тестовый JWT, иначе логика first-login никогда не будет честной
        // Session.jwt = "..."  // НЕ НУЖНО

        setContent {
            val context = LocalContext.current

            // 1) Загружаем сохранённый токен
            val savedJwt = remember {
                com.example.schlaftagebuch_vers_1.api.SessionStorage.loadJwt(context)
            }

            // 2) Стартовый экран: если токен есть -> Menu, иначе -> PrivacyPolicy
            var currentScreen by remember {
                mutableStateOf(
                    if (savedJwt.isNullOrBlank()) Screen.PrivacyPolicy else Screen.Menu
                )
            }

            // 3) Прокидываем токен в Session при старте
            LaunchedEffect(savedJwt) {
                Session.jwt = savedJwt
            }

            // ========= Onboarding / First Login flow state =========
            var consentAccepted by remember { mutableStateOf(false) }
            var createdJwt by remember { mutableStateOf<String?>(null) }
            var createdUsername by remember { mutableStateOf<String?>(null) }

            // ========= Sprache laden =========
            var currentLanguage by remember {
                mutableStateOf(DataLoader.getSavedLanguage(context))
            }

            // ========= Fragen laden =========
            val allQuestions by remember(currentLanguage) {
                val loadedList = DataLoader.getQuestions(context, currentLanguage)
                mutableStateOf(loadedList.sortedBy { it.id })
            }

            val answers = remember { mutableStateMapOf<Int, String>() }

            val visibleQuestions by remember(allQuestions) {
                derivedStateOf {
                    allQuestions.filter { candidate ->
                        val blockers = allQuestions
                            .filterIsInstance<QuestionYesNo>()
                            .filter { boolQ -> answers[boolQ.id] == "Nein" }

                        val isHidden = blockers.any { it.skipIfNo.contains(candidate.id) }
                        !isHidden
                    }
                }
            }

            var questionIndex by remember { mutableIntStateOf(0) }

            // ========= Auflistung der Seiten =========
            when (currentScreen) {

                // 1) Datenschutz -> дальше FirstLogin
                Screen.PrivacyPolicy -> {
                    PrivacyPolicyScreen { accepted ->
                        consentAccepted = accepted
                        currentScreen = Screen.FirstLogin
                    }
                }

                // 2) Первый вход: код + имя/фамилия/др + пароль -> получаем jwt + username
                Screen.FirstLogin -> {
                    FirstLoginScreen(
                        consentAccepted = consentAccepted,
                        onSuccess = { jwt, username ->
                            createdJwt = jwt
                            createdUsername = username
                            currentScreen = Screen.CredentialsInfo
                        }
                    )
                }

                // 3) Показываем username, просим сохранить -> сохраняем jwt и пускаем в Menu
                Screen.CredentialsInfo -> {
                    CredentialsInfoScreen(
                        username = createdUsername ?: "unbekannt",
                        onContinue = {
                            val jwt = createdJwt
                            if (!jwt.isNullOrBlank()) {
                                Session.jwt = jwt
                                com.example.schlaftagebuch_vers_1.api.SessionStorage.saveJwt(context, jwt)
                            }
                            currentScreen = Screen.Menu
                        }
                    )
                }

                // 4) Главное меню (разблокировано)
                Screen.Menu -> {
                    MainMenuScreen(
                        onStartQuestions = {
                            questionIndex = 0
                            answers.clear()
                            currentScreen = Screen.Questions_Protocol
                        },
                        onStartPersonalQuestions = {
                            // если у вас есть отдельный экран — поменяй сюда нужный Screen.*
                            currentScreen = Screen.Questions_Personal
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

                // (Пока заглушка, чтобы не было "пустого" перехода)
                Screen.Questions_Personal -> {
                    androidx.compose.material3.Text("Personal questions: coming soon.")
                }

                Screen.Questions_Protocol -> {
                    if (visibleQuestions.isNotEmpty()) {
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
                        questions = visibleQuestions,
                        answers = answers,
                        onBack = {
                            currentScreen = Screen.Questions_Protocol
                        },
                        onSave = {
                            val relevantAnswers = answers.filterKeys { id ->
                                visibleQuestions.any { it.id == id }
                            }

                            val answerList = relevantAnswers.map { (id, value) -> Answer(id, value) }
                            DataLoader.saveAnswers(context, answerList)

                            // отправка на бэкенд
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                try {
                                    val req = ProtocolSubmissionRequest(
                                        templateKey = "schlaftagebuch_v1",
                                        locale = "de",
                                        filledAt = Instant.now().toString(),
                                        answers = answers.entries
                                            .sortedBy { it.key }
                                            .map { ProtocolAnswerDto(it.key, it.value) }
                                    )

                                    val resp = com.example.schlaftagebuch_vers_1.api.ApiClient
                                        .protocolApi
                                        .submit(req)

                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Auf Server gespeichert! ID=${resp.submissionId}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                } catch (e: Exception) {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Server-Fehler: ${e.message}",
                                            Toast.LENGTH_LONG
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
