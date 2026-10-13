package com.example.schlaftagebuch_vers_1.uiScreens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.api.ApiClient
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.SessionStorage
import com.example.schlaftagebuch_vers_1.api.report.PdfUtils
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ist das Hauptmenü
 * Von hieraus gelangt man:
 * Einstellungen
 * Uhr Synchronisation
 * Schlafsync starten
 * PDF Download
 */

// Hilfsfunktion PDF
private fun filenameFromContentDisposition(cd: String?): String? {
    if (cd.isNullOrBlank()) return null
    val m = Regex("filename=\"([^\"]+)\"").find(cd)
    return m?.groupValues?.getOrNull(1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onStartQuestions: () -> Unit,
    onStartSleep: () -> Unit,
    onStartSync: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // WICHTIG: JWT nach Navigation / Recomposition wieder laden
    LaunchedEffect(Unit) {
        if (Session.jwt.isNullOrBlank()) {
            Session.jwt = SessionStorage.loadJwt(context)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { // nur Titel
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
        ) {

            // Interaktion

            // GRID (Fragebogen & Schlaf)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fragebogen
                MenuActionCard(
                    title = stringResource(R.string.menu_btn_questions_protocol),
                    icon = Icons.Default.EditCalendar,
                    modifier = Modifier.weight(1f),
                    onClick = onStartQuestions
                )

                // Schlafen
                MenuActionCard(
                    title = stringResource(R.string.menu_btn_sleep_control),
                    icon = Icons.Default.Bedtime,
                    modifier = Modifier.weight(1f),
                    onClick = onStartSleep
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SYNC
            WideActionCard(
                title = stringResource(R.string.menu_btn_smartwatch),
                subtitle = stringResource(R.string.menu_btn_smartwatch_desc),
                icon = Icons.Default.Watch,
                onClick = onStartSync
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PDF
            WideActionCard(
                title = stringResource(R.string.menu_btn_PDF),
                subtitle = stringResource(R.string.menu_btn_PDF_desc),
                icon = Icons.Default.Description,
                onClick = {
                    scope.launch {

                        // JWT sicherstellen
                        if (Session.jwt.isNullOrBlank()) {
                            Session.jwt = SessionStorage.loadJwt(context)
                        }

                        try {
                            val resp = withContext(Dispatchers.IO) {
                                ApiClient.reportApi.myReportPdf(days = 14, allSubmissions = false)
                            }

                            if (!resp.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    TranslationHelper(R.string.menu_toast_pdf_fail_1) + "HTTP ${resp.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            val body = resp.body()
                            if (body == null) {
                                TranslationHelper.toast(R.string.menu_toast_pdf_fail_2)
                                return@launch
                            }

                            // Optional: Content-Type prüfen (nicht hart abbrechen)
                            val ct = resp.headers()["Content-Type"] ?: ""
                            if (!ct.contains("pdf", ignoreCase = true)) {
                                // trotzdem speichern/öffnen, nur Hinweis
                                Toast.makeText(
                                    context,
                                    "Hinweis: Content-Type=$ct",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            val bytes = withContext(Dispatchers.IO) { body.bytes() }

                            // Dateiname aus Header (wenn vorhanden), sonst Fallback
                            val headerName = filenameFromContentDisposition(resp.headers()["Content-Disposition"])
                            val fileName = headerName ?: "sleep-report.pdf"

                            val file = PdfUtils.savePdfToAppDownloads(
                                context = context,
                                bytes = bytes,
                                filename = fileName
                            )

                            PdfUtils.openPdf(context, file)

                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                TranslationHelper(R.string.menu_toast_pdf_fail_3) + "" + e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Einstellungen
            WideActionCard(
                title = stringResource(R.string.menu_btn_settings),
                subtitle = stringResource(R.string.menu_btn_settings_desc),
                icon = Icons.Default.Settings,
                onClick = onOpenSettings
            )
        }
    }
}

// --- Karten Baukasten ---

@Composable
fun MenuActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.1f),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun WideActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}