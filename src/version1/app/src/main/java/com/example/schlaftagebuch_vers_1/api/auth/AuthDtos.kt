package com.example.schlaftagebuch_vers_1.api.auth

data class PatientFirstLoginRequest(
    val code: String,
    val consentAccepted: Boolean,
    val givenName: String,
    val familyName: String,
    val birthDate: String,
    val password: String
)

data class TokenResponse(
    val token: String
)