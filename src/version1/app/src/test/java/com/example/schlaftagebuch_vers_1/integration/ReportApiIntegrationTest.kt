package com.example.schlaftagebuch_vers_1.integration

import com.example.schlaftagebuch_vers_1.api.report.ReportApi
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

import java.net.HttpURLConnection
import kotlinx.coroutines.runBlocking
class ReportApiIntegrationTest {
//Teste PDF-Download mit verschiedenen Parametern
    private lateinit var mockWebServer: MockWebServer
    private lateinit var reportApi: ReportApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .build()

        reportApi = retrofit.create(ReportApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun myReportPdf_DefaultDays_ReturnsPdfBytes() = runBlocking {
        // ARRANGE - Simuliere PDF-Bytes
        val fakePdfContent = "%PDF-1.4\nFake PDF Content".toByteArray()
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(okio.Buffer().write(fakePdfContent))
            .addHeader("Content-Type", "application/pdf")
            .addHeader("Content-Disposition", "attachment; filename=report.pdf")

        mockWebServer.enqueue(mockResponse)

        // ACT
        val response = reportApi.myReportPdf()

        // ASSERT
        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
        assertEquals("application/pdf", response.headers()["Content-Type"])

        val body = response.body()?.bytes()
        assertNotNull(body)
        assertTrue(body!!.isNotEmpty())
        assertTrue(String(body).startsWith("%PDF"))

        // Request prüfen
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertTrue(recordedRequest.path!!.contains("/api/reports/me/pdf"))
        assertTrue(recordedRequest.path!!.contains("days=14"))
    }

    @Test
    fun myReportPdf_WithDateRange_UsesCorrectParams() = runBlocking {
        // ARRANGE
        val fakePdf = "PDF".toByteArray()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(okio.Buffer().write(fakePdf))
                .addHeader("Content-Type", "application/pdf")
        )

        // ACT
        val response = reportApi.myReportPdf(
            days = null,
            from = "2026-01-01",
            to = "2026-01-21",
            allSubmissions = true
        )

        // ASSERT
        assertTrue(response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val path = recordedRequest.path!!

        println("Request Path: $path")
        assertTrue(path.contains("from=2026-01-01"))
        assertTrue(path.contains("to=2026-01-21"))
        assertTrue(path.contains("allSubmissions=true"))
    }

    @Test
    fun myReportPdf_Unauthorized_Returns401() = runBlocking {
        // ARRANGE
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error":"UNAUTHORIZED"}""")
        )

        // ACT
        val response = reportApi.myReportPdf()

        // ASSERT
        assertEquals(401, response.code())
        assertTrue(!response.isSuccessful)
    }

    @Test
    fun myReportPdf_NoData_Returns404() = runBlocking {
        // ARRANGE - Keine Daten im Zeitraum
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody("""{"error":"NO_DATA_FOUND"}""")
        )

        // ACT
        val response = reportApi.myReportPdf(days = 30)

        // ASSERT
        assertEquals(404, response.code())
    }
}