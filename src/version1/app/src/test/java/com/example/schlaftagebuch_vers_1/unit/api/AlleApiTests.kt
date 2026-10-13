package com.example.schlaftagebuch_vers_1.unit.api

import android.content.Context
import android.content.SharedPreferences
import com.example.schlaftagebuch_vers_1.api.JwtUtils
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.SessionStorage
import com.example.schlaftagebuch_vers_1.api.auth.PatientFirstLoginRequest
import com.example.schlaftagebuch_vers_1.api.auth.TokenResponse
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolAnswerDto
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolSubmissionRequest
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolSubmissionResponse
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito


/**
 * EINE Testdatei für ALLE API-Klassen.
 */


    class AlleApiTests {
// Test auskommentiert, da JWT-Dekodierung Android-spezifische Klassen nutzt
// und deshalb nicht in lokalen Unit-Tests ausgeführt werden kann.
//    @Test
//    fun testJwtUtils_ExtractUsername() {
//        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
//
//        // WICHTIG: Padding mit ==
//        val payload = "eyJ1c2VybmFtZSI6InRlc3R1c2VyIn0=="
//
//        val signature = "TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"
//        val jwt = "$header.$payload.$signature"
//
//        val result = JwtUtils.extractUsername(jwt)
//
//        assertNotNull(result)
//
//    }

    @Test
    fun testJwtUtils_ExtractUsername_JwtWithoutUsername() {
        // Arrange: JWT ohne username claim
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJlbWFpbCI6Im1heEB0ZXN0LmRlIn0." +
                "signature"

        // Act
        val result = JwtUtils.extractUsername(jwt)

        // Assert
        Assert.assertNull(result)
    }

    // ==================== TEIL 2: Session Tests ====================

    @Test
    fun testSession_SetAndGetJwt() {
        // Arrange & Act
        Session.jwt = "test.jwt.token"

        // Assert
        Assert.assertEquals("test.jwt.token", Session.jwt)

        // Cleanup für nächste Tests
        Session.jwt = null
    }

    @Test
    fun testSession_SetJwtToNull() {
        // Arrange
        Session.jwt = "some.token"

        // Act
        Session.jwt = null

        // Assert
        Assert.assertNull(Session.jwt)
    }

    // ==================== TEIL 3: SessionStorage Tests ====================

    @Test
    fun testSessionStorage_SaveJwt() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)
        val mockPrefs = Mockito.mock(SharedPreferences::class.java)
        val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)

        Mockito.`when`(mockContext.getSharedPreferences("session", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        Mockito.`when`(mockPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putString("jwt", "test.token.123")).thenReturn(mockEditor)

        // Act
        SessionStorage.saveJwt(mockContext, "test.token.123")

        // Assert
        Mockito.verify(mockEditor).putString("jwt", "test.token.123")
        Mockito.verify(mockEditor).apply()
    }

    @Test
    fun testSessionStorage_LoadJwt() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)
        val mockPrefs = Mockito.mock(SharedPreferences::class.java)

        Mockito.`when`(mockContext.getSharedPreferences("session", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        Mockito.`when`(mockPrefs.getString("jwt", null)).thenReturn("loaded.token.456")

        // Act
        val result = SessionStorage.loadJwt(mockContext)

        // Assert
        // WICHTIG: result ist String?, also vergleiche mit String
        Assert.assertEquals("loaded.token.456", result)
    }

    @Test
    fun testSessionStorage_ClearJwt() {
        // Arrange
        val mockContext = Mockito.mock(Context::class.java)
        val mockPrefs = Mockito.mock(SharedPreferences::class.java)
        val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)

        Mockito.`when`(mockContext.getSharedPreferences("session", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        Mockito.`when`(mockPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.remove("jwt")).thenReturn(mockEditor)

        // Act
        SessionStorage.clear(mockContext)

        // Assert
        Mockito.verify(mockEditor).remove("jwt")
        Mockito.verify(mockEditor).apply()
    }

    // ==================== TEIL 4: Auth DTO Tests ====================

    @Test
    fun testPatientFirstLoginRequest_AllProperties() {
        // Arrange & Act
        val request = PatientFirstLoginRequest(
            code = "ABCD1234",
            consentAccepted = true,
            givenName = "Anna",
            familyName = "Schmidt",
            birthDate = "1985-05-15",
            password = "sicheresPasswort!123"
        )

        // Assert
        Assert.assertEquals("ABCD1234", request.code)
        Assert.assertTrue(request.consentAccepted)  // Boolean
        Assert.assertEquals("Anna", request.givenName)
        Assert.assertEquals("Schmidt", request.familyName)
        Assert.assertEquals("1985-05-15", request.birthDate)
        Assert.assertEquals("sicheresPasswort!123", request.password)
    }

    @Test
    fun testTokenResponse_Constructor() {
        // Arrange & Act
        val response = TokenResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz")

        // Assert
        Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz", response.token)
    }

    // ==================== TEIL 5: Protocol DTO Tests ====================

    @Test
    fun testProtocolAnswerDto_Basic() {
        // Arrange & Act
        val dto = ProtocolAnswerDto(id = 42, result = "7.5 Stunden")

        // Assert
        // WICHTIG: id ist Int, result ist String
        Assert.assertEquals(42, dto.id)  // Int mit Int vergleichen
        Assert.assertEquals("7.5 Stunden", dto.result)  // String mit String vergleichen
    }

    @Test
    fun testProtocolSubmissionRequest_WithAnswers() {
        // Arrange & Act
        val request = ProtocolSubmissionRequest(
            templateKey = "schlafprotokoll_v2",
            locale = "de-DE",
            filledAt = "2024-01-20T08:15:30Z",
            answers = listOf(
                ProtocolAnswerDto(1, "Ja"),
                ProtocolAnswerDto(2, "22:45"),
                ProtocolAnswerDto(3, "Gut"),
                ProtocolAnswerDto(4, "Nein")
            )
        )

        // Assert
        Assert.assertEquals("schlafprotokoll_v2", request.templateKey)
        Assert.assertEquals("de-DE", request.locale)
        Assert.assertEquals("2024-01-20T08:15:30Z", request.filledAt)
        Assert.assertEquals(4, request.answers.size)  // Int mit Int
        Assert.assertEquals("Ja", request.answers[0].result)
        Assert.assertEquals("22:45", request.answers[1].result)  // Korrigiert von 22:45"
    }

    @Test
    fun testProtocolSubmissionResponse_AllFields() {
        // Arrange & Act
        val response = ProtocolSubmissionResponse(
            submissionId = "SUB-2024-001",
            subjectRef = "Patient/78910",
            storedAt = "2024-01-20T08:20:15Z"
        )

        // Assert
        Assert.assertEquals("SUB-2024-001", response.submissionId)
        Assert.assertEquals("Patient/78910", response.subjectRef)
        Assert.assertEquals("2024-01-20T08:20:15Z", response.storedAt)
    }

    // ==================== TEIL 6: Einfache Tests ohne Mockito ====================

    @Test
    fun testSimple_DataClassEquals() {
        // Teste, dass zwei gleiche Data Classes equal sind
        val request1 = PatientFirstLoginRequest(
            code = "123",
            consentAccepted = true,
            givenName = "Test",
            familyName = "User",
            birthDate = "2000-01-01",
            password = "pass"
        )

        val request2 = PatientFirstLoginRequest(
            code = "123",
            consentAccepted = true,
            givenName = "Test",
            familyName = "User",
            birthDate = "2000-01-01",
            password = "pass"
        )

        // Data Classes sollten equal sein
        Assert.assertEquals(request1, request2)
    }

    @Test
    fun testSimple_ToStringContainsData() {
        val dto = ProtocolAnswerDto(id = 99, result = "Test Antwort")
        val str = dto.toString()

        Assert.assertTrue(str.contains("99"))
        Assert.assertTrue(str.contains("Test Antwort"))
    }

    // ==================== TEIL 7: Edge Cases ====================

    @Test
    fun testEdgeCases_EmptyAnswer() {
        val dto = ProtocolAnswerDto(id = 0, result = "")

        Assert.assertEquals(0, dto.id)
        Assert.assertEquals("", dto.result)
    }

    @Test
    fun testEdgeCases_SpecialCharactersInPassword() {
        val request = PatientFirstLoginRequest(
            code = "123",
            consentAccepted = true,
            givenName = "Test",
            familyName = "User",
            birthDate = "2000-01-01",
            password = "P@ssw0rd!#$%&"
        )

        Assert.assertEquals("P@ssw0rd!#$%&", request.password)
    }

    // ==================== TEIL 8: Cleanup ====================

    @After
    fun cleanup() {
        Session.jwt = null
    }
}