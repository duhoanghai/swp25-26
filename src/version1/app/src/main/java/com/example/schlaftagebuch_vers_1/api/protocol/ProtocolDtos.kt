package com.example.schlaftagebuch_vers_1.api.protocol

/**
 * DTOs für die Protokoll-Submission.
 *
 * Hinweis:
 * - Retrofit (Gson) serialisiert diese Datenklassen automatisch zu JSON
 *   und deserialisiert JSON-Antworten wieder zurück.
 */

/**
 * Ein einzelner Antwort-Eintrag.
 *
 * @param id     ID der Frage (questionId)
 * @param result Antwort als String (z.B. "Ja/Nein", Zahlenwerte, Uhrzeit)
 */

data class ProtocolAnswerDto(
    val id: Int,
    val result: String
)

/**
 * Request zum Speichern einer Protokoll-Abgabe.
 *
 * @param templateKey Identifiziert das Protokoll-Template (z.B. "schlaftagebuch_v1")
 * @param locale      Sprache/Locale (z.B. "de")
 * @param filledAt    Zeitpunkt der Eingabe durch den Nutzer (ISO-String)
 * @param answers     Liste der Antworten (id + result)
 */

data class ProtocolSubmissionRequest(
    val templateKey: String,
    val locale: String,
    val filledAt: String,
    val answers: List<ProtocolAnswerDto>
)

/**
 * Response nach erfolgreichem Speichern der Submission.
 *
 * @param submissionId ID der gespeicherten Submission (Server-seitig)
 * @param subjectRef   Referenz auf das Subject/Patient (z.B. "Patient/123")
 * @param storedAt     Zeitpunkt der Speicherung im Backend (ISO-String)
 */

data class ProtocolSubmissionResponse(
    val submissionId: String,
    val subjectRef: String,
    val storedAt: String
)