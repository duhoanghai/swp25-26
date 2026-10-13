package com.example.schlaftagebuch_vers_1.dataService

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.time.LocalTime

/**
 * Komplette Unit-Tests für ALLE DataService-Klassen in EINER Datei.
 * Testet: Answer.kt, DataLoader.kt, Question.kt, QuestionTypeAdapter.kt
 */
class AllDataServiceTests {

    // ==================== TEIL 1: Answer.kt Tests ====================

    @Test
    fun testAnswer_ConstructorAndProperties() {
        // Arrange & Act
        val answer = Answer(id = 42, result = "Test Antwort")

        // Assert
        assertEquals(42, answer.id)
        assertEquals("Test Antwort", answer.result)
    }


    // ==================== TEIL 2: Question.kt Tests ====================

    @Test
    fun testQuestionSlider_AllProperties() {
        // Arrange & Act
        val question = QuestionSlider(
            id = 1,
            title = "Wie zufrieden sind Sie?",
            explanation = "Bewerten Sie von 1-10",
            sliderMin = 1.0f,
            sliderMax = 10.0f
        )

        // Assert
        assertEquals(1, question.id)
        assertEquals("Wie zufrieden sind Sie?", question.title)
        assertEquals("Bewerten Sie von 1-10", question.explanation)
        assertEquals(1.0f, question.sliderMin)
        assertEquals(10.0f, question.sliderMax)
        assertTrue(question is Question)
    }

    @Test
    fun testQuestionTime_AllProperties() {
        // Arrange & Act
        val question = QuestionTime(
            id = 2,
            title = "Wann sind Sie eingeschlafen?",
            explanation = "Bitte Uhrzeit eingeben"
        )

        // Assert
        assertEquals(2, question.id)
        assertEquals("Wann sind Sie eingeschlafen?", question.title)
        assertEquals("Bitte Uhrzeit eingeben", question.explanation)
    }

    @Test
    fun testQuestionDuration_AllProperties() {
        // Arrange & Act
        val question = QuestionDuration(
            id = 3,
            title = "Wie lange haben Sie geschlafen?",
            explanation = "Dauer in Stunden"
        )

        // Assert
        assertEquals(3, question.id)
        assertEquals("Wie lange haben Sie geschlafen?", question.title)
        assertEquals("Dauer in Stunden", question.explanation)
    }

    @Test
    fun testQuestionMultipleChoice_AllProperties() {
        // Arrange & Act
        val question = QuestionMultipleChoice(
            id = 4,
            title = "Wie fühlen Sie sich?",
            explanation = "Wählen Sie eine Option",
            choices = listOf("Gut", "Mittel", "Schlecht")
        )

        // Assert
        assertEquals(4, question.id)
        assertEquals("Wie fühlen Sie sich?", question.title)
        assertEquals("Wählen Sie eine Option", question.explanation)
        assertEquals(3, question.choices.size)
        assertEquals("Gut", question.choices[0])
        assertEquals("Mittel", question.choices[1])
        assertEquals("Schlecht", question.choices[2])
    }

    @Test
    fun testQuestionYesNo_AllProperties() {
        // Arrange & Act
        val question = QuestionYesNo(
            id = 5,
            title = "Hatten Sie Albträume?",
            explanation = "Ja oder Nein",
            skipIfNo = listOf(6, 7, 8)
        )

        // Assert
        assertEquals(5, question.id)
        assertEquals("Hatten Sie Albträume?", question.title)
        assertEquals("Ja oder Nein", question.explanation)
        assertEquals(3, question.skipIfNo.size)
        assertTrue(question.skipIfNo.contains(6))
        assertTrue(question.skipIfNo.contains(7))
        assertTrue(question.skipIfNo.contains(8))
    }

    @Test
    fun testQuestionYesNo_EmptySkipIfNo() {
        // Arrange & Act
        val question = QuestionYesNo(
            id = 9,
            title = "Test",
            explanation = "Test",
            skipIfNo = emptyList()
        )

        // Assert
        assertTrue(question.skipIfNo.isEmpty())
    }

