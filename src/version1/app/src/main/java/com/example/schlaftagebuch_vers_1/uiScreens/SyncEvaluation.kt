package com.example.schlaftagebuch_vers_1.uiScreens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.schlaftagebuch_vers_1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enthält die Funktionen für dem Import der Schlafdaten der Uhr
 * Integriert mit Preview, da Veranschaulichen ohne Dateien manchmal schwer
 * Neu an Logik: etwas aufgeteilter als vorher, Karten in extra Methode
 */

// jetzt nur noch Logik und Dateien
@Composable
fun SyncEvaluationScreen(
    onBack: () -> Unit,
    onBreak: () -> Unit,
    onAnalyze: (File) -> Unit
) {
    // Zurück Geste (Dreieck bzw Pfeil am unteren Bildschirmrand oder nach innen wischen)
    BackHandler(enabled = true) {
        onBack()
    }

    val context = LocalContext.current
    var fileList by remember { mutableStateOf(listOf<File>()) }

    fun refreshFiles() {
        val dir = context.filesDir
        val files = dir.listFiles { _, name -> name.endsWith(".csv") }
        fileList = files?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Alles Löschen
    fun deleteAll() {
        context.filesDir.listFiles()?.forEach { if (it.name.endsWith(".csv")) it.delete() }
        refreshFiles()
    }

    // Beim Start laden
    LaunchedEffect(Unit) {
        refreshFiles()
    }

    // Übergabe an die UI
    SyncEvaluationContent(
        fileList = fileList,
        onBack = onBack,
        onBreak = onBreak,
        onAnalyze = onAnalyze,
        onRefresh = { refreshFiles() },
        onDeleteAll = { deleteAll() }
    )
}

// Jetzt neu für UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncEvaluationContent(
    fileList: List<File>,
    onBack: () -> Unit,
    onBreak: () -> Unit,
    onAnalyze: (File) -> Unit,
    onRefresh: () -> Unit,
    onDeleteAll: () -> Unit,
    isPreview: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.sync_evaluation_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.questions_protocol_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.sync_evaluation_btn_refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },

        // Untere Leiste
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Abstand zwischen Buttons
                ) {
                    // Linker Button: Löschen (Rot) - Nur zeigen wenn Dateien da sind
                    if (fileList.isNotEmpty()) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f) // Nimmt 50% Platz ein
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.sync_evaluation_btn_delete),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Button(
                        onClick = onBreak,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f) // Nimmt den Rest oder 50% ein
                            .height(56.dp)
                    ) {
                        Text(
                            stringResource(R.string.sync_evaluation_btn_next),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // Inhalt der Liste
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (fileList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.sync_evaluation_box_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(fileList) { file ->
                        FileItem(
                            file = file,
                            onClick = { onAnalyze(file) },
                            // Für Preview simulieren wir Daten, da File-Objekte dort leer sind
                            forcedDate = if (isPreview) System.currentTimeMillis() else null,
                            forcedSize = if (isPreview) 12048L else null
                        )
                    }
                }
            }
        }

        // Sicherheitsabfrage Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.sync_evaluation_delete_warning)) },
                text = { Text(stringResource(R.string.sync_evaluation_delete_warning_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteAll()
                        showDeleteDialog = false
                    }) {
                        Text(stringResource(R.string.sync_evaluation_delete_confirm), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.sync_evaluation_delete_break))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

// Karten als Elemente
@Composable
fun FileItem(
    file: File,
    onClick: () -> Unit,
    forcedDate: Long? = null,
    forcedSize: Long? = null
) {
    // Falls Preview, dann andere Daten
    val dateToUse = forcedDate ?: file.lastModified()
    val sizeToUse = forcedSize ?: file.length()

    val dateFormat = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())
    val lastMod = dateFormat.format(Date(dateToUse))
    val sizeKb = sizeToUse / 1024

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),

    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.sync_evaluation_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${stringResource(R.string.sync_evaluation_card_collected)} $lastMod",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$sizeKb KB",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.sync_evaluation_card_action),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Sync Screen mit Daten")
@Composable
fun SyncEvaluationScreenPreview() {
    // Beispieldaten für die Preview
    val dummyFiles = listOf(
        File("Hier_könnte_ihre_Werbung_stehen.csv"),
        File("2w31t3_D4t31.csv")
    )

    SyncEvaluationContent(
        fileList = dummyFiles,
        onBack = {},
        onBreak = {},
        onAnalyze = {},
        onRefresh = {},
        onDeleteAll = {},
        isPreview = true
    )
}

@Preview(showBackground = true, name = "Sync Screen Leer")
@Composable
fun SyncEvaluationScreenEmptyPreview() {
    SyncEvaluationContent(
        fileList = emptyList(),
        onBack = {},
        onBreak = {},
        onAnalyze = {},
        onRefresh = {},
        onDeleteAll = {}
    )
}