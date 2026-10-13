package com.example.schlaftagebuch_vers_1.helper

//zwingt die App eine eigene Sprachkonfigiuration zu laden um lokal die Sprache zu ändern

import android.content.Context
import java.util.Locale

@Suppress("DEPRECATION")
fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val resources = context.resources
    val configuration = resources.configuration
    configuration.setLocale(locale)
    resources.updateConfiguration(configuration, resources.displayMetrics)
}