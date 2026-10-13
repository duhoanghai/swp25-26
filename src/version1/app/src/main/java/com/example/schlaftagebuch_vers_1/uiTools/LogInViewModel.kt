package com.example.schlaftagebuch_vers_1.uiTools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.schlaftagebuch_vers_1.api.SessionStorage
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.example.schlaftagebuch_vers_1.api.ApiClient
import com.example.schlaftagebuch_vers_1.api.JwtUtils
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.auth.PatientFirstLoginRequest
import com.example.schlaftagebuch_vers_1.api.auth.PatientLoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * UI-Zustand für den First-Login-Flow.
 *
 * loading:
 * - true => Request läuft, UI kann Button deaktivieren
 *
 * error:
 * - Fehlermeldung (z. B. HTTP-Fehler, Netzwerkfehler)
 */

data class LogInUiState(
    val loading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel für den First-Login und Login.
 *
 * - Validierungs-/UI-State verwalten (loading/error)
 * - Backend-Request über ApiClient ausführen
 * - Bei Erfolg: JWT speichern + Username aus JWT extrahieren
 *
 * - Wird in FirstLoginScreen via viewModel() verwendet
 * - Nutzt ApiClient.authApi (Retrofit)
 */

class LogInViewModel(app: Application) : AndroidViewModel(app) {

    // Compose-State: UI kann automatisch reagieren, wenn sich uiState ändert
    var uiState by mutableStateOf(LogInUiState())
        private set

    /**
     * Führt den First-Login gegen das Backend aus.
     */

    fun firstLogin(
        consentAccepted: Boolean,
        code: String,
        givenName: String,
        familyName: String,
        birthDate: String,
        password: String,
        onSuccess: (jwt: String, username: String?) -> Unit
    ) {
        // UI: Request startet
        uiState = LogInUiState(loading = true)

        // Coroutine im ViewModel-Lifecycle
        viewModelScope.launch {
            try {
                // Request-DTO erstellen (trim() entfernt führende/folgende Leerzeichen)
                val response = ApiClient.authApi.patientFirstLogin(
                    PatientFirstLoginRequest(
                        code = code.trim(),
                        consentAccepted = consentAccepted,
                        givenName = givenName.trim(),
                        familyName = familyName.trim(),
                        birthDate = birthDateToIso(birthDate),
                        password = password
                    )
                )

                // Backend liefert Token zurück
                val jwt = response.token
                // Token in Session speichern (wird für weitere API-Calls genutzt)
                Session.jwt = jwt
                // WICHTIG: Token persistent speichern (sonst PDF = 403)
                SessionStorage.saveJwt(getApplication(), jwt)
                // Optional: Username aus JWT extrahieren
                val username = JwtUtils.extractUsername(jwt)

                // UI: fertig, kein Fehler
                uiState = LogInUiState(loading = false)
                // Weitergabe an UI (jetzt: CredetialsInfoScreen)
                onSuccess(jwt, username)

            } catch (e: HttpException) {
                // HTTP-Fehler (z. B. 400/401/403/500)
                uiState = LogInUiState(
                    loading = false,
                    error = "HTTP ${e.code()}: ${e.message()}"
                )
            } catch (e: Exception) {
                // Sonstige Fehler (Netzwerk, Parsing, unerwartet)
                uiState = LogInUiState(
                    loading = false,
                    error = e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }

    /**
     * Führt den Login gegen das Backend aus.
     */

    fun login(
        username: String,
        password: String,
        onSuccess: (jwt: String, username: String?) -> Unit
    ) {

        uiState = LogInUiState(loading = true)

        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.patientLogin(
                    PatientLoginRequest(
                        username = username.trim(),
                        password = password
                    )
                )

                val jwt = response.token
                Session.jwt = jwt
                SessionStorage.saveJwt(getApplication(), jwt)

                // Username wird aus dem JWT gelesen, um nicht vom Response-Body abhängig zu sein.
                val extractedUsername = JwtUtils.extractUsername(jwt)

                uiState = LogInUiState(loading = false)
                onSuccess(jwt, extractedUsername)

            } catch (e: HttpException) {
                uiState = LogInUiState(
                    loading = false,
                    error = "HTTP ${e.code()}: ${e.message()}"
                )
            } catch (e: Exception) {
                uiState = LogInUiState(
                    loading = false,
                    error = e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }
}

//Hilfsfunktionen

/**
 * Konvertiert Geburtsdatum von TT.MM.JJJJ nach ISO YYYY-MM-DD.
 *
 * Wichtig:
 * - FirstLoginScreen validiert vorher das Format.
 */

private fun birthDateToIso(input: String): String {
    val parts = input.split(".")
    val day = parts[0]
    val month = parts[1]
    val year = parts[2]
    return "$year-$month-$day" // YYYY-MM-DD
}
