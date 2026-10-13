package com.example.schlaftagebuch_vers_1.uiScreens

/**
 * UI: First Login
 *  Enthält Compose-Screens für:
 *  1) Datenschutzhinweis + Zustimmung
 *  2) Erster Login (Einmalcode + Daten) -> Backend
 *  3) Anzeige des generierten Benutzernamens
 *  + Hilfsfunktionen (Validierung / DatePicker)
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper
import com.example.schlaftagebuch_vers_1.uiTools.LogInViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Datenschutzhinweise.
 *
 * - Nutzer muss aktiv zustimmen (Checkbox), sonst ist "Weiter" deaktiviert
 * - MainActivity entscheidet, ob dieser Screen zuerst gezeigt wird
 * - onAccept(accepted) steuert den nächsten Schritt im Flow
 */

@Composable
fun PrivacyPolicyScreen(
    onAccept: (Boolean) -> Unit
) {
    var accepted by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Oberer Bereich und karte
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.privacy_policy_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Eigentliche Datenschutz
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Zentriert den Link
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy_content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Trennlinie
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link zur Datenschutzrichtlinie
                    TextButton(
                        onClick = { uriHandler.openUri("https://mul-ct.de/datenschutz") }
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_policy_link),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Unterer Bereich
        Column {
            // Checkbox-Zeile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { accepted = !accepted } // Ganze Zeile schaltet um
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = null, // wird von Row gesteuert
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.privacy_policy_accept_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Akzeptieren Button
            Button(
                onClick = { onAccept(accepted) },
                enabled = accepted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy_accept_btn),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            // Kleiner Abstand unten
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AuthChoiceScreen(
    onLogin: () -> Unit,
    onActivate: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.auth_choice_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    stringResource(R.string.auth_choice_btn_login),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onActivate,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    stringResource(R.string.auth_choice_btn_activate),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Erster Login / Kontoerstellung.
 *
 * - Validierung (Passwort/Datum)
 * - Aufruf von vm.firstLogin(...) -> Backend
 *
 * - LogInViewModel führt den Netzwerkrequest aus und liefert JWT
 */

@Composable
fun FirstLoginScreen(
    consentAccepted: Boolean,
    onSuccess: (jwt: String, username: String?) -> Unit,
    vm: LogInViewModel = viewModel()
) {

    var familyName by remember { mutableStateOf("") }
    var givenName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // Eingabeformat TT.MM.JJJJ
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Zustand aus ViewModel (loading/error)
    val state = vm.uiState

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.registration_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            // Zugangscode
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.trim() },
                label = { Text(stringResource(R.string.registration_box_code)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Nachname
            OutlinedTextField(
                value = familyName,
                onValueChange = { familyName = it.trim() },
                label = { Text(stringResource(R.string.registration_box_name_last)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            //Vorname
            OutlinedTextField(
                value = givenName,
                onValueChange = { givenName = it.trim() },
                label = { Text(stringResource(R.string.registration_box_name_first)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Geburtsdatum (hier: Freitext, Validierung unten)
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text(stringResource(R.string.registration_box_birthday)) },
                placeholder = { Text("TT.MM.JJJJ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // Passwort (verdeckt angezeigt)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.trim() },
                label = { Text(stringResource(R.string.registration_box_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, // Verhindert Zeilenumbrüche

                // Logik für Sichtbarkeit
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                // Augen Icon
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription =
                                if (passwordVisible)
                                    stringResource(R.string.login_icon_password_visible)
                                else
                                    stringResource(R.string.login_icon_password_hidden)
                        )
                    }
                },
                // Tastatur auf Passwort-Modus stellen, verhinder Autokorektur und ähnliches
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(8.dp))

            localError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(6.dp))
            }

            // Backend-/Netzwerkfehler aus ViewModel
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(14.dp))

            Button(
                enabled = !state.loading,
                onClick = {
                    val pwErr = validatePassword(password)
                    if (pwErr != null) { localError = pwErr; return@Button }

                    val bdErr = validateBirthDate(birthDate)
                    if (bdErr != null) { localError = bdErr; return@Button }

                    localError = null

                    vm.firstLogin(
                        consentAccepted = consentAccepted,
                        code = code,
                        givenName = givenName,
                        familyName = familyName,
                        birthDate = birthDate,
                        password = password,
                        onSuccess = onSuccess
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (state.loading) "..." else stringResource(R.string.registration_btn_finish),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

/**
 * Login (beim Abmelden (Token ist nicht mehr gültig))
 */

@Composable
fun LoginScreen(
    initialUsername: String?,
    onSuccess: (jwt: String, username: String?) -> Unit,
    onActivateInstead: (() -> Unit)? = null,
    vm: LogInViewModel = viewModel()
) {

    var username by remember { mutableStateOf(initialUsername ?: "") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val state = vm.uiState

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.login_box_username)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_box_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            localError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(6.dp))
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(14.dp))

            Button(
                enabled = !state.loading,
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        localError = TranslationHelper(R.string.login_error)
                        return@Button
                    }
                    localError = null

                    vm.login(username, password) { jwt, extractedUsername ->
                        onSuccess(jwt, extractedUsername)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (state.loading) "..." else stringResource(R.string.login_btn_login))
            }

            if (onActivateInstead != null) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onActivateInstead) {
                    Text(
                        stringResource(R.string.login_btn_activate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/**
 * Anzeige der automatisch generierten Zugangsdaten (Username).
 */

@Composable
fun CredentialsInfoScreen(
    username: String,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.credential_info_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.credential_info_code),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Username + Button "Kopieren"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(username, style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(username))
                        TranslationHelper(R.string.credential_info_copy_2)
                    },
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        stringResource(R.string.credential_info_copy_1),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Weiter-Button -> Flow geht ins Menü
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    stringResource(R.string.credential_info_btn_next),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


//Hilfsfunktionen

/**
 * Optionales UI-Element: DatePicker-Feld für Geburtsdatum.
 * Aktuell wird in FirstLoginScreen noch Freitext genutzt.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDatePickerField(
    birthDate: String,
    onBirthDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    OutlinedTextField(
        value = birthDate,
        onValueChange = {},
        readOnly = true,
        label = { Text("Geburtsdatum") },
        placeholder = { Text("YYYY-MM-DD") },
        trailingIcon = { Text("📅") },
        modifier = modifier.clickable { open = true }
    )

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onBirthDateSelected(localDate.toString()) // YYYY-MM-DD
                        }
                        open = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/**
 * Validiert ein Geburtsdatum im Format TT.MM.JJJJ.
 */

private fun validateBirthDate(input: String): String? {
    val t = input.trim()
    if (t.isBlank()) return TranslationHelper(R.string.registration_error_birthday_1)

    val regex = Regex("""\d{2}\.\d{2}\.\d{4}""")
    if (!regex.matches(t)) {
        return TranslationHelper(R.string.registration_error_birthday_2)
    }

    return try {
        val parts = t.split(".")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        // Prüft, ob das Datum real existiert
        LocalDate.of(year, month, day)
        null
    } catch (e: Exception) {
        TranslationHelper(R.string.registration_error_birthday_3)
    }
}

/**
 * Passwort-Regeln:
 * - mindestens 8 Zeichen
 * - mindestens 1 Buchstabe
 * - mindestens 1 Zahl
 */

private fun validatePassword(pw: String): String? {
    if (pw.length < 8) return TranslationHelper(R.string.registration_error_password_1)
    if (!pw.any { it.isLetter() }) return TranslationHelper(R.string.registration_error_password_2)
    if (!pw.any { it.isDigit() }) return TranslationHelper(R.string.registration_error_password_3)
    return null
}