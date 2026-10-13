package com.example.schlaftagebuch_vers_1.integration

import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolAnswerDto
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolApi
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolSubmissionRequest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import kotlinx.coroutines.runBlocking
//Teste das Absenden von ausgefüllten Protokollen
class ProtocolApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var protocolApi: ProtocolApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        protocolApi = retrofit.create(ProtocolApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun submitProtocol_Success_ReturnsSubmissionId() = runBlocking {
        // ARRANGE
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                    "submissionId": "sub_123",
                    "subjectRef": "Patient/456",
                    "storedAt": "2026-01-21T10:30:00Z"
                }
            """.trimIndent())
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        val request = ProtocolSubmissionRequest(
            templateKey = "schlaftagebuch_v1",
            locale = "de",
            filledAt = "2026-01-21T09:00:00Z",
            answers = listOf(
                ProtocolAnswerDto(id = 1, result = "22:30"),
                ProtocolAnswerDto(id = 2, result = "07:00"),
                ProtocolAnswerDto(id = 3, result = "Ja")
            )
        )

        // ACT
        val response = protocolApi.submit(request)

        // ASSERT
        assertEquals("sub_123", response.submissionId)
        assertEquals("Patient/456", response.subjectRef)
        assertNotNull(response.storedAt)

        // Request prüfen
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/protocols/submissions", recordedRequest.path)

        val body = recordedRequest.body.readUtf8()
        println("Protocol Submit Body: $body")
        assertTrue(body.contains("schlaftagebuch_v1"))
    }

    @Test
    fun submitProtocol_InvalidData_Returns400() = runBlocking {
        // ARRANGE
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
            .setBody("""{"error":"INVALID_ANSWERS"}""")

        mockWebServer.enqueue(mockResponse)

        val request = ProtocolSubmissionRequest(
            templateKey = "unknown_template",
            locale = "de",
            filledAt = "invalid-date",
            answers = emptyList()
        )

        // ACT & ASSERT
        try {
            protocolApi.submit(request)
            assertTrue("Should throw HttpException", false)
        } catch (e: retrofit2.HttpException) {
            assertEquals(400, e.code())
        }
    }

    @Test
    fun submitProtocol_Unauthorized_Returns401() = runBlocking {
        // ARRANGE - Token fehlt oder abgelaufen
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
            .setBody("""{"error":"UNAUTHORIZED"}""")

        mockWebServer.enqueue(mockResponse)

        val request = ProtocolSubmissionRequest(
            templateKey = "schlaftagebuch_v1",
            locale = "de",
            filledAt = "2026-01-21T09:00:00Z",
            answers = listOf(ProtocolAnswerDto(1, "test"))
        )

        // ACT & ASSERT
        try {
            protocolApi.submit(request)
            assertTrue(false)
        } catch (e: retrofit2.HttpException) {
            assertEquals(401, e.code())
        }
    }
}