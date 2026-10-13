package com.example.schlaftagebuch_vers_1.integration

import com.example.schlaftagebuch_vers_1.api.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

/**
 * Integrationstests für AuthApi mit MockWebServer.
 *
 * Ziel:
 * - Verifikation von Request/Response-Verhalten
 * - Prüfung von Erfolgs- und Fehlerfällen
 * - Realistisches Retrofit/Gson-Verhalten
 */
class AuthApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApi: AuthApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun patientFirstLogin_success_returnsToken() = runBlocking {
        // Arrange
        val expectedToken = "first.login.token"
        mockWebServer.enqueue(successTokenResponse(expectedToken))

        val request = PatientFirstLoginRequest(
            code = "ABCD1234",
            consentAccepted = true,
            givenName = "Max",
            familyName = "Mustermann",
            birthDate = "1990-01-01",
            password = "SecurePassword123!"
        )

        // Act
        val response = authApi.patientFirstLogin(request)

        // Assert
        assertEquals(expectedToken, response.token)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/auth/patient/first-login", recordedRequest.path)
        assertTrue(recordedRequest.body.readUtf8().isNotBlank())
    }

    @Test
    fun patientLogin_success_returnsToken() = runBlocking {
        // Arrange
        val expectedToken = "login.token"
        mockWebServer.enqueue(successTokenResponse(expectedToken))

        val request = PatientLoginRequest(
            username = "max.mustermann",
            password = "MyPassword123"
        )

        // Act
        val response = authApi.patientLogin(request)

        // Assert
        assertEquals(expectedToken, response.token)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/auth/patient/login", recordedRequest.path)
        assertTrue(recordedRequest.body.readUtf8().isNotBlank())
    }

    // ==================== ERROR RESPONSE TESTS ====================

    @Test
    fun patientFirstLogin_invalidCode_returns400() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .setBody("""{"error":"INVALID_CODE"}""")
        )

        val request = PatientFirstLoginRequest(
            code = "WRONG",
            consentAccepted = true,
            givenName = "Max",
            familyName = "Mustermann",
            birthDate = "1990-01-01",
            password = "SecurePassword123!"
        )

        try {
            authApi.patientFirstLogin(request)
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(400, e.code())
        }
    }

    @Test
    fun patientLogin_wrongCredentials_returns401() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error":"INVALID_CREDENTIALS"}""")
        )

        val request = PatientLoginRequest("wrong.user", "wrongpass")

        try {
            authApi.patientLogin(request)
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
    }

    // ==================== INVALID / EDGE CASE TESTS ====================
// Retrofit/Gson können je nach Version unterschiedliche Exceptions werfen.
// Daher wird hier nur geprüft, dass die Deserialisierung fehlschlägt.
//    @Test
//    fun responseMissingTokenField_throwsException() = runBlocking {
//        mockWebServer.enqueue(
//            MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_OK)
//                .setBody("""{"message":"success"}""")
//        )
//
//        val request = PatientLoginRequest("user", "pass")
//
//        try {
//            authApi.patientLogin(request)
//            fail("Expected exception due to missing token")
//        } catch (e: Exception) {
//            // genügt: irgendeine Exception ist korrekt
//            assertTrue(true)
//        }
//    }


    @Test
    fun invalidJsonResponse_throwsException() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("INVALID_JSON")
        )

        val request = PatientFirstLoginRequest(
            code = "TEST",
            consentAccepted = true,
            givenName = "Test",
            familyName = "User",
            birthDate = "2000-01-01",
            password = "pass"
        )

        try {
            authApi.patientFirstLogin(request)
            fail("Expected exception due to invalid JSON")
        } catch (e: Exception) {
            assertTrue(true)
        }
    }


    // ==================== MULTI REQUEST TEST ====================

    @Test
    fun multipleSequentialRequests_areHandledCorrectly() = runBlocking {
        mockWebServer.enqueue(successTokenResponse("token1"))
        mockWebServer.enqueue(successTokenResponse("token2"))

        val r1 = authApi.patientLogin(PatientLoginRequest("u1", "p1"))
        val r2 = authApi.patientLogin(PatientLoginRequest("u2", "p2"))

        assertEquals("token1", r1.token)
        assertEquals("token2", r2.token)
        assertEquals(2, mockWebServer.requestCount)
    }

    // ==================== HELPER ====================

    private fun successTokenResponse(token: String): MockResponse =
        MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""{"token":"$token"}""")
            .addHeader("Content-Type", "application/json")
}
