package com.example.schlaftagebuch_vers_1.dataService

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.time.LocalTime
import androidx.core.content.edit

object DataLoader {
    private const val PREFS_NAME = "AppSettings"
    private const val KEY_LANGUAGE = "LanguageCode"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_ALARM_HOUR = "alarm_hour"
    private const val KEY_ALARM_MINUTE = "alarm_minute"
    private const val KEY_THEME = "theme"

    // Neu hinzugefügt für Login
    private const val KEY_STORED_USERNAME = "stored_username"

    // gson für die Konvertierung
    private val gson = GsonBuilder()
        .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
        .create()

    // --- Sprache ändern/laden ---
    fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANGUAGE, langCode) }
    }
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "DE") ?: "DE"
    }

    // --- Benachrichtigung ändern/laden ---
    fun saveAlarmState(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_ALARM_ENABLED, isEnabled) }
    }
    fun loadAlarmState(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALARM_ENABLED, false)
    }

    // --- Benachrichtigungszeit ändern/laden ---
    fun saveAlarmTime(context: Context, time: LocalTime) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_ALARM_HOUR, time.hour)
            putInt(KEY_ALARM_MINUTE, time.minute)
            apply()
        }
    }
    fun loadAlarmTime(context: Context): LocalTime {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_ALARM_HOUR, 20) // Standard: 20 Uhr
        val minute = prefs.getInt(KEY_ALARM_MINUTE, 0)
        return LocalTime.of(hour, minute)
    }

    // --- Anmeldung / First Time ---
    fun isFirstTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("isFirstTime", true)
    }
    fun setFirstTimeCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean("isFirstTime", false) }
    }

    // --- Username Speicherung (Neu) ---
    /**
     * Speichert den zuletzt verwendeten Benutzernamen,
     * um ihn beim nächsten Login vorzubelegen.
     */
    fun saveStoredUsername(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_STORED_USERNAME, username) }
    }

    /** Gibt den gespeicherten Benutzernamen zurück (oder null) */
    fun getStoredUsername(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_STORED_USERNAME, null)
    }

    // --- Farbschema ändern/laden ---
    fun saveColorTheme(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, langCode).apply() }
    }
    fun getColorTheme(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "system")
    }

    // --- Fragen laden (Debug mit Error Log) ---
    fun getQuestions(context: Context, langCode: String): List<Question> {
        val fileName = "Questions_$langCode.json"

        val jsonContent = loadJsonString(context, fileName)
            ?: return emptyList()

        val type = object : TypeToken<List<Question>>() {}.type

        return try {
            gson.fromJson(jsonContent, type)
        } catch (e: Exception) {
            // WICHTIG: Fehler ausgeben!
            e.printStackTrace()
            println("JSON ERROR: ${e.message}")
            emptyList()
        }
    }

    // --- Lesen der Datei (Intern) ---
    private fun loadJsonString(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // --- Antworten speichern ---
    fun saveAnswers(context: Context, answers: List<Answer>) {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gsonPretty.toJson(answers)

        try {
            context.openFileOutput("answers.json", Context.MODE_PRIVATE).use { stream ->
                stream.write(jsonString.toByteArray())
            }
            //Konsolenausgabe Debug
            println("Datei gespeichert.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    // Optional: Methoden zum Laden/Löschen lokaler Antworten
    fun loadAnswers(context: Context): List<Answer> {
        return try {
            val json = context.openFileInput("answers.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Answer>>() {}.type
            GsonBuilder().create().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearAnswers(context: Context) {
        try {
            context.deleteFile("answers.json")
        } catch (_: Exception) { }
    }
    */
}