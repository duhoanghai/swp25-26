package com.example.prototyp_1.fhir

import com.example.prototyp_1.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Mapper für Umwandlung von App-Fragen zu FHIR Observations
 */
object FhirMapper {

    /**
     * Hauptmapping-Funktion
     * @param question Die beantwortete Frage
     * @param rawAnswer Die Antwort als String
     * @param patientId Die ID des Patienten
     * @return FHIR Observation oder null wenn Mapping nicht möglich
     */
    fun mapToFhir(
        question: Question,
        rawAnswer: String,
        patientId: String
    ): FhirObservation? {

        if (rawAnswer.isBlank()) return null

        return when (question) {
            is QuestionSlider -> mapSliderToFhir(question, rawAnswer, patientId)
            is QuestionTime -> mapTimeToFhir(question, rawAnswer, patientId)
            is QuestionMultipleChoice -> mapMultipleChoiceToFhir(question, rawAnswer, patientId)
            is QuestionYesNo -> mapYesNoToFhir(question, rawAnswer, patientId)
            is QuestionDate -> mapDateToFhir(question, rawAnswer, patientId)
        }
    }

    /**
     * Slider -> Quantity Observation (numerische Werte)
     */
    private fun mapSliderToFhir(
        question: QuestionSlider,
        rawAnswer: String,
        patientId: String
    ): FhirObservation? {

        val value = rawAnswer.toDoubleOrNull() ?: return null

        // Beispiel: Mapping basierend auf Frage-ID oder Titel
        val (loincCode, display, unit, ucumCode) = getSliderMapping(question)

        return FhirObservation(
            id = UUID.randomUUID().toString(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = "http://loinc.org",
                        code = loincCode,
                        display = display
                    )
                ),
                text = question.title
            ),
            subject = Reference(
                reference = "Patient/$patientId"
            ),
            effectiveDateTime = getCurrentFhirDateTime(),
            valueQuantity = Quantity(
                value = value,
                unit = unit,
                code = ucumCode
            )
        )
    }

    /**
     * Zeit -> String Observation
     */
    private fun mapTimeToFhir(
        question: QuestionTime,
        rawAnswer: String,
        patientId: String
    ): FhirObservation {

        return FhirObservation(
            id = UUID.randomUUID().toString(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = "http://loinc.org",
                        code = "93043-8", // Time of event
                        display = "Time"
                    )
                ),
                text = question.title
            ),
            subject = Reference(
                reference = "Patient/$patientId"
            ),
            effectiveDateTime = getCurrentFhirDateTime(),
            valueString = rawAnswer
        )
    }

    /**
     * Multiple Choice -> CodeableConcept Observation
     */
    private fun mapMultipleChoiceToFhir(
        question: QuestionMultipleChoice,
        rawAnswer: String,
        patientId: String
    ): FhirObservation {

        val selectedOptions = rawAnswer.split(",").map { it.trim() }

        return FhirObservation(
            id = UUID.randomUUID().toString(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = "http://loinc.org",
                        code = "74465-6", // Questionnaire response
                        display = question.title
                    )
                ),
                text = question.title
            ),
            subject = Reference(
                reference = "Patient/$patientId"
            ),
            effectiveDateTime = getCurrentFhirDateTime(),
            valueCodeableConcept = CodeableConcept(
                coding = selectedOptions.mapIndexed { index, option ->
                    Coding(
                        system = "http://example.org/fhir/CodeSystem/questionnaire-answers",
                        code = "q${question.id}-opt${index}",
                        display = option
                    )
                },
                text = rawAnswer
            )
        )
    }

    /**
     * Yes/No -> Boolean Observation
     */
    private fun mapYesNoToFhir(
        question: QuestionYesNo,
        rawAnswer: String,
        patientId: String
    ): FhirObservation {

        val boolValue = when (rawAnswer.lowercase()) {
            "ja", "yes" -> true
            "nein", "no" -> false
            else -> null
        }

        return FhirObservation(
            id = UUID.randomUUID().toString(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = "http://loinc.org",
                        code = "74465-6",
                        display = question.title
                    )
                ),
                text = question.title
            ),
            subject = Reference(
                reference = "Patient/$patientId"
            ),
            effectiveDateTime = getCurrentFhirDateTime(),
            valueBoolean = boolValue
        )
    }

    /**
     * Date -> DateTime Observation
     */
    private fun mapDateToFhir(
        question: QuestionDate,
        rawAnswer: String,
        patientId: String
    ): FhirObservation? {

        // Parse deutsches Format "dd.MM.yyyy"
        val date = try {
            LocalDate.parse(rawAnswer, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            return null
        }

        return FhirObservation(
            id = UUID.randomUUID().toString(),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = "http://loinc.org",
                        code = "74465-6",
                        display = question.title
                    )
                ),
                text = question.title
            ),
            subject = Reference(
                reference = "Patient/$patientId"
            ),
            effectiveDateTime = getCurrentFhirDateTime(),
            valueDateTime = date.toString() // ISO format: "2024-12-17"
        )
    }

    /**
     * Mapping-Tabelle für Slider-Fragen zu LOINC-Codes
     */
    private fun getSliderMapping(question: QuestionSlider): SliderMapping {
        // Beispiel-Mappings - ANPASSEN an deine Fragen!
        return when {
            question.title.contains("Gewicht", ignoreCase = true) ->
                SliderMapping("29463-7", "Body weight", "kg", "kg")

            question.title.contains("Größe", ignoreCase = true) ->
                SliderMapping("8302-2", "Body height", "cm", "cm")

            question.title.contains("Temperatur", ignoreCase = true) ->
                SliderMapping("8310-5", "Body temperature", "°C", "Cel")

            question.title.contains("Schmerz", ignoreCase = true) ->
                SliderMapping("72514-3", "Pain severity", "Score", "{score}")

            question.title.contains("Blutdruck", ignoreCase = true) ->
                SliderMapping("8480-6", "Systolic blood pressure", "mmHg", "mm[Hg]")

            else ->
                SliderMapping("74465-6", question.title, "Score", "{score}")
        }
    }

    /**
     * Daten-Klasse für Slider-Mapping
     */
    private data class SliderMapping(
        val loincCode: String,
        val display: String,
        val unit: String,
        val ucumCode: String
    )
}