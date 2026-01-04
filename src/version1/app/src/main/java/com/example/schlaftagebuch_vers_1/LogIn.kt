package com.example.schlaftagebuch_vers_1

import LogInViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PrivacyPolicyScreen(
    onAccept: (Boolean) -> Unit
) {
    var accepted by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(
                text = "Datenschutz",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text =
                    "Ihre personenbezogenen und medizinischen Daten werden\n" +
                            "im Rahmen der Behandlung durch die\n" +
                            "Medizinische Universität Lausitz – Carl Thiem verarbeitet.\n\n" +
                            "Weitere Informationen zu Art, Umfang und Zweck der\n" +
                            "Datenverarbeitung sowie zu Ihren Rechten finden Sie in der\n" +
                            "Datenschutzerklärung der MUL-CT.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Datenschutzerklärung öffnen",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://mul-ct.de/datenschutz")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it }
                )
                Text(
                    text = "Ich habe die Datenschutzhinweise gelesen\n " +
                            "und zur Kenntnis genommen."
                )
            }
        }

        Button(
            onClick = { onAccept(accepted) },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Weiter")
        }
    }
}

// Screen 2: Anmeldung (Minimum: 3 Felder + Weiter)
@Composable
fun FirstLoginScreen(
    consentAccepted: Boolean,
    onSuccess: (jwt: String, username: String?) -> Unit,
    vm: LogInViewModel = viewModel()
) {
    var familyName by remember { mutableStateOf("") }
    var givenName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // YYYY-MM-DD
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

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
            Text("Erster Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Einmalcode") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = familyName,
                onValueChange = { familyName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = givenName,
                onValueChange = { givenName = it },
                label = { Text("Vorname") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Geburtsdatum") },
                placeholder = { Text("TT.MM.JJJJ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.loading) "..." else "Konto erstellen")
            }
        }
    }
}

@Composable
fun CredentialsInfoScreen(
    username: String,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wichtig", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Ihr Benutzername wurde automatisch erstellt.\n" +
                        "Bitte notieren Sie ihn oder speichern Sie ihn zusammen\n" +
                        "mit Ihrem Passwort im Passwort-Manager.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(username, style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(username))
                        Toast.makeText(context, "Kopiert!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Kopieren")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Weiter")
            }
        }
    }
}


//Hilfsfunktionen

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
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
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


private fun validateBirthDate(input: String): String? {
    val t = input.trim()
    if (t.isBlank()) return "Geburtsdatum ist erforderlich."

    val regex = Regex("""\d{2}\.\d{2}\.\d{4}""")
    if (!regex.matches(t)) {
        return "Format: TT.MM.JJJJ (z.B. 21.10.1996)"
    }

    return try {
        val parts = t.split(".")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        java.time.LocalDate.of(year, month, day)
        null
    } catch (e: Exception) {
        "Ungültiges Geburtsdatum."
    }
}

private fun validatePassword(pw: String): String? {
    if (pw.length < 8) return "Passwort: mindestens 8 Zeichen."
    if (!pw.any { it.isLetter() }) return "Passwort: mindestens 1 Buchstabe."
    if (!pw.any { it.isDigit() }) return "Passwort: mindestens 1 Zahl."
    return null
}