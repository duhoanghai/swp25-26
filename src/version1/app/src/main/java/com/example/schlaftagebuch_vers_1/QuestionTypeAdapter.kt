package com.example.schlaftagebuch_vers_1

//Beinhaltet die Möglichkeit aus den .json mittels gson eine Question zu machen

import com.google.gson.*
import java.lang.reflect.Type

class QuestionTypeAdapter : JsonDeserializer<Question> {

    // Bildet den String aus dem JSON ("type": "...") auf die Kotlin-Klasse ab.
    private val typeMap = mapOf(
        "QuestionSlider" to QuestionSlider::class.java,
        "QuestionMultipleChoice" to QuestionMultipleChoice::class.java,
        "QuestionYesNo" to QuestionYesNo::class.java,
        "QuestionTime" to QuestionTime::class.java,
        "QuestionDuration" to QuestionDuration::class.java,
        "QuestionDate" to QuestionDate::class.java,
    )

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Question {

        //json als Objekt auslesen
        val jsonObject = json.asJsonObject

        //type auslesen
        val typeElement = jsonObject.get("type")
            ?: throw JsonParseException("Question JSON muss das Feld 'type' enthalten.")

        val typeName = typeElement.asString

        //String = Kotlinklasse
        val typeClass = typeMap[typeName]
            ?: throw JsonParseException("Unbekannter Question-Typ: $typeName")

        //Parsen
        return context.deserialize(jsonObject, typeClass)
    }
}