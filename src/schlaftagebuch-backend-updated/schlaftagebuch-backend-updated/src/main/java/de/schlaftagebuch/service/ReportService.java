package de.schlaftagebuch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.dto.report.SleepReportResponse;
import de.schlaftagebuch.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.repository_database.ProtocolSubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Geschäftslogik für "BerichtErstellen".
 * Liest ProtocolSubmissions aus DB, parsed answersJson und berechnet Kennzahlen.
 */
@Service
public class ReportService {

    private final ProtocolSubmissionRepository submissionRepo;
    private final ObjectMapper om;

    // Antworten Q2/Q3 sind Uhrzeiten im Format "HH:mm"
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public ReportService(ProtocolSubmissionRepository submissionRepo) {
        this.submissionRepo = submissionRepo;
        this.om = new ObjectMapper();
    }

    /**
     * Erstellt einen Bericht für ein SubjectRef (z.B. "Patient/{uuid}") für letzte N Tage.
     * @param subjectRef "Patient/{uuid}"
     * @param days Anzahl Tage (z.B. 14)
     */
    public SleepReportResponse createLastDaysReport(String subjectRef, int days) throws Exception {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");

        // Zeitraum berechnen (z.B. letzte 14 Tage inkl. heute)
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days - 1L);

        Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // bis Ende des Tages
        Instant to = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1);

        List<ProtocolSubmissionEntity> subs =
                submissionRepo
                        .findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                                subjectRef,
                                from,
                                to
                        );


        SleepReportResponse resp = new SleepReportResponse();
        resp.subjectRef = subjectRef;
        resp.from = fromDate;
        resp.to = toDate;
        resp.expectedDays = days;
        resp.daysAvailable = subs.size();

        // Tageszeilen bauen
        List<SleepReportResponse.DayRow> dayRows = new ArrayList<>();
        for (ProtocolSubmissionEntity e : subs) {
            dayRows.add(mapToDayRow(e));
        }
        resp.days = dayRows;

        // Summary berechnen
        resp.summary = buildSummary(dayRows);

        return resp;
    }

    /**
     * Wandelt eine gespeicherte Submission in eine DayRow um.
     * Wichtig: answersJson wird in Map<Integer,Object> geparst.
     */
    private SleepReportResponse.DayRow mapToDayRow(ProtocolSubmissionEntity e) throws Exception {
        SleepReportResponse.DayRow d = new SleepReportResponse.DayRow();

        // Wir nehmen das Datum aus filledAt (Instant) -> LocalDate
        Instant filledAt = e.getFilledAt();
        LocalDate date = filledAt.atZone(ZoneId.systemDefault()).toLocalDate();

        d.date = date;
        d.filledAt = filledAt.toString();

        // answersJson -> Map<Integer,Object>
        Map<Integer, Object> answers = parseAnswersJson(e.getAnswersJson());

        // Felder nach Question-IDs setzen
        d.wellbeing = getInt(answers, 0);
        d.dreamed = getBool(answers, 1);
        d.sleepTime = getString(answers, 2);
        d.wakeTime = getString(answers, 3);
        d.sleepQuality = getInt(answers, 4);
        d.morningFeeling = getInt(answers, 5);

        d.nap = getBool(answers, 6);
        d.napFrequency = getInt(answers, 7);
        d.napMinutes = getInt(answers, 8);

        d.preSleepActivities = answers.get(9);

        d.nightWoke = getBool(answers, 10);
        d.nightWakeCount = getInt(answers, 11);
        d.nightWakeMinutes = getInt(answers, 12);

        d.medication = getBool(answers, 13);
        d.medicationFreq = getString(answers, 14);
        d.caffeine = getBool(answers, 15);
        d.screenMinutes = getInt(answers, 16);

        // Berechnete Schlafdauer
        d.computedSleepMinutes = computeSleepMinutes(d.sleepTime, d.wakeTime);

        return d;
    }

    /**
     * Aggregation über alle Tage.
     */
    private SleepReportResponse.Summary buildSummary(List<SleepReportResponse.DayRow> days) {
        SleepReportResponse.Summary s = new SleepReportResponse.Summary();

        s.avgSleepMinutes = avg(days.stream().map(d -> d.computedSleepMinutes));
        s.avgSleepQuality = avg(days.stream().map(d -> d.sleepQuality));
        s.avgMorningFeeling = avg(days.stream().map(d -> d.morningFeeling));
        s.avgNightWakeMinutes = avg(days.stream().map(d -> d.nightWakeMinutes));
        s.avgNightWakeCount = avg(days.stream().map(d -> d.nightWakeCount));
        s.avgScreenMinutes = avg(days.stream().map(d -> d.screenMinutes));

        s.dreamYesDays = (int) days.stream().filter(d -> Boolean.TRUE.equals(d.dreamed)).count();
        s.medicationYesDays = (int) days.stream().filter(d -> Boolean.TRUE.equals(d.medication)).count();
        s.caffeineYesDays = (int) days.stream().filter(d -> Boolean.TRUE.equals(d.caffeine)).count();

        // Q9 Aktivitäten zählen (kann String oder Liste sein)
        Map<String, Integer> activityCounts = new HashMap<>();
        for (SleepReportResponse.DayRow d : days) {
            Object v = d.preSleepActivities;
            if (v == null) continue;

            if (v instanceof String str) {
                activityCounts.merge(str, 1, Integer::sum);
            } else if (v instanceof Collection<?> col) {
                for (Object o : col) {
                    if (o != null) activityCounts.merge(o.toString(), 1, Integer::sum);
                }
            } else {
                // Fallback: irgendein Typ -> toString
                activityCounts.merge(v.toString(), 1, Integer::sum);
            }
        }
        s.activityCounts = activityCounts;

        return s;
    }

    /**
     * Schlafminuten aus "HH:mm" -> "HH:mm".
     * Wenn wakeTime vor sleepTime liegt: über Mitternacht.
     */
    private Integer computeSleepMinutes(String sleepTime, String wakeTime) {
        if (sleepTime == null || wakeTime == null) return null;
        try {
            LocalTime sleep = LocalTime.parse(sleepTime, HHMM);
            LocalTime wake = LocalTime.parse(wakeTime, HHMM);

            LocalDate base = LocalDate.of(2000, 1, 1);
            LocalDateTime start = LocalDateTime.of(base, sleep);
            LocalDateTime end = LocalDateTime.of(base, wake);

            // Über Mitternacht
            if (end.isBefore(start)) end = end.plusDays(1);

            long minutes = Duration.between(start, end).toMinutes();
            return minutes >= 0 ? (int) minutes : null;
        } catch (Exception ex) {
            // Zeitformat war nicht parsebar
            return null;
        }
    }

    /**
     * answersJson kann Keys als String haben ("2":"23:00").
     * Diese Methode macht robust: Map<String,Object> lesen, dann in Map<Integer,Object> umwandeln.
     */
    private Map<Integer, Object> parseAnswersJson(String answersJson) throws Exception {
        if (answersJson == null || answersJson.isBlank()) return Map.of();

        Map<String, Object> raw = om.readValue(answersJson, new TypeReference<>() {});
        Map<Integer, Object> out = new HashMap<>();

        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            try {
                int key = Integer.parseInt(entry.getKey());
                out.put(key, entry.getValue());
            } catch (NumberFormatException ignored) {
                // wenn ein Key nicht int ist -> ignorieren
            }
        }
        return out;
    }

    /** Durchschnitt aus Stream<Integer> -> Double oder null */
    private static Double avg(java.util.stream.Stream<Integer> values) {
        List<Integer> list = values.filter(Objects::nonNull).toList();
        if (list.isEmpty()) return null;

        double sum = 0;
        for (Integer v : list) sum += v;
        return sum / list.size();
    }

    private static Integer getInt(Map<Integer, Object> a, int id) {
        Object v = a.get(id);
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private static Boolean getBool(Map<Integer, Object> a, int id) {
        Object v = a.get(id);
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    private static String getString(Map<Integer, Object> a, int id) {
        Object v = a.get(id);
        return v == null ? null : v.toString();
    }
}
