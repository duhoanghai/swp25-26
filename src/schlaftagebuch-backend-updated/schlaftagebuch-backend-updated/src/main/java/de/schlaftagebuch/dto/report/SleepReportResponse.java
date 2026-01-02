package de.schlaftagebuch.dto.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO für den 14-Tage Bericht.
 * Wird vom Frontend (Arzt-Webseite) genutzt, um Tabelle/Charts darzustellen.
 */
public class SleepReportResponse {

    public String subjectRef;         // z.B. "Patient/{uuid}"
    public LocalDate from;            // Startdatum (inkl.)
    public LocalDate to;              // Enddatum (inkl.)
    public int daysAvailable;         // wie viele Einträge wurden gefunden
    public int expectedDays;          // normalerweise 14

    public Summary summary;
    public List<DayRow> days;

    /** Aggregierte Kennzahlen über alle Tage */
    public static class Summary {
        public Double avgSleepMinutes;
        public Double avgSleepQuality;          // Q4 (1..5)
        public Double avgMorningFeeling;        // Q5 (1..5)
        public Double avgNightWakeMinutes;      // Q12
        public Double avgNightWakeCount;        // Q11
        public Double avgScreenMinutes;         // Q16

        public int dreamYesDays;                // Q1==true
        public int medicationYesDays;           // Q13==true
        public int caffeineYesDays;             // Q15==true

        public Map<String, Integer> activityCounts; // Q9
    }

    /** Tageszeile: Werte pro Tag + berechnete Schlafminuten */
    public static class DayRow {
        public LocalDate date;
        public String filledAt;                 // ISO String (optional fürs UI)

        // Fragen (IDs aus eurer JSON Template)
        public Integer wellbeing;               // Q0
        public Boolean dreamed;                 // Q1
        public String sleepTime;                // Q2 "HH:mm"
        public String wakeTime;                 // Q3 "HH:mm"
        public Integer sleepQuality;            // Q4
        public Integer morningFeeling;          // Q5

        public Boolean nap;                     // Q6
        public Integer napFrequency;            // Q7
        public Integer napMinutes;              // Q8

        public Object preSleepActivities;       // Q9: String oder List<String>

        public Boolean nightWoke;               // Q10
        public Integer nightWakeCount;          // Q11
        public Integer nightWakeMinutes;        // Q12

        public Boolean medication;              // Q13
        public String medicationFreq;           // Q14
        public Boolean caffeine;                // Q15
        public Integer screenMinutes;           // Q16

        // Berechnet
        public Integer computedSleepMinutes;
    }
}
