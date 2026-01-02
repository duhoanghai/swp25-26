/**
 * Dieser Test überprüft, ob die verschiedenen Question-Datenklassen korrekt die Werte speichern und ob ihre typischen Eigenschaften funktionieren.
 *
 * Erstellt von: Marwa Al Siyamji Al Mousli, den 03.12.25
 */

package com.example.prototyp_1

import org.junit.Assert.*
import org.junit.Test

class QuestionTest {


    @Test
    fun `question slider stores correct values`() {
        val q = QuestionSlider(
            id = 1,
            title = "Schlafqualität",
            explanation = "Bewerte deine Schlafqualität",
            sliderMin = 0f,
            sliderMax = 10f
        )

        assertEquals(1, q.id)
        assertEquals("Schlafqualität", q.title)
        assertEquals("Bewerte deine Schlafqualität", q.explanation)
        assertEquals(0f, q.sliderMin)
        assertEquals(10f, q.sliderMax)
    }


    @Test
    fun `question time stores correct values`() {
        val q = QuestionTime(
            id = 1,
            title = "Einschlafzeit",
            explanation = "Wann bist du eingeschlafen?"
        )

        assertEquals(1, q.id)
        assertEquals("Einschlafzeit", q.title)
        assertEquals("Wann bist du eingeschlafen?", q.explanation)
    }


    @Test
    fun `question multiple choice stores values including list`() {
        val choices = listOf("Gut", "Mittel", "Schlecht")

        val q = QuestionMultipleChoice(
            id = 1,
            title = "Schlafqualität",
            explanation = "Wie war deine Nacht?",
            choices = choices
        )

        assertEquals(1, q.id)
        assertEquals("Schlafqualität", q.title)
        assertEquals("Wie war deine Nacht?", q.explanation)
        assertEquals(choices, q.choices)
    }


    @Test
    fun `question yesno stores correct values`() {
        val q = QuestionYesNo(
            id = 1,
            title = "Medikamente",
            explanation = "Nehmen sie Medikamente?"
        )

        assertEquals(4, q.id)
        assertEquals("Medikamente", q.title)
        assertEquals("Nehmen sie Medikamente?", q.explanation)
    }

    @Test
    fun `question date stores correct values`() {
        val q = QuestionDate(
            id = 1,
            title = "Datum",
            explanation = "Tragen Sie das heutiges Datum ein?"
        )

        assertEquals(1, q.id)
        assertEquals("Datum", q.title)
        assertEquals("Tragen Sie das heutiges Datum ein?", q.explanation)
    }
}
