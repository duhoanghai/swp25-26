package com.example.schlaftagebuch_vers_1.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Erklärungen siehe Hell
    primary = MintSoft,
    onPrimary = BlueDark,

    background = BlackRich,
    onBackground = OffWhite,

    surface = SlateDark,
    onSurface = GreyLight,

    secondary = CardDark,
    onSecondary = OffWhite,

    tertiary = Periwinkle,
    onTertiary = BlueDark,

    outline = GreyMedium,

    //Nur im Dunklen, kräftigeres Error Rot, Standard sehr Pastell
    error = RedErrorDark,
    onError = OffWhite
)

private val LightColorScheme = lightColorScheme(
    // Buttons & Interaktion
    primary = MintStrong,                 // Alles interaktive
    onPrimary = BlueDark,           // Schrift und Elemente auf Interaktiven

    // Hintergrund
    background = OffWhite,          // Haupt Hintergrund
    onBackground = BlueDark,        // Haupt Schriftfarbe

    // Oberflächen
    surface = SlateLight,           // Das neue, sehr helle Grau für Cards
    onSurface = BlueDark,           // Standardschrift

    // Sekundäre Elemente
    secondary = CardLight,          // Zweitfarbe
    onSecondary = BlueDark,         // Hellere Schrit auf dunkel

    // Andere Aktive Elemente wie Switch oder Checkbox
    tertiary = IndigoDeep,
    onTertiary = OffWhite,

    // Linien
    outline = GreyLight
)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

@Composable
fun SchlaftagebuchColorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), //Errechnet in Main

    //Dynamic Color deaktivieren (macht nur Stress)
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // --- Statusleiste anpassen ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Färbt die Leiste oben passend zum Hintergrund
            window.statusBarColor = colorScheme.background.toArgb()

            // Steuert, ob die Icons (Uhrzeit, Batterie) weiß oder schwarz sind
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Verweist auf Type.kt
        content = content
    )
}