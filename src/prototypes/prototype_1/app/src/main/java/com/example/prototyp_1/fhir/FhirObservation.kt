package com.example.prototyp_1.fhir

import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * FHIR R4 Observation Resource
 * Vereinfachte Implementierung für Gesundheitsdaten
 */
data class FhirObservation(
    @SerializedName("resourceType")
    val resourceType: String = "Observation",

    @SerializedName("id")
    val id: String,

    @SerializedName("status")
    val status: String = "final", // final, preliminary, registered, amended, corrected

    @SerializedName("code")
    val code: CodeableConcept,

    @SerializedName("subject")
    val subject: Reference,

    @SerializedName("effectiveDateTime")
    val effectiveDateTime: String, // ISO 8601 Format: "2024-12-17T10:30:00+01:00"

    @SerializedName("valueQuantity")
    val valueQuantity: Quantity? = null,

    @SerializedName("valueString")
    val valueString: String? = null,

    @SerializedName("valueBoolean")
    val valueBoolean: Boolean? = null,

    @SerializedName("valueDateTime")
    val valueDateTime: String? = null,

    @SerializedName("valueCodeableConcept")
    val valueCodeableConcept: CodeableConcept? = null,

    @SerializedName("interpretation")
    val interpretation: List<CodeableConcept>? = null,

    @SerializedName("note")
    val note: List<Annotation>? = null
)

/**
 * CodeableConcept - Kodierte Konzepte (z.B. LOINC, SNOMED CT)
 */
data class CodeableConcept(
    @SerializedName("coding")
    val coding: List<Coding>,

    @SerializedName("text")
    val text: String? = null
)

/**
 * Coding - Einzelner Code aus einem Codesystem
 */
data class Coding(
    @SerializedName("system")
    val system: String, // z.B. "http://loinc.org" oder "http://snomed.info/sct"

    @SerializedName("code")
    val code: String,

    @SerializedName("display")
    val display: String? = null
)

/**
 * Reference - Verweis auf andere Ressourcen (z.B. Patient)
 */
data class Reference(
    @SerializedName("reference")
    val reference: String, // z.B. "Patient/123"

    @SerializedName("display")
    val display: String? = null
)

/**
 * Quantity - Messwert mit Einheit
 */
data class Quantity(
    @SerializedName("value")
    val value: Double,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("system")
    val system: String = "http://unitsofmeasure.org", // UCUM

    @SerializedName("code")
    val code: String // UCUM Code z.B. "kg", "cm", "mg/dL"
)

/**
 * Annotation - Notizen/Kommentare
 */
data class Annotation(
    @SerializedName("text")
    val text: String,

    @SerializedName("time")
    val time: String? = null
)

/**
 * Helper-Funktion für ISO 8601 Zeitstempel
 */
fun getCurrentFhirDateTime(): String {
    return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}