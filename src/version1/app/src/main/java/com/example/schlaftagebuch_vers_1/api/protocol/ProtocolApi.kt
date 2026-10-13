package com.example.schlaftagebuch_vers_1.api.protocol

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit-API für Protokoll-Submissions.
 *
 * Zweck:
 * - Definiert HTTP-Endpunkte für das Speichern eines ausgefüllten Protokolls.
 *
 * Verbindung:
 * - Wird in ApiClient als protocolApi erzeugt.
 * - Authorization-Header (Bearer JWT) wird automatisch über OkHttp-Interceptor hinzugefügt.
 */

interface ProtocolApi {
    /**
     * Sendet eine Protokoll-Abgabe (Submission) an das Backend.
     *
     * HTTP:
     * - POST /api/protocols/submissions
     *
     * @param req Request-DTO mit Template-Key, Sprache, Zeitstempel und Antworten
     * @return Response-DTO mit submissionId und Metadaten
     */
    @POST("/api/protocols/submissions")
    suspend fun submit(@Body req: ProtocolSubmissionRequest): ProtocolSubmissionResponse
}