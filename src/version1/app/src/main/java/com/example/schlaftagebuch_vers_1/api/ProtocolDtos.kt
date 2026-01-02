package com.example.schlaftagebuch_vers_1.api

data class ProtocolAnswerDto(
    val id: Int,
    val result: String
)

data class ProtocolSubmissionRequest(
    val templateKey: String,
    val locale: String,
    val filledAt: String,
    val answers: List<ProtocolAnswerDto>
)

data class ProtocolSubmissionResponse(
    val submissionId: String,
    val subjectRef: String,
    val storedAt: String
)