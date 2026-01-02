/**
 * Tests für den QuestionTypeAdapter.
 *
 * Testet:
 *  ob der Adapter aus dem "type" Feld im JSON die richtige Frage-Art auswählt
 *    (z. B. Slider-Frage, Ja/Nein-Frage, Zeit-Frage …).
 *
 *  ob die Daten aus dem JSON richtig in das passende Question-Kotlinobjekt
 *    übernommen werden (z. B. id, min/max Werte).
 *
 *  ob ein Fehler geworfen wird, wenn im "type" Feld ein falscher oder
 *    unbekannter Typ steht.
 *
 *  ob ein Fehler geworfen wird, wenn das "type" Feld im JSON fehlt.
 *
 *  Erstellt von: Marwa Al Siyamji Al Mousli, den 03.12.25
 */


package com.example.prototyp_1

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestionTypeAdapterTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()
    }
    // Gültige Frage Typen?
    @Test
    fun `deserialize QuestionSlider`() {
        val json = """
            {
              "id": 1,
              "title": "Test",
              "explanation": "Slider Test",
              "type": "QuestionSlider",
              "sliderMin": 0,
              "sliderMax": 10
            }
        """.trimIndent()

      //GSON nimmt den JSON-String und baut daraus ein Kotlin-Objekt.
        val q = gson.fromJson(json, Question::class.java)

        //richtige Typ?
        assertTrue(q is QuestionSlider)

        // wurde JSON korrekt geparst?
        // Die String-Felder werden bereits vollständig im QuestionTest getestet.(Keine Redundanzen)
        q as QuestionSlider
        assertEquals(1, q.id)
        assertEquals(0f, q.sliderMin)
        assertEquals(10f, q.sliderMax)
    }
    @Test
    fun `deserialize QuestionTime`() {
        val json = """
            {
              "id": 1,
              "title": "Test",
              "explanation": "time",
              "type": "QuestionTime"
            }
        """.trimIndent()

        val q = gson.fromJson(json, Question::class.java)

        assertTrue(q is QuestionTime)
        assertEquals(1, q.id)
    }
    @Test
    fun `deserialize QuestionMultipleChoice`() {
        val json = """
            {
              "id": 1,
              "title": "Test",
              "explanation": "Multiple Choice",
              "type": "QuestionMultipleChoice",
              "choices": ["A", "B", "C"]
            }
        """.trimIndent()

        val q = gson.fromJson(json, Question::class.java)

        assertTrue(q is QuestionMultipleChoice)

        q as QuestionMultipleChoice
        assertEquals(1, q.id)
        assertEquals(listOf("A", "B", "C"), q.choices)

    }

    @Test
    fun `deserialize QuestionYesNo`() {
        val json = """
            {
              "id": 1,
              "title": "Test",
              "explanation": "YesNo",
              "type": "QuestionYesNo"
            }
        """.trimIndent()

        val q = gson.fromJson(json, Question::class.java)

        assertTrue(q is QuestionYesNo)
        assertEquals(1, q.id)
    }

    @Test
    fun `deserialize QuestionDate`() {
        val json = """
            {
              "id": 1,
              "title": "Test",
              "explanation": "Date",
              "type": "QuestionDate"
            }
        """.trimIndent()

        val q = gson.fromJson(json, Question::class.java)


        assertTrue(q is QuestionDate)
        assertEquals(1, q.id)
    }




    // Fehlerfälle



    @Test
    fun `unknown type throws error`() {
        val json = """
            {
              "id": 1,
              "title": "Fehler",
              "explanation": "Falscher Typ",
              "type": "UnknownType123"
            }
        """.trimIndent()

        assertThrows(JsonParseException::class.java) {
            gson.fromJson(json, Question::class.java)
        }
    }
    @Test
    fun `missing type throws error`() {
        val json = """
            {
              "id": 1,
              "title": "Fehler",
              "explanation": "Kein Typ"
            }
        """.trimIndent()

        assertThrows(JsonParseException::class.java) {
            gson.fromJson(json, Question::class.java)
        }
    }
}
