package com.example.schlaftagebuch_vers_1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// --- API Imports ---
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.SessionStorage
import com.example.schlaftagebuch_vers_1.api.ApiClient
import com.example.schlaftagebuch_vers_1.api.JwtUtils
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolAnswerDto
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolSubmissionRequest

// --- Helper Imports ---
import com.example.schlaftagebuch_vers_1.helper.NotificationHelper
import com.example.schlaftagebuch_vers_1.helper.updateLocale
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper

// --- Data Services ---
import com.example.schlaftagebuch_vers_1.dataService.Answer
import com.example.schlaftagebuch_vers_1.dataService.DataLoader
import com.example.schlaftagebuch_vers_1.dataService.DataLoader.isFirstTime
import com.example.schlaftagebuch_vers_1.dataService.QuestionYesNo

// --- Screens ---
import com.example.schlaftagebuch_vers_1.ui.theme.SchlaftagebuchColorTheme
import com.example.schlaftagebuch_vers_1.uiScreens.MainMenuScreen
import com.example.schlaftagebuch_vers_1.uiScreens.QuestionsPersonalScreen
import com.example.schlaftagebuch_vers_1.uiScreens.SummaryScreen
import com.example.schlaftagebuch_vers_1.uiScreens.PrivacyPolicyScreen
import com.example.schlaftagebuch_vers_1.uiScreens.CredentialsInfoScreen
import com.example.schlaftagebuch_vers_1.uiScreens.FirstLoginScreen
import com.example.schlaftagebuch_vers_1.uiScreens.SettingsScreen
import com.example.schlaftagebuch_vers_1.uiScreens.SleepControlScreen
import com.example.schlaftagebuch_vers_1.uiScreens.SyncEvaluationScreen
import com.example.schlaftagebuch_vers_1.uiScreens.AnalysisResultScreen
import com.example.schlaftagebuch_vers_1.uiScreens.AuthChoiceScreen
import com.example.schlaftagebuch_vers_1.uiScreens.LanguageScreen
import com.example.schlaftagebuch_vers_1.uiScreens.LoginScreen
import com.example.schlaftagebuch_vers_1.uiTools.WizardLayout
import com.example.schlaftagebuch_vers_1.uiTools.Screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.time.Instant


/**
 * MainActivity = Einstiegspunkt der Android-App.
 *
 * Verantwortung:
 * - Setzt Compose-UI (setContent)
 * - Steuert den Screen-Flow (First Login / Menu / Questions / Summary)
 * - Lädt gespeichertes JWT und setzt Session.jwt
 * - Sammeln von Antworten und Absenden des Protokolls an Backend
 * - (Neu) Erweiterte Token-Prüfung und Lifecycle-Management (Auth-Refresh)
 */

