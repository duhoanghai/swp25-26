package com.example.schlaftagebuch_vers_1.api.auth

/**
 * DTOs für First-Login/Login.
 *
 * Hinweis:
 * - Retrofit (Gson) serialisiert diese Datenklassen automatisch zu JSON
 *   und deserialisiert JSON-Antworten wieder zurück.
 */

/**
 * Request für den Patient First-Login (Erstzugang).
 *
 * @param code            Einmalcode (vom Patienten eingegeben)
 * @param consentAccepted Zustimmung zu Datenschutz/Einwilligung
 * @param givenName       Vorname zur Verifikation
 * @param familyName      Nachname zur Verifikation
 * @param birthDate       Geburtsdatum (typisch ISO: YYYY-MM-DD)
 * @param password        Neues Passwort, das beim Erstzugang gesetzt wird
 */

data class PatientFirstLoginRequest(
    val code: String,
    val consentAccepted: Boolean,
    val givenName: String,
    val familyName: String,
    val birthDate: String,
    val password: String
)

data class PatientLoginRequest(
    val username: String,
    val password: String
)

/**
 * Response nach erfolgreichem Login.
 *
 * @param token JWT (wird für Authorization: Bearer <token> verwendet)
 */

data class TokenResponse(
    val token: String
)