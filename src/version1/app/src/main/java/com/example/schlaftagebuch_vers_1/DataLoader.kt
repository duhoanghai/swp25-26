package com.example.schlaftagebuch_vers_1

import android.R.attr.type
import android.content.Context
import com.example.schlaftagebuch_vers_1.Question
import com.example.schlaftagebuch_vers_1.Answer
import com.example.schlaftagebuch_vers_1.QuestionTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException

object DataLoader {
    private const val PREFS_NAME = "AppSettings"
    private const val KEY_LANGUAGE = "LanguageCode"

    //gson für die Konvertierung
    private val gson = GsonBuilder()
        .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
        .create()

    //neu zum Sprache ändern
    fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    //lädt die geänderte Einstellung
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "DE") ?: "DE"
    }

    //Standard
//    fun getQuestions(context: Context, langCode: String): List<Question> {
//        val fileName = "Questions_$langCode.json"
//
//        val jsonContent = loadJsonString(context, fileName)
//            ?: return emptyList()
//
//        val type = object : TypeToken<List<Question>>() {}.type
//
//        return try {
//            gson.fromJson(jsonContent, type)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }

    //Debug
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

    //Lesen der Datei
    private fun loadJsonString(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //ab hier speichern
    fun saveAnswers(context: Context, answers: List<Answer>) {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gsonPretty.toJson(answers)

        try {
            context.openFileOutput("answers.json", Context.MODE_PRIVATE).use { stream ->
                stream.write(jsonString.toByteArray())
            }
            // Optional: Konsolenausgabe zur Kontrolle
            println("Datei gespeichert.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}