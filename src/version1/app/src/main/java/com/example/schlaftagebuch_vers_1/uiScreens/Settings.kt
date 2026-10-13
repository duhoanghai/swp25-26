package com.example.schlaftagebuch_vers_1.uiScreens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.dataService.DataLoader
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper

/**
 * Folgende Einstellungen und Funktionen sind enthalten:
 * Benachrichtigung
 * Farbschema
 * Benutzername anzeigen
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeString: String?,
    onThemeChanged: (String) -> Unit,
    currentLanguageCode: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var context = LocalContext.current
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var themeMenuExpanded by remember { mutableStateOf(false) }

    fun onSaveAndContinue() {
        DataLoader.setFirstTimeCompleted(context)
        TranslationHelper(R.string.settings_toast_saved)
    }

    // Zurück Geste (Dreieck bzw Pfeil am unteren Bildschirmrand oder nach innen wischen)
    BackHandler(enabled = true) {
        onSaveAndContinue()
        onBack()
    }

    // Map welche Farben (links System, rechts angezeigt)
    var currentTheme: String = when (currentThemeString) {
        "dark" -> stringResource(R.string.settings_color_dark)
        "bright" -> stringResource(R.string.settings_color_bright)
        else -> stringResource(R.string.settings_color_system)
    }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { // nur Titel
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = { // nur Zurück Knopf
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                ) {
                    Button(
                        onClick = {
                            onBack()
                            onSaveAndContinue()
                                  },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            stringResource(R.string.settings_btn_back),
                            style = MaterialTheme.typography.titleMedium
                        )

                    }
                }
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Card( // Erinnerung
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Notification()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card( // Farbauswahl
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_color_title), style = MaterialTheme.typography.titleMedium)
                    Box() {
                        Button(
                            onClick = { themeMenuExpanded = true },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(currentTheme)
                        }


                        DropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_color_system)) },
                                onClick = {
                                    onThemeChanged("system")
                                    themeMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_color_bright)) },
                                onClick = {
                                    onThemeChanged("bright")
                                    themeMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_color_dark)) },
                                onClick = {
                                    onThemeChanged("dark")
                                    themeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card( // Sprachauswahl
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_btn_language), style = MaterialTheme.typography.titleMedium)
                    Box() {
                        Button(
                            onClick = { languageMenuExpanded = true },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.language_long))
                        }


                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text( "Deutsch" ) },
                                onClick = {
                                    onLanguageChange("DE")
                                    languageMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text( "English" ) },
                                onClick = {
                                    onLanguageChange("EN")
                                    languageMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text( "Русский" ) },
                                onClick = {
                                    onLanguageChange("RU")
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card( // Benutzername anzeigen
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_username), style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "" + DataLoader.getStoredUsername(context),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}