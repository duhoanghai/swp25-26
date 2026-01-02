package com.example.prototyp_1

//Beinhaltet alle Import und Export und Verwaltungsfunktionen

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.example.prototyp_1.fhir.FhirObservation

object  DataLoader {

    private val gson = GsonBuilder().registerTypeAdapter(Question::class.java, QuestionTypeAdapter()).create()

    fun loadJsonString(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getQuestions(context: Context): List<Question> {
        val jsonContent: String = loadJsonString(context, QUESTION_FILE_NAME)
            ?: return emptyList() //wenn Datei nicht existiert (wie Nothing)

        val type = object : TypeToken<List<Question>>() {}.type

        return gson.fromJson(jsonContent, type)
    }

    fun saveAnswers(context: Context, answers: List<Answer>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(answers)

        try {
            // MODE_PRIVATE = Nur diese App darf die Datei lesen
            context.openFileOutput("answers.json", Context.MODE_PRIVATE).use { stream ->
                stream.write(jsonString.toByteArray())
            }
            println("Datei gespeichert unter: ${context.filesDir}/answers.json")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  HIER KOMMT FHIR REIN
    fun saveFhirObservations(
        context: Context,
        observations: List<FhirObservation>
    ) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(observations)

        context.openFileOutput("fhir_observations.json", Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

}
