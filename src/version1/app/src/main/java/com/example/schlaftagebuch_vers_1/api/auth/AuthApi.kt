package com.example.schlaftagebuch_vers_1.api.auth

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit-API für First-Login/Login.
 *
 * Zweck:
 * - Definiert HTTP-Endpunkte für den initialen Zugang (Patient First Login) und ein normales Login
 * mit Username + Passwort.
 *
 * Verbindung:
 * - Wird in ApiClient als authApi erzeugt.
 * - Rückgabewert enthält ein JWT, das anschließend in Session/SessionStorage gespeichert wird.
 */

interface AuthApi {

    /**
     * Patient First-Login.
     *
     * HTTP:
     * - POST /api/auth/patient/first-login
     *
     * @param req Request-DTO mit Zugangscode, Einwilligung und Verifikationsdaten
     * @return TokenResponse mit JWT (token)
     */

    @POST("/api/auth/patient/first-login")
    suspend fun patientFirstLogin(@Body req: PatientFirstLoginRequest): TokenResponse

    @POST("/api/auth/patient/login")
    suspend fun patientLogin(@Body req: PatientLoginRequest): TokenResponse
}