package com.example.schlaftagebuch_vers_1.uiScreens


import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt
import com.example.schlaftagebuch_vers_1.R
import com.example.schlaftagebuch_vers_1.helper.TranslationHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut

/**
 * Diese Datei regelt die Darstellung der Schlafdaten der Uhr
 */

// Farben
val SleepGreen = Color(0xFF4CAF50)
val WakeRed = Color(0xFFFA1428)
val UnrestOrange = Color(0xFFFA7800)
val UnrestYellow = Color(0xFFFFFF14)
val PulseBlue = Color(0xFF2196F3)
val AxisGray = Color.Gray

//Datentypen
data class ProcessedEpoch(
    val timestamp: Long,
    val hr: Float,
    val enmo: Float,
    val sleepScore: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    dataFile: File,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    // Zurück Geste (Dreieck bzw Pfeil am unteren Bildschirmrand oder nach innen wischen)
    BackHandler(enabled = true) {
        onBack()
    }

    var epochs by remember { mutableStateOf<List<ProcessedEpoch>>(emptyList()) }
    var isComputing by remember { mutableStateOf(true) }
    var analysisText by remember { mutableStateOf("") }
    var isZoomed by remember { mutableStateOf(false) }
    val sharedScrollState = rememberScrollState()

    LaunchedEffect(dataFile) {
        withContext(Dispatchers.IO) {
            val (data, text) = analyzeSleepData(dataFile)
            epochs = data
            analysisText = text
            isComputing = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.analysis_result_title_main),
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
                    // Zoom als Icon oben rechts (statt Switch)
                    IconButton(onClick = { isZoomed = !isZoomed }) {
                        Icon(
                            imageVector = if (isZoomed) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = stringResource(R.string.analysis_result_switch_zoom),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },

        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.analysis_result_btn_next))
                    }
                }
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isComputing) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                Text(
                    stringResource(R.string.analysis_result_status_computing),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            } else if (epochs.isEmpty()) {
                Text(
                    stringResource(R.string.analysis_result_status_no_data),
                    color = MaterialTheme.colorScheme.error
                )
            } else {

                val startTime = epochs.first().timestamp
                val endTime = epochs.last().timestamp

                // Zoom Logik
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                val contentWidth = if (isZoomed) screenWidth * 10 else screenWidth - 64.dp // -64dp wegen Padding in Cards

                Card( // Erste Karten mit HR
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Titel in der Karte
                        Text(
                            stringResource(R.string.analysis_result_legend_sleep_pulse),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(16.dp))

                        // Graph (Scrollbar, nutzt sharedState)
                        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(sharedScrollState)) {
                            SleepGraphWithAxes(
                                epochs = epochs,
                                startTime = startTime,
                                endTime = endTime,
                                height = 200.dp,
                                width = contentWidth
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Legende Karte mit HR
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LegendBox(SleepGreen.copy(alpha = 0.3f), stringResource(R.string.analysis_result_legend_item_sleep))
                            Spacer(modifier = Modifier.width(12.dp))
                            LegendBox(WakeRed.copy(alpha = 0.3f), stringResource(R.string.analysis_result_legend_item_wake))
                            Spacer(modifier = Modifier.width(12.dp))
                            LegendLine(PulseBlue, stringResource(R.string.analysis_result_legend_item_pulse))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card( //Zweite Karte mit ACC
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.analysis_result_legend_motion),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(sharedScrollState)) {
                            MotionGraphWithAxes(
                                epochs = epochs,
                                startTime = startTime,
                                endTime = endTime,
                                height = 160.dp,
                                width = contentWidth
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LegendBox(WakeRed, stringResource(R.string.analysis_result_legend_item_wake))
                            Spacer(modifier = Modifier.width(12.dp))
                            LegendBox(UnrestOrange, stringResource(R.string.analysis_result_legend_item_very_restless))
                            Spacer(modifier = Modifier.width(12.dp))
                            LegendBox(UnrestYellow, stringResource(R.string.analysis_result_legend_item_restless))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card( //Dritte Karte mit Zusammenfassung
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.analysis_result_summary),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            analysisText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Platzhalter unten, damit man bis zum Ende scrollen kann (wegen BottomBar)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

//Legenden
@Composable
fun LegendBox(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun LegendLine(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(16.dp, 2.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

//Layout
const val Y_AXIS_WIDTH_DP = 40f // Etwas breiter für Labels
const val X_AXIS_HEIGHT_DP = 25f

//Skalierung
@Composable
fun SleepGraphWithAxes(
    epochs: List<ProcessedEpoch>,
    startTime: Long,
    endTime: Long,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp
) {
    val textPaint = remember {
        Paint().apply {
            color = AxisGray.toArgb(); textSize = 28f; typeface = Typeface.DEFAULT
        }
    }

    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        val graphWidth = size.width - Y_AXIS_WIDTH_DP.dp.toPx()
        val graphHeight = size.height - X_AXIS_HEIGHT_DP.dp.toPx()
        val graphOffsetX = Y_AXIS_WIDTH_DP.dp.toPx()

        val minHr = 40f
        val maxHr = 160f
        val rangeHr = maxHr - minHr
        val yTicks = listOf(60, 100, 140) // Hilfslinien

        yTicks.forEach { tickVal ->
            val normalizedY = (tickVal - minHr) / rangeHr
            val yPos = graphHeight - (normalizedY * graphHeight)
            drawLine(
                AxisGray.copy(alpha = 0.3f),
                start = Offset(graphOffsetX, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                tickVal.toString(),
                graphOffsetX - 8f,
                yPos + 10f,
                textPaint.apply { textAlign = Paint.Align.RIGHT })
        }
        drawLine(
            AxisGray,
            start = Offset(graphOffsetX, 0f),
            end = Offset(graphOffsetX, graphHeight),
            strokeWidth = 2f
        )

        //Zeichnen
        translate(left = graphOffsetX) {
            val widthPerEpoch = graphWidth / epochs.size

            //Hintergrund Schlafphasen (mit Farben)
            epochs.forEachIndexed { index, epoch ->
                val x = index * widthPerEpoch
                val isWake = epoch.sleepScore >= 0.25f
                val color =
                    if (isWake) WakeRed.copy(alpha = 0.3f) else SleepGreen.copy(alpha = 0.3f)
                drawRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = Size(widthPerEpoch + 0.5f, graphHeight)
                )
            }

            //Puls
            val path = Path()
            var firstPoint = true
            epochs.forEachIndexed { index, epoch ->
                if (epoch.hr > 0) {
                    val x = index * widthPerEpoch + (widthPerEpoch / 2)
                    val normalizedHr = ((epoch.hr - minHr) / rangeHr).coerceIn(0f, 1f)
                    val y = graphHeight - (normalizedHr * graphHeight)
                    if (firstPoint) {
                        path.moveTo(x, y); firstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }
                }
            }
            drawPath(path = path, color = PulseBlue, style = Stroke(width = 3f))
        }

        //X Achse
        drawXAxisLabels(this, startTime, endTime, graphOffsetX, graphWidth, graphHeight, textPaint)
    }
}

@Composable
fun MotionGraphWithAxes(
    epochs: List<ProcessedEpoch>,
    startTime: Long,
    endTime: Long,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp
) {
    val textPaint = remember {
        Paint().apply {
            color = AxisGray.toArgb(); textSize = 28f; typeface = Typeface.DEFAULT
        }
    }

    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        val graphWidth = size.width - Y_AXIS_WIDTH_DP.dp.toPx()
        val graphHeight = size.height - X_AXIS_HEIGHT_DP.dp.toPx()
        val graphOffsetX = Y_AXIS_WIDTH_DP.dp.toPx()

        //Y Achse
        val maxDisplayEnmo = 0.5f

        val yTicks = listOf(0.1f, 0.25f, 0.5f)
        yTicks.forEach { tickVal ->
            val normalizedY = tickVal / maxDisplayEnmo
            val yPos = graphHeight - (normalizedY * graphHeight)
            if (tickVal < maxDisplayEnmo) {
                drawLine(
                    AxisGray.copy(alpha = 0.5f),
                    start = Offset(graphOffsetX, yPos),
                    end = Offset(size.width, yPos),
                    strokeWidth = 1f
                )
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${tickVal}g",
                graphOffsetX - 8f,
                yPos + 10f,
                textPaint.apply { textAlign = Paint.Align.RIGHT })
        }
        drawLine(
            AxisGray,
            start = Offset(graphOffsetX, 0f),
            end = Offset(graphOffsetX, graphHeight),
            strokeWidth = 2f
        )

        //Zeichnen
        translate(left = graphOffsetX) {
            val widthPerEpoch = graphWidth / epochs.size
            epochs.forEachIndexed { index, epoch ->
                val x = index * widthPerEpoch
                // Höhe basierend auf festem Maximum 0.5g. Alles drüber wird abgeschnitten (coerceIn).
                val barHeight = (epoch.enmo / maxDisplayEnmo).coerceIn(0f, 1f) * graphHeight

                //Farben nach Kriterien
                val color = when {
                    epoch.enmo >= 0.22 -> WakeRed
                    epoch.enmo >= 0.16 -> UnrestOrange
                    epoch.enmo >= 0.12 -> UnrestYellow
                    else -> SleepGreen.copy(alpha = 0.5f)
                }

                if (epoch.enmo > 0.005f) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x, graphHeight - barHeight),
                        size = Size(widthPerEpoch + 0.5f, barHeight)
                    )
                }
            }
        }


        drawXAxisLabels(this, startTime, endTime, graphOffsetX, graphWidth, graphHeight, textPaint)
    }
}

//Zeitachse Daten
fun drawXAxisLabels(
    scope: DrawScope,
    startTime: Long,
    endTime: Long,
    offsetX: Float,
    graphWidth: Float,
    graphHeight: Float,
    paint: Paint
) {
    val durationMs = endTime - startTime
    val oneHourMs = 3600 * 1000L
    val timeFormatShort = SimpleDateFormat("HH:mm", Locale.GERMANY)

    paint.textAlign = Paint.Align.CENTER

    if (durationMs < oneHourMs) {
        //Bei weniger als einer Stunde Anfang und Ende
        scope.drawContext.canvas.nativeCanvas.drawText(
            timeFormatShort.format(Date(startTime)),
            offsetX,
            graphHeight + 40f,
            paint.apply { textAlign = Paint.Align.LEFT })
        scope.drawContext.canvas.nativeCanvas.drawText(
            timeFormatShort.format(Date(endTime)),
            offsetX + graphWidth,
            graphHeight + 40f,
            paint.apply { textAlign = Paint.Align.RIGHT })
    } else {
        //Bei über einer Stunde Einteilung nach vollen Stunden
        val calendar = Calendar.getInstance(Locale.GERMANY).apply { timeInMillis = startTime }
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        var nextHourMillis = calendar.timeInMillis

        while (nextHourMillis < endTime) {
            val relativePos = (nextHourMillis - startTime).toFloat() / durationMs
            val xPos = offsetX + (relativePos * graphWidth)

            scope.drawContext.canvas.nativeCanvas.drawText(
                timeFormatShort.format(
                    Date(
                        nextHourMillis
                    )
                ), xPos, graphHeight + 50f, paint.apply { textAlign = Paint.Align.CENTER })

            nextHourMillis += oneHourMs
        }
    }
}

//Algorithmus abgeändert von Cole Kripke
data class RawSample(val ts: Long, val type: String, val v1: Float, val v2: Float, val v3: Float)

fun analyzeSleepData(file: File): Pair<List<ProcessedEpoch>, String> {
    val rawLines = file.readLines().drop(1)
    val rawSamples = mutableListOf<RawSample>()

    for (line in rawLines) {
        val parts = line.split(",")
        if (parts.size >= 5) {
            try {
                rawSamples.add(
                    RawSample(
                        parts[0].toLong(), parts[1],
                        parts[2].toFloat(), parts[3].toFloat(), parts[4].toFloat()
                    )
                )
            } catch (e: Exception) {
                continue
            }
        }
    }

    if (rawSamples.isEmpty()) return Pair(
        emptyList(),
        TranslationHelper(R.string.analysis_result_error_invalid_data)
    )

    val groupedBySecond = rawSamples.groupBy { it.ts / 1000 }
    val sortedSeconds = groupedBySecond.keys.sorted()
    val epochs1Hz = mutableListOf<ProcessedEpoch>()

    for (sec in sortedSeconds) {
        val samples = groupedBySecond[sec] ?: continue
        val hrSamples = samples.filter { it.type == "HR" && it.v1 > 0 }
        val hr = if (hrSamples.isNotEmpty()) hrSamples.map { it.v1 }.average().toFloat() else 0f
        val accSamples = samples.filter { it.type == "ACC" }
        var avgEnmoG = 0f
        if (accSamples.isNotEmpty()) {
            val sumEnmoMss = accSamples.map {
                val magnitudeMss = sqrt(it.v1 * it.v1 + it.v2 * it.v2 + it.v3 * it.v3)
                max(0f, magnitudeMss - 9.81f) // Schwerkraft abziehen
            }.sum()
            val avgEnmoMss = sumEnmoMss / accSamples.size
            avgEnmoG =
                (avgEnmoMss / 9.81f).coerceAtMost(0.7f) // In g umrechnen, auf 0.7 begrenzen um Zacken zu vermeiden
        }
        epochs1Hz.add(ProcessedEpoch(sec * 1000, hr, avgEnmoG, 0f))
    }

    val smoothedEpochs = mutableListOf<ProcessedEpoch>()
    for (i in epochs1Hz.indices) {
        val tMinus2 = if (i >= 2) epochs1Hz[i - 2].enmo else 0f
        val tMinus1 = if (i >= 1) epochs1Hz[i - 1].enmo else 0f
        val tZero = epochs1Hz[i].enmo
        val tPlus1 = if (i < epochs1Hz.size - 1) epochs1Hz[i + 1].enmo else 0f
        val score = (0.1f * tMinus2) + (0.2f * tMinus1) + (0.5f * tZero) + (0.2f * tPlus1)
        smoothedEpochs.add(epochs1Hz[i].copy(sleepScore = score))
    }

    var countUnruhig = 0
    var countSehrUnruhig = 0
    var countWach = 0
    smoothedEpochs.forEach {
        if (it.sleepScore >= 0.25f) countWach++
        else if (it.sleepScore >= 0.17f) countSehrUnruhig++
        else if (it.sleepScore >= 0.10f) countUnruhig++
    }

    val totalSeconds = smoothedEpochs.size
    val durationStr =
        "${totalSeconds / 3600} h ${totalSeconds % 3600 / 60} min ${totalSeconds % 60} s"

    val sb = StringBuilder()
    sb.append(TranslationHelper(R.string.analysis_result_stats_total_duration) + " $durationStr\n\n")
    sb.append(TranslationHelper(R.string.analysis_result_stats_phases_title) + " \n")
    sb.append(TranslationHelper(R.string.analysis_result_stats_phase_unrest) + " ${countUnruhig}s\n")
    sb.append(TranslationHelper(R.string.analysis_result_stats_phase_very_unrest) + " ${countSehrUnruhig}s\n")
    sb.append(TranslationHelper(R.string.analysis_result_stats_phase_wake) + " ${countWach}s\n")

    val avgPulse = smoothedEpochs.filter { it.hr > 0 }.map { it.hr }.average()
    if (!avgPulse.isNaN() && avgPulse > 0) {
        sb.append("\n" + TranslationHelper(R.string.analysis_result_stats_avg_pulse) + " ${avgPulse.toInt()} bpm")
    }

    return Pair(smoothedEpochs, sb.toString())
}