    @Test
    fun testQuestionDate_AllProperties() {
        // Arrange & Act
        val question = QuestionDate(
            id = 10,
            title = "Wann war das?",
            explanation = "Datum auswählen"
        )

        // Assert
        assertEquals(10, question.id)
        assertEquals("Wann war das?", question.title)
        assertEquals("Datum auswählen", question.explanation)
    }

    @Test
    fun testAllQuestionTypes_AreSubclassesOfQuestion() {
        // Teste, dass alle Typen von Question erben
        val slider = QuestionSlider(1, "t", "e", 0f, 10f)
        val time = QuestionTime(2, "t", "e")
        val duration = QuestionDuration(3, "t", "e")
        val mc = QuestionMultipleChoice(4, "t", "e", listOf("a", "b"))
        val yesNo = QuestionYesNo(5, "t", "e", emptyList())
        val date = QuestionDate(6, "t", "e")

        assertTrue(slider is Question)
        assertTrue(time is Question)
        assertTrue(duration is Question)
        assertTrue(mc is Question)
        assertTrue(yesNo is Question)
        assertTrue(date is Question)
    }

    // ==================== TEIL 3: DataLoader.kt Tests (SharedPreferences) ====================

    @Test
    fun testDataLoader_SaveAndGetLanguage() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString("LanguageCode", "EN")).thenReturn(mockEditor)
        `when`(mockPrefs.getString("LanguageCode", "DE")).thenReturn("EN")

        // Act & Assert - save
        DataLoader.saveLanguage(mockContext, "EN")
        verify(mockEditor).putString("LanguageCode", "EN")

        // Act & Assert - get
        val result = DataLoader.getLanguage(mockContext)
        assertEquals("EN", result)
    }

    @Test
    fun testDataLoader_GetLanguage_DefaultValue() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.getString("LanguageCode", "DE")).thenReturn(null)

        // Act
        val result = DataLoader.getLanguage(mockContext)

        // Assert
        assertEquals("DE", result) // Default value
    }

    @Test
    fun testDataLoader_SaveAndLoadAlarmState() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean("alarm_enabled", true)).thenReturn(mockEditor)
        `when`(mockPrefs.getBoolean("alarm_enabled", false)).thenReturn(true)

        // Act & Assert - save
        DataLoader.saveAlarmState(mockContext, true)
        verify(mockEditor).putBoolean("alarm_enabled", true)

        // Act & Assert - load
        val result = DataLoader.loadAlarmState(mockContext)
        assertTrue(result)
    }

    @Test
    fun testDataLoader_SaveAndLoadAlarmTime() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt("alarm_hour", 22)).thenReturn(mockEditor)
        `when`(mockEditor.putInt("alarm_minute", 30)).thenReturn(mockEditor)
        `when`(mockPrefs.getInt("alarm_hour", 20)).thenReturn(22)
        `when`(mockPrefs.getInt("alarm_minute", 0)).thenReturn(30)

        // Act & Assert - save
        val timeToSave = LocalTime.of(22, 30)
        DataLoader.saveAlarmTime(mockContext, timeToSave)

        verify(mockEditor).putInt("alarm_hour", 22)
        verify(mockEditor).putInt("alarm_minute", 30)

        // Act & Assert - load
        val loadedTime = DataLoader.loadAlarmTime(mockContext)
        assertEquals(22, loadedTime.hour)
        assertEquals(30, loadedTime.minute)
    }

    @Test
    fun testDataLoader_LoadAlarmTime_DefaultValues() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.getInt("alarm_hour", 20)).thenReturn(20) // Default
        `when`(mockPrefs.getInt("alarm_minute", 0)).thenReturn(0) // Default

        // Act
        val result = DataLoader.loadAlarmTime(mockContext)

        // Assert
        assertEquals(20, result.hour)
        assertEquals(0, result.minute)
    }

    @Test
    fun testDataLoader_SaveAndGetColorTheme() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString("theme", "dark")).thenReturn(mockEditor)
        `when`(mockPrefs.getString("theme", "system")).thenReturn("dark")

        // Act & Assert - save
        DataLoader.saveColorTheme(mockContext, "dark")
        verify(mockEditor).putString("theme", "dark")

        // Act & Assert - get
        val result = DataLoader.getColorTheme(mockContext)
        assertEquals("dark", result)
    }

    @Test
    fun testDataLoader_GetColorTheme_DefaultValue() {
        // Arrange
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)

        `when`(mockContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.getString("theme", "system")).thenReturn("system")

        // Act
        val result = DataLoader.getColorTheme(mockContext)

        // Assert
        assertEquals("system", result)
    }
    @Test
    fun testDataLoader_GetQuestions_FileNotFound() {
        val context = mock(Context::class.java)
        val assetManager = mock(android.content.res.AssetManager::class.java)

        `when`(context.assets).thenReturn(assetManager)
        `when`(assetManager.open("Questions_DE.json"))
            .thenThrow(java.io.IOException("File not found"))

        val result = DataLoader.getQuestions(context, "DE")

        assertTrue(result.isEmpty())
    }



    @Test
    fun testDataLoader_FirstTime_DefaultTrue() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)

        `when`(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(prefs)
        `when`(prefs.getBoolean("isFirstTime", true))
            .thenReturn(true)

        assertTrue(DataLoader.isFirstTime(context))
    }

    @Test
    fun testDataLoader_SetFirstTimeCompleted() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        `when`(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(prefs)
        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.putBoolean("isFirstTime", false)).thenReturn(editor)

        DataLoader.setFirstTimeCompleted(context)

        verify(editor).putBoolean("isFirstTime", false)
    }
    @Test
    fun testDataLoader_SaveAndGetStoredUsername() {
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        `when`(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
            .thenReturn(prefs)
        `when`(prefs.edit()).thenReturn(editor)
        `when`(editor.putString("stored_username", "marwa")).thenReturn(editor)
        `when`(prefs.getString("stored_username", null)).thenReturn("marwa")

        DataLoader.saveStoredUsername(context, "marwa")
        val result = DataLoader.getStoredUsername(context)

        assertEquals("marwa", result)
    }


    // ==================== TEIL 4: Gson Serialization Tests ====================

    @Test
    fun testGson_SerializeAnswer() {
        // Arrange
        val gson = GsonBuilder().create()
        val answer = Answer(id = 1, result = "Test")

        // Act
        val json = gson.toJson(answer)

        // Assert
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"result\":\"Test\""))
    }

    @Test
    fun testGson_DeserializeAnswer() {
        // Arrange
        val gson = GsonBuilder().create()
        val json = """{"id":5,"result":"Ja"}"""

        // Act
        val answer = gson.fromJson(json, Answer::class.java)

        // Assert
        assertEquals(5, answer.id)
        assertEquals("Ja", answer.result)
    }

    @Test
    fun testGson_SerializeQuestionList() {
        // Arrange
        val gson = GsonBuilder().create()
        val questions = listOf(
            Answer(1, "A"),
            Answer(2, "B"),
            Answer(3, "C")
        )

        // Act
        val json = gson.toJson(questions)

        // Assert
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"id\":2"))
        assertTrue(json.contains("\"id\":3"))
    }

    @Test
    fun testGson_QuestionSlider_RoundTrip_WithType() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
        {
            "type": "QuestionSlider",
            "id": 1,
            "title": "Slider",
            "explanation": "Desc",
            "sliderMin": 0.0,
            "sliderMax": 10.0
        }
    """.trimIndent()

        val parsed = gson.fromJson(json, Question::class.java)

        assertTrue(parsed is QuestionSlider)

        val slider = parsed as QuestionSlider
        assertEquals(1, slider.id)
        assertEquals("Slider", slider.title)
        assertEquals(0.0f, slider.sliderMin)
        assertEquals(10.0f, slider.sliderMax)
    }



    // ==================== TEIL 5: QuestionTypeAdapter.kt Tests ====================

    @Test
    fun testQuestionTypeAdapter_TypeMap() {
        // Arrange
        val adapter = QuestionTypeAdapter()

        // Act & Assert - Test durch Reflection (oder wir testen die Deserialisierung direkt)
        // Da typeMap private ist, testen wir indirekt
        assertNotNull(adapter)
    }



    @Test
    fun testQuestionDeserialization_QuestionSlider() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionSlider",
                "id": 1,
                "title": "Test Slider",
                "explanation": "Explanation",
                "sliderMin": 0.0,
                "sliderMax": 10.0
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionSlider)
        assertEquals(1, question.id)
        assertEquals("Test Slider", question.title)
        assertEquals("Explanation", question.explanation)

        val slider = question as QuestionSlider
        assertEquals(0.0f, slider.sliderMin)
        assertEquals(10.0f, slider.sliderMax)
    }

    @Test
    fun testQuestionDeserialization_QuestionMultipleChoice() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionMultipleChoice",
                "id": 2,
                "title": "Test MC",
                "explanation": "Choose one",
                "choices": ["Option A", "Option B", "Option C"]
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionMultipleChoice)
        assertEquals(2, question.id)

        val mc = question as QuestionMultipleChoice
        assertEquals(3, mc.choices.size)
        assertEquals("Option A", mc.choices[0])
        assertEquals("Option B", mc.choices[1])
        assertEquals("Option C", mc.choices[2])
    }

    @Test
    fun testQuestionDeserialization_QuestionYesNo() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionYesNo",
                "id": 3,
                "title": "Test YesNo",
                "explanation": "Answer yes or no",
                "skipIfNo": [4, 5, 6]
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionYesNo)

        val yesNo = question as QuestionYesNo
        assertEquals(3, yesNo.skipIfNo.size)
        assertTrue(yesNo.skipIfNo.contains(4))
        assertTrue(yesNo.skipIfNo.contains(5))
        assertTrue(yesNo.skipIfNo.contains(6))
    }

    @Test
    fun testQuestionDeserialization_QuestionTime() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionTime",
                "id": 4,
                "title": "Test Time",
                "explanation": "Enter time"
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionTime)
        assertEquals(4, question.id)
        assertEquals("Test Time", question.title)
    }

    @Test
    fun testQuestionDeserialization_QuestionDuration() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionDuration",
                "id": 5,
                "title": "Test Duration",
                "explanation": "Enter duration"
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionDuration)
        assertEquals(5, question.id)
    }

    @Test
    fun testQuestionDeserialization_QuestionDate() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "QuestionDate",
                "id": 6,
                "title": "Test Date",
                "explanation": "Enter date"
            }
        """.trimIndent()

        // Act
        val question = gson.fromJson(json, Question::class.java)

        // Assert
        assertTrue(question is QuestionDate)
        assertEquals(6, question.id)
    }

    @Test
    fun testQuestionDeserialization_InvalidType() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "type": "InvalidType",
                "id": 99,
                "title": "Test"
            }
        """.trimIndent()

        // Act & Assert - sollte Exception werfen
        try {
            gson.fromJson(json, Question::class.java)
            assertFalse(true) // Should not reach here
        } catch (e: Exception) {
            // Expected - sollte JsonParseException sein
            assertTrue(e is com.google.gson.JsonParseException)
        }
    }


    @Test
    fun testQuestionDeserialization_MissingType() {
        // Arrange
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
            {
                "id": 99,
                "title": "Test"
            }
        """.trimIndent()

        // Act & Assert - sollte Exception werfen
        try {
            gson.fromJson(json, Question::class.java)
            assertFalse(true) // Should not reach here
        } catch (e: Exception) {
            // Expected
            assertTrue(e is com.google.gson.JsonParseException)
        }
    }
    @Test
    fun testQuestionDeserialization_EmptyType() {
        val gson = GsonBuilder()
            .registerTypeAdapter(Question::class.java, QuestionTypeAdapter())
            .create()

        val json = """
        {
            "type": "",
            "id": 1,
            "title": "Test"
        }
    """

        try {
            gson.fromJson(json, Question::class.java)
            assertFalse(true)
        } catch (e: Exception) {
            assertTrue(e is com.google.gson.JsonParseException)
        }
    }

    // ==================== TEIL 6: DataLoader - Save Answers Tests ====================
    @Test
    fun testDataLoader_SaveAnswers_WritesFile() {
        val context = mock(Context::class.java)
        val stream = mock(java.io.FileOutputStream::class.java)

        `when`(context.openFileOutput("answers.json", Context.MODE_PRIVATE))
            .thenReturn(stream)

        val answers = listOf(
            Answer(1, "A"),
            Answer(2, "B")
        )

        DataLoader.saveAnswers(context, answers)

        verify(context).openFileOutput("answers.json", Context.MODE_PRIVATE)
    }


    // ==================== TEIL 7: Integration Tests (ohne Android-Abhängigkeiten) ====================

    @Test
    fun testQuestionHierarchy() {
        // Teste die Vererbungshierarchie
        val questions: List<Question> = listOf(
            QuestionSlider(1, "Slider", "Desc", 0f, 10f),
            QuestionTime(2, "Time", "Desc"),
            QuestionDuration(3, "Duration", "Desc"),
            QuestionMultipleChoice(4, "MC", "Desc", listOf("A", "B")),
            QuestionYesNo(5, "YesNo", "Desc", listOf(6)),
            QuestionDate(6, "Date", "Desc")
        )

        assertEquals(6, questions.size)
        questions.forEach { assertTrue(it is Question) }
    }

    @Test
    fun testAnswerListOperations() {
        // Arrange
        val answers = mutableListOf<Answer>()

        // Act
        answers.add(Answer(1, "A"))
        answers.add(Answer(2, "B"))
        answers.add(Answer(3, "C"))

        // Assert
        assertEquals(3, answers.size)
        assertEquals(1, answers[0].id)
        assertEquals(2, answers[1].id)
        assertEquals(3, answers[2].id)

        // Test find
        val answer2 = answers.find { it.id == 2 }
        assertNotNull(answer2)
        assertEquals("B", answer2?.result)

        // Test filter
        val filtered = answers.filter { it.id > 1 }
        assertEquals(2, filtered.size)
    }

    // ==================== TEIL 8: Edge Cases ====================

    @Test
    fun testEdgeCases_EmptyStrings() {
        val answer = Answer(0, "")
        assertEquals("", answer.result)

        val question = QuestionTime(0, "", "")
        assertEquals("", question.title)
        assertEquals("", question.explanation)
    }

    @Test
    fun testEdgeCases_SpecialCharacters() {
        val answer = Answer(1, "ÄÖÜß © ® ™")
        assertEquals("ÄÖÜß © ® ™", answer.result)

        val question = QuestionTime(1, "Frage mit Umlauten: äöü", "Beschreibung mit Sonderzeichen: !@#$%")
        assertEquals("Frage mit Umlauten: äöü", question.title)
        assertEquals("Beschreibung mit Sonderzeichen: !@#$%", question.explanation)
    }

    @Test
    fun testEdgeCases_MaxValues() {
        val slider = QuestionSlider(
            id = Int.MAX_VALUE,
            title = "Test",
            explanation = "Test",
            sliderMin = Float.MAX_VALUE,
            sliderMax = Float.MAX_VALUE
        )

        assertEquals(Int.MAX_VALUE, slider.id)
        assertEquals(Float.MAX_VALUE, slider.sliderMin, 0.001f)
        assertEquals(Float.MAX_VALUE, slider.sliderMax, 0.001f)
    }

    @Test
    fun testEdgeCases_NegativeValues() {
        val slider = QuestionSlider(
            id = -1,
            title = "Test",
            explanation = "Test",
            sliderMin = -10.0f,
            sliderMax = -1.0f
        )

        assertEquals(-1, slider.id)
        assertEquals(-10.0f, slider.sliderMin, 0.001f)
        assertEquals(-1.0f, slider.sliderMax, 0.001f)
    }

    // ==================== TEIL 9: Helper Methods ====================

    @Test
    fun testListContains() {
        val yesNoQuestion = QuestionYesNo(
            id = 1,
            title = "Test",
            explanation = "Test",
            skipIfNo = listOf(2, 3, 4, 5)
        )

        assertTrue(yesNoQuestion.skipIfNo.contains(3))
        assertFalse(yesNoQuestion.skipIfNo.contains(99))
    }

    @Test
    fun testJsonFormat() {
        // Teste dass JSON korrekt formatiert werden kann
        val answer = Answer(100, "Test Answer with \"quotes\"")
        val gson = GsonBuilder().create()

        val json = gson.toJson(answer)

        // JSON sollte korrekt escaped sein
        assertTrue(json.contains("\"id\":100"))
        assertTrue(json.contains("Test Answer with \\\"quotes\\\""))
    }
}