// jetzt Public, da für UI benötigt, falls irgenetwas schiefgehen sollte Standardmäßig hell
var useDarkTheme = false

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TranslationHelper.init(this)

        NotificationHelper.createNotificationChannel(this)

        val savedLanguage = DataLoader.getLanguage(this)
        updateLocale(this, savedLanguage)

        // val appTheme = DataLoader.getColorTheme(this) // Wird unten im State verwaltet

        setContent {
            // Android Context (für Storage/Toast)
            val context = LocalContext.current
            var currentAppTheme by remember {
                mutableStateOf(DataLoader.getColorTheme(context))
            }

            // 1) Gespeichertes JWT aus lokalem Speicher laden (falls vorhanden)
            // - JWT beim Start einmal laden
            val savedJwt = SessionStorage.loadJwt(context)

            // - Entscheidung: Token gültig oder nicht (Erweiterte Prüfung mit JwtUtils)
            val jwtMissing = savedJwt.isNullOrBlank()
            val jwtExpired = !jwtMissing && JwtUtils.isExpired(savedJwt!!)
            val jwtValid = !jwtMissing && !jwtExpired

            // Session.jwt beim Start setzen, damit API-Calls den Token nutzen können (Nur wenn valid)
            LaunchedEffect(savedJwt) {
                if (jwtValid) {
                    Session.jwt = savedJwt
                }
            }

            // 2) Entscheidung: Login Flow oder Direkt ins Menü?
            // - Start-Screen festlegen -> Wenn Token valid - Menu, sonst AuthChoice
            // (War früher: Screen.PrivacyPolicy vs Screen.Menu)
            var startDestination = if (jwtValid) Screen.Menu else Screen.AuthChoice

            if (isFirstTime(context = context)) {
                startDestination = Screen.Language
            }

            var currentScreen by remember { mutableStateOf(startDestination) }
            var selectedAnalysisFile by remember { mutableStateOf<File?>(null) }
            var questionIndex by remember { mutableIntStateOf(0) }
            var consentAccepted by remember { mutableStateOf(false) }

            // Zustände für Login/Registrierung
            var createdJwt by remember { mutableStateOf<String?>(null) }
            var createdUsername by remember { mutableStateOf<String?>(null) }

            var currentLanguage by remember { mutableStateOf(DataLoader.getLanguage(context)) }
            var currentThemeString by remember { mutableStateOf(DataLoader.getColorTheme(context)) }

            // Neuer State: Merkt sich, ob ein Submit ausstehend ist (falls Token-Refresh nötig war)
            var pendingSubmit by remember { mutableStateOf(false) }

            // LifecycleOwner für ON_START Hook (Token beim Zurückkehren in App prüfen)
            val lifecycleOwner = LocalLifecycleOwner.current

            /**
             * Re-Check beim App-Start/Resume:
             * - JWT neu aus Storage lesen
             * - wenn gültig: Session.jwt setzen und ins Menu
             * - wenn ungültig: Session leeren + Storage löschen + AuthChoice anzeigen
             *
             * Das verhindert, dass man im Menu landet, obwohl das Token abgelaufen ist.
             */
            fun reevaluateAuthAndNavigate() {
                if (currentScreen == Screen.Language) return

                val jwt = SessionStorage.loadJwt(context)
                val isMissing = jwt.isNullOrBlank()
                val isExpired = !isMissing && JwtUtils.isExpired(jwt!!)
                val isValid = !isMissing && !isExpired

                // Prüfen beim Start, ob jwt gültig ist.
                if (isValid) {
                    Session.jwt = jwt
                    // Wenn wir auf AuthChoice oder Login waren, aber Token jetzt valid ist -> Menu (außer Submit pending)
                    if (currentScreen == Screen.AuthChoice || currentScreen == Screen.Login) {
                        if (!pendingSubmit) currentScreen = Screen.Menu
                    }
                } else {
                    Session.jwt = null
                    // Wenn nicht - AuthChoiceScreen zeigen.
                    SessionStorage.clear(context)

                    // Nur Navigieren, wenn wir nicht gerade mitten in der Registrierung sind
                    if (currentScreen != Screen.Language && //... oder gerade die Sprache gewählt wird
                        currentScreen != Screen.AuthChoice &&
                        currentScreen != Screen.FirstLogin &&
                        currentScreen != Screen.PrivacyPolicy &&
                        currentScreen != Screen.Login
                    ) {
                        currentScreen = Screen.AuthChoice
                    }
                }
            }

            /**
             * Lifecycle Observer:
             * - Auf ON_START prüfen wir den Token-Status erneut.
             * (Wichtig z.B. wenn App im Hintergrund war und Token abläuft.)
             */

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        reevaluateAuthAndNavigate()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            useDarkTheme = when (currentThemeString) {
                "dark" -> true
                "bright" -> false
                else -> isSystemInDarkTheme()
            }

            SchlaftagebuchColorTheme(useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    key(currentLanguage) {

                        val allQuestions by remember(currentLanguage) {
                            val loadedList = DataLoader.getQuestions(context, currentLanguage)
                            mutableStateOf(loadedList.sortedBy { it.id })
                        }

                        val answers = remember { mutableStateMapOf<Int, String>() }

                        // Filterung (Ja/Nein Logik)
                        val visibleQuestions by remember(allQuestions) {
                            derivedStateOf {
                                allQuestions.filter { candidate ->
                                    val blockers = allQuestions
                                        .filterIsInstance<QuestionYesNo>()
                                        .filter { boolQ -> answers[boolQ.id] == "Nein" }
                                    val isHidden =
                                        blockers.any { it.skipIfNo.contains(candidate.id) }
                                    !isHidden
                                }
                            }
                        }

                        // 3) Screenauswahl
                        when (currentScreen) {

                            // neuer Einstieg Sprachauswahl
                            Screen.Language -> {
                                LanguageScreen(
                                    currentLanguageCode = currentLanguage,

                                    onLanguageChange = { newCode ->
                                        DataLoader.saveLanguage(context, newCode)
                                        updateLocale(context, newCode)
                                        currentLanguage = newCode
                                        TranslationHelper.init(context)
                                    },
                                    //onNext = { currentScreen = Screen.AuthChoice } Debug
                                    onNext = { currentScreen = Screen.AuthChoice }
                                )
                            }


                            // User wählt Login oder Aktivierung (FirstLogin)
                            Screen.AuthChoice -> {
                                AuthChoiceScreen(
                                    onLogin = {
                                        currentScreen = Screen.Login
                                    }, // Einwilligung → firstLogin
                                    onActivate = { currentScreen = Screen.PrivacyPolicy }
                                )
                            }

                            // Datenschutzerklärung
                            Screen.PrivacyPolicy -> {
                                PrivacyPolicyScreen(
                                    onAccept = {
                                        consentAccepted = true
                                        // Logik Änderung: Erst Registrierung (FirstLogin), dann Info
                                        currentScreen = Screen.FirstLogin
                                    }
                                )
                            }

                            // First-Login / Aktivierung via Code + persönliche Daten
                            Screen.FirstLogin -> {
                                FirstLoginScreen(
                                    consentAccepted = consentAccepted,
                                    onSuccess = { jwt, username ->
                                        createdJwt = jwt
                                        createdUsername = username

                                        // JWT in Session + Storage speichern, damit ApiClient authen kann
                                        if (!jwt.isNullOrBlank()) {
                                            Session.jwt = jwt // Für ApiClient
                                            SessionStorage.saveJwt(context, jwt) // Für App-Neustart
                                        }

                                        DataLoader.saveStoredUsername(context, createdUsername ?: "unbekannt")

                                        currentScreen = Screen.CredentialsInfo
                                    }
                                )
                            }

                            // Login (mit Username+Password) - Neu hinzugefügt
                            Screen.Login -> {
                                LoginScreen(
                                    initialUsername = DataLoader.getStoredUsername(context),
                                    onSuccess = { jwt, _ ->
                                        if (!jwt.isNullOrBlank()) {
                                            Session.jwt = jwt
                                            SessionStorage.saveJwt(context, jwt)
                                        }
                                        // Wenn Speicherung von Protocol wegen Token abgebrochen wurde (pendingSubmit)
                                        currentScreen =
                                            if (pendingSubmit) Screen.Summary else Screen.Menu
                                    }
                                )
                            }

                            Screen.CredentialsInfo -> {
                                CredentialsInfoScreen(
                                    username = createdUsername ?: "unbekannt",
                                    onContinue = {
                                        currentScreen = Screen.Questions_Personal
                                    }
                                )
                            }

                            Screen.Questions_Personal -> {
                                QuestionsPersonalScreen(
                                    onFinished = {
                                        currentScreen = Screen.Menu
                                    }
                                )
                            }

                            Screen.SleepControl -> {
                                SleepControlScreen(
                                    onBack = {
                                        currentScreen = Screen.Menu
                                    }
                                )
                            }

                            Screen.SyncEvaluation -> {
                                SyncEvaluationScreen(
                                    onBack = {
                                        currentScreen = Screen.Questions_Protocol
                                    },
                                    onAnalyze = { file ->
                                        selectedAnalysisFile = file
                                        currentScreen = Screen.AnalysisResult
                                    },
                                    onBreak = {
                                        currentScreen = Screen.Summary
                                    }
                                )
                            }

                            Screen.SyncEvaluationMenu -> {
                                SyncEvaluationScreen(
                                    onBack = {
                                        currentScreen = Screen.Menu
                                    },
                                    onAnalyze = { file ->
                                        selectedAnalysisFile = file
                                        currentScreen = Screen.AnalysisResultMenu
                                    },
                                    onBreak = {
                                        currentScreen = Screen.Menu
                                    }
                                )
                            }

                            Screen.AnalysisResult -> {
                                // Sicherheitscheck, falls Variable leer ist
                                val file = selectedAnalysisFile
                                if (file != null) {
                                    AnalysisResultScreen(
                                        dataFile = file,
                                        onContinue = {
                                            currentScreen = Screen.Summary
                                        },
                                        onBack = {
                                            currentScreen = Screen.SyncEvaluation
                                        }
                                    )
                                } else {
                                    // Fallback, falls was schief ging und man irgendwie in diesem Screen landet
                                    Text("Error: No Data")
                                    Button(onClick = {
                                        currentScreen = Screen.SyncEvaluation
                                    }) { Text("Back") }
                                }
                            }

                            Screen.AnalysisResultMenu -> {
                                // Sicherheitscheck, falls Variable leer ist
                                val file = selectedAnalysisFile
                                if (file != null) {
                                    AnalysisResultScreen(
                                        dataFile = file,
                                        onContinue = {
                                            currentScreen = Screen.Menu
                                        },
                                        onBack = {
                                            currentScreen = Screen.SyncEvaluationMenu
                                        }
                                    )
                                } else {
                                    // Fallback, falls was schief ging und man irgendwie in diesem Screen landet
                                    Text("Error: No Data")
                                    Button(onClick = {
                                        currentScreen = Screen.SyncEvaluationMenu
                                    }) { Text("Back") }
                                }
                            }

                            Screen.Menu -> {
                                MainMenuScreen(
                                    onStartSleep = {
                                        currentScreen = Screen.SleepControl
                                    },
                                    onStartQuestions = {
                                        questionIndex = 0
                                        answers.clear()
                                        currentScreen = Screen.Questions_Protocol
                                    },
                                    onOpenSettings = {
                                        currentScreen = Screen.Settings
                                    },
                                    onStartSync = {
                                        currentScreen = Screen.SyncEvaluationMenu
                                    }
                                )
                            }

                            Screen.Settings -> {
                                SettingsScreen(
                                    currentThemeString = currentThemeString,
                                    onThemeChanged = { newThemeString ->
                                        DataLoader.saveColorTheme(context, newThemeString)
                                        currentThemeString = newThemeString
                                    },
                                    onBack = {
                                        currentScreen = Screen.Menu
                                    },
                                    currentLanguageCode = currentLanguage,

                                    onLanguageChange = { newCode ->
                                        DataLoader.saveLanguage(context, newCode)
                                        updateLocale(context, newCode)
                                        currentLanguage = newCode
                                        TranslationHelper.init(context)
                                    },
                                )
                            }

                            Screen.Questions_Protocol -> {
                                if (visibleQuestions.isNotEmpty()) {
                                    if (questionIndex >= visibleQuestions.size) {
                                        questionIndex = visibleQuestions.size - 1
                                    }

                                    val currentQuestion = visibleQuestions[questionIndex]
                                    val isLast = questionIndex == visibleQuestions.size - 1
                                    val progressTitle =
                                        "${questionIndex + 1} / ${visibleQuestions.size}"

                                    WizardLayout(
                                        question = currentQuestion,
                                        answer = answers[currentQuestion.id],
                                        onAnswerChanged = { answers[currentQuestion.id] = it },
                                        onNext = {
                                            if (isLast) currentScreen =
                                                Screen.SyncEvaluation else questionIndex++
                                        },
                                        onBack = {
                                            if (questionIndex > 0) questionIndex-- else currentScreen =
                                                Screen.Menu
                                        },
                                        isLastQuestion = isLast,
                                        stepTitle = progressTitle
                                    )
                                } else {
                                    Text(stringResource(R.string.questions_protocol_error_no_questions))
                                }

                            }

                            Screen.Summary -> {
                                SummaryScreen(
                                    questions = visibleQuestions,
                                    answers = answers,
                                    onBack = { currentScreen = Screen.SyncEvaluation },
                                    onSave = {
                                        // A) Lokal speichern
                                        val relevantAnswers = answers.filterKeys { id ->
                                            visibleQuestions.any { it.id == id }
                                        }
                                        val answerList =
                                            relevantAnswers.map { (id, value) -> Answer(id, value) }
                                        DataLoader.saveAnswers(context, answerList)

                                        // B) An Backend senden (Backend Logik)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                // 1) Vor dem Senden Token prüfen (verhindert unnötige 401/403)
                                                val jwt = SessionStorage.loadJwt(context)
                                                val expired =
                                                    jwt.isNullOrBlank() || JwtUtils.isExpired(jwt!!)

                                                if (expired) {
                                                    withContext(Dispatchers.Main) {
                                                        pendingSubmit = true
                                                        currentScreen = Screen.Login
                                                    }
                                                    return@launch
                                                }

                                                // 2) Request DTO bauen
                                                val req = ProtocolSubmissionRequest(
                                                    templateKey = "schlaftagebuch_v1",
                                                    locale = currentLanguage.lowercase(),
                                                    filledAt = Instant.now().toString(),
                                                    answers = relevantAnswers.map { (id, value) ->
                                                        ProtocolAnswerDto(
                                                            id,
                                                            value
                                                        )
                                                    }
                                                )

                                                // 3) API Call - ApiClient Import muss oben vorhanden sein!
                                                val resp = ApiClient.protocolApi.submit(req)

                                                // 4) UI Update im Main Thread
                                                withContext(Dispatchers.Main) {
                                                    pendingSubmit = false
                                                    Toast.makeText(
                                                        context,
                                                        "Gespeichert! ID=${resp.submissionId}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    currentScreen = Screen.Menu
                                                }
                                            } catch (e: HttpException) {
                                                withContext(Dispatchers.Main) {
                                                    // Wenn Auth fehlt/abgelaufen: Login erzwingen (Status 401/403)
                                                    if (e.code() == 401 || e.code() == 403) {
                                                        pendingSubmit = true
                                                        currentScreen = Screen.Login
                                                    } else {
                                                        // Sonst: lokal ok, serverseitiger Fehler
                                                        Toast.makeText(
                                                            context,
                                                            "Lokal gespeichert. Server-Fehler: ${e.message()}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        currentScreen = Screen.Menu
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Netzwerk/sonstiger Fehler: lokal ok, server fail
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Lokal gespeichert. Server-Fehler: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    currentScreen = Screen.Menu
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}