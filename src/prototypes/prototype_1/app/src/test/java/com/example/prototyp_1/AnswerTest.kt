/**
 * Dieser Test überprüft, ob die Answer-Klasse Werte korrekt speichert und in verschiedenen Situationen richtig funktioniert.
 *
 * Erstellt von: Marwa Al Siyamji Al Mousli, den 03.12.25
 */
package com.example.prototyp_1
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class AnswerTest {
    lateinit var answer: Answer

    @Before
    fun setUp() {
        answer = Answer(1, "Hallo")
    }

    @Test
    fun testId() {
        assertEquals(1, answer.id)
    }

    @Test
    fun testResult() {
        assertEquals("Hallo", answer.result)
    }

}