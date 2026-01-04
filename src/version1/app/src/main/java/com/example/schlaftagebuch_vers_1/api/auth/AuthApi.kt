package com.example.schlaftagebuch_vers_1.api.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/patient/first-login")
    suspend fun patientFirstLogin(@Body req: PatientFirstLoginRequest): TokenResponse
}