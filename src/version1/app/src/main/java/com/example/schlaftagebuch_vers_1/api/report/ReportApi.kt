package com.example.schlaftagebuch_vers_1.api.report

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApi {

    /**
     * Endbericht PDF
     * - Standard: days = 14
     * - Optional: from / to
     * - Testmodus: allSubmissions=true -> nimmt alle Submissions im Zeitraum
     */
    @Streaming
    @Headers("Accept: application/pdf")
    @GET("/api/reports/me/pdf")
    suspend fun myReportPdf(
        @Query("days") days: Int? = 14,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("allSubmissions") allSubmissions: Boolean? = null
    ): Response<ResponseBody>
}
