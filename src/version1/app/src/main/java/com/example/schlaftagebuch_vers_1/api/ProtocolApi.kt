package com.example.schlaftagebuch_vers_1.api

import retrofit2.http.Body
import retrofit2.http.POST

interface ProtocolApi {
    @POST("/api/protocols/submissions")
    suspend fun submit(@Body req: ProtocolSubmissionRequest): ProtocolSubmissionResponse
}