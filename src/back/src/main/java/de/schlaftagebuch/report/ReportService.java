package de.schlaftagebuch.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.report.dto.SleepReportResponse;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.protocol.repository.ProtocolSubmissionRepository;
import de.schlaftagebuch.fhir.FhirReportService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.auth.dto.PatientEntity;

/**
 * Baut den Endbericht (JSON-DTO), der anschließend auch fürs PDF gerendert wird.
 *
 * Standardverhalten:
 * - pro Kalendertag nur die neueste Submission (übersichtlicher)
 *
 * Testmodus:
 * - allSubmissions=true -> ALLE Submissions im Zeitraum werden übernommen
 *   (auch mehrere am selben Tag). Damit kann man ohne 14 Tage warten testen.
 */
@Service
public class ReportService {

    private final ProtocolSubmissionRepository submissionRepo;
    private final FhirReportService fhirReportService;
    private final PatientRepository patientRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportService(
            ProtocolSubmissionRepository submissionRepo,
            FhirReportService fhirReportService,
            PatientRepository patientRepo
    ) {
        this.submissionRepo = submissionRepo;
        this.fhirReportService = fhirReportService;
        this.patientRepo = patientRepo;
    }

    private String extractPatientId(String subjectRef) {
        if (subjectRef == null) return null;
        if (subjectRef.startsWith("Patient/")) {
            return subjectRef.substring("Patient/".length());
        }
        return subjectRef;
    }
    private String buildPatientDisplay(PatientEntity p, String fallback) {
        String given = p.getGivenName();
        String family = p.getFamilyName();
        if (family != null && !family.isBlank() && given != null && !given.isBlank()) return family + " " + given;
        if (given != null && !given.isBlank()) return given;
        if (family != null && !family.isBlank()) return family;
        return fallback;
    }
    private String resolvePatientLabel(String subjectRef) {
        if (subjectRef == null || subjectRef.isBlank()) return "-";

        // 1) subjectRef = "Patient/<uuid>" → suche mit <uuid>
        String id = extractPatientId(subjectRef);
        if (id != null && !id.isBlank()) {
            Optional<PatientEntity> p1 = patientRepo.findByPatientId(id);
            if (p1.isPresent()) return buildPatientDisplay(p1.get(), subjectRef);
        }

        // 2) patient_id als "Patient/<uuid>"
        Optional<PatientEntity> p2 = patientRepo.findByPatientId(subjectRef);
        if (p2.isPresent()) return buildPatientDisplay(p2.get(), subjectRef);

        // 3) Fallback
        return subjectRef;
    }

    @Transactional
    public SleepReportResponse createLastDaysReport(String subjectRef, int days) throws Exception {
        return createLastDaysReport(subjectRef, days, false);
    }

    @Transactional
    public SleepReportResponse createLastDaysReport(String subjectRef, int days, boolean allSubmissions) throws Exception {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1L);

        return createReportBetweenInternal(subjectRef, from, to, days, "LAST_DAYS", allSubmissions);
    }

    @Transactional
    public SleepReportResponse createReportBetween(String subjectRef, LocalDate from, LocalDate to) throws Exception {
        return createReportBetween(subjectRef, from, to, false);
    }

    @Transactional
    public SleepReportResponse createReportBetween(String subjectRef, LocalDate from, LocalDate to, boolean allSubmissions) throws Exception {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }

        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        int expectedDays = (int) (to.toEpochDay() - from.toEpochDay() + 1);

        return createReportBetweenInternal(subjectRef, from, to, expectedDays, "RANGE", allSubmissions);
    }

    private SleepReportResponse createReportBetweenInternal(
            String subjectRef,
            LocalDate from,
            LocalDate to,
            int expectedDays,
            String reportType,
            boolean allSubmissions
    ) throws Exception {

        Instant fromInstant =
                from.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Instant toInstant =
                to.plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

        List<ProtocolSubmissionEntity> submissions =
                submissionRepo
                        .findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                                subjectRef,
                                fromInstant,
                                toInstant
                        );

        List<SleepReportResponse.DayRow> rows = new ArrayList<>();

        if (allSubmissions) {
            for (ProtocolSubmissionEntity e : submissions) {
                rows.add(mapToDayRow(e));
            }
        } else {
            Map<LocalDate, ProtocolSubmissionEntity> latestPerDay = new LinkedHashMap<>();
            for (ProtocolSubmissionEntity e : submissions) {
                LocalDate d = e.getFilledAt().atZone(ZoneId.systemDefault()).toLocalDate();
                latestPerDay.put(d, e);
            }
            for (ProtocolSubmissionEntity e : latestPerDay.values()) {
                rows.add(mapToDayRow(e));
            }
        }

        SleepReportResponse response = new SleepReportResponse();
        response.subjectRef = subjectRef;
        response.from = from;
        response.to = to;
        response.expectedDays = expectedDays;
        response.daysAvailable = rows.size();
        response.days = rows;
        response.summary = buildSummary(rows);
        response.patientName = resolvePatientLabel(subjectRef);

        fhirReportService.createAndStoreDiagnosticReport(response, reportType);
        return response;
    }

    @Deprecated
    public SleepReportResponse.DayRow createDayReportRow(String subjectRef, LocalDate date) throws Exception {
        Instant start =
                date.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Instant end =
                date.plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

        List<ProtocolSubmissionEntity> submissions =
                submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                        subjectRef, start, end
                );

        if (submissions.isEmpty()) {
            return null;
        }

        ProtocolSubmissionEntity latest = submissions.get(submissions.size() - 1);
        return mapToDayRow(latest);
    }

    // ----------------------------
    // Mapping + Summary
    // ----------------------------

    private SleepReportResponse.DayRow mapToDayRow(ProtocolSubmissionEntity e) throws Exception {
        SleepReportResponse.DayRow d = new SleepReportResponse.DayRow();

        LocalDate day = e.getFilledAt().atZone(ZoneId.systemDefault()).toLocalDate();
        d.date = day;

        // FIX: parseAnswers kann jetzt Array ODER Map lesen
        Map<Integer, Object> a = parseAnswers(e.getAnswersJson());

        // Zuordnung der Fragen (IDs aus JSON-Template)
        d.wellbeing = getInt(a, 0);               // "3.0" -> 3
        d.dreamed = getBool(a, 1);                // "Ja"/"Nein" -> true/false
        d.sleepTime = getString(a, 2);            // "01:35"
        d.wakeTime = getString(a, 3);             // "06:00"
        d.sleepQuality = getInt(a, 4);            // "3.0" -> 3
        d.morningFeeling = getInt(a, 5);          // "2.0" -> 2

        d.nap = getBool(a, 6);
        d.napFrequency = getInt(a, 7);

        // FIX: Dauer "HH:mm" -> Minuten
        d.napMinutes = getDurationMinutes(a, 8);

        d.preSleepActivities = a.get(9);

        d.nightWoke = getBool(a, 10);
        d.nightWakeCount = getInt(a, 11);

        // FIX: Dauer "HH:mm" -> Minuten
        d.nightWakeMinutes = getDurationMinutes(a, 12);

        d.medication = getBool(a, 13);
        d.medicationFreq = getString(a, 14);
        d.caffeine = getBool(a, 15);

        // FIX: Dauer "HH:mm" -> Minuten
        d.screenMinutes = getDurationMinutes(a, 16);

        // computedSleepMinutes (aus sleepTime + wakeTime)
        d.computedSleepMinutes = computeSleepMinutes(d.sleepTime, d.wakeTime);

        return d;
    }

    private SleepReportResponse.Summary buildSummary(List<SleepReportResponse.DayRow> rows) {
        SleepReportResponse.Summary s = new SleepReportResponse.Summary();

        if (rows == null || rows.isEmpty()) {
            return s; // alles bleibt null -> PDF zeigt "-"
        }

        int sumSleepMin = 0, countSleepMin = 0;
        int sumSleepTimeMin = 0, countSleepTimeMin = 0;
        int sumWakeTimeMin = 0, countWakeTimeMin = 0;
        int sumQuality = 0, countQuality = 0;

        int sumMorning = 0, countMorning = 0;

        int sumNightWakeCount = 0, countNightWakeCount = 0;
        int sumNightWakeMinutes = 0, countNightWakeMinutes = 0;

        int sumScreenMinutes = 0, countScreenMinutes = 0;

        int dreamYesDays = 0;
        int medicationYesDays = 0;
        int caffeineYesDays = 0;

        for (SleepReportResponse.DayRow r : rows) {
            if (r == null) continue;

            // Schlafdauer
            if (r.computedSleepMinutes != null) {
                sumSleepMin += r.computedSleepMinutes;
                countSleepMin++;
            }

            // Einschlaf-/Aufwachzeit (Uhrzeit als Minuten seit 00:00)
            Integer sleepClockMin = parseClockMinutes(r.sleepTime);
            if (sleepClockMin != null) {
                sumSleepTimeMin += sleepClockMin;
                countSleepTimeMin++;
            }
            Integer wakeClockMin = parseClockMinutes(r.wakeTime);
            if (wakeClockMin != null) {
                sumWakeTimeMin += wakeClockMin;
                countWakeTimeMin++;
            }

            // Schlafqualität
            if (r.sleepQuality != null) {
                sumQuality += r.sleepQuality;
                countQuality++;
            }

            // Morgengefühl
            if (r.morningFeeling != null) {
                sumMorning += r.morningFeeling;
                countMorning++;
            }

            // nächtliches Aufwachen (Anzahl)
            if (r.nightWakeCount != null) {
                sumNightWakeCount += r.nightWakeCount;
                countNightWakeCount++;
            }

            // Wach-Minuten nachts
            if (r.nightWakeMinutes != null) {
                sumNightWakeMinutes += r.nightWakeMinutes;
                countNightWakeMinutes++;
            }

            // Bildschirmzeit
            if (r.screenMinutes != null) {
                sumScreenMinutes += r.screenMinutes;
                countScreenMinutes++;
            }

            // Ja/Nein Tage zählen
            if (Boolean.TRUE.equals(r.dreamed)) dreamYesDays++;
            if (Boolean.TRUE.equals(r.medication)) medicationYesDays++;
            if (Boolean.TRUE.equals(r.caffeine)) caffeineYesDays++;
        }

        // Averages
        s.avgSleepMinutes = (countSleepMin == 0) ? null : (sumSleepMin * 1.0) / countSleepMin;
        s.avgSleepTimeMinutes = (countSleepTimeMin == 0) ? null : (sumSleepTimeMin * 1.0) / countSleepTimeMin;
        s.avgWakeTimeMinutes = (countWakeTimeMin == 0) ? null : (sumWakeTimeMin * 1.0) / countWakeTimeMin;
        s.avgSleepQuality = (countQuality == 0) ? null : (sumQuality * 1.0) / countQuality;

        s.avgMorningFeeling = (countMorning == 0) ? null : (sumMorning * 1.0) / countMorning;
        s.avgNightWakeCount = (countNightWakeCount == 0) ? null : (sumNightWakeCount * 1.0) / countNightWakeCount;
        s.avgNightWakeMinutes = (countNightWakeMinutes == 0) ? null : (sumNightWakeMinutes * 1.0) / countNightWakeMinutes;
        s.avgScreenMinutes = (countScreenMinutes == 0) ? null : (sumScreenMinutes * 1.0) / countScreenMinutes;

        // Counts
        s.dreamYesDays = dreamYesDays;
        s.medicationYesDays = medicationYesDays;
        s.caffeineYesDays = caffeineYesDays;

        return s;
    }

    private Integer parseClockMinutes(String time) {
        if (time == null || time.isBlank()) return null;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            LocalTime t = LocalTime.parse(time.trim(), fmt);
            return t.getHour() * 60 + t.getMinute();
        } catch (Exception ex) {
            return null;
        }
    }


    private Integer computeSleepMinutes(String sleepTime, String wakeTime) {
        if (sleepTime == null || wakeTime == null) return null;

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            LocalTime s = LocalTime.parse(sleepTime.trim(), fmt);
            LocalTime w = LocalTime.parse(wakeTime.trim(), fmt);

            int minutes = (int) Duration.between(s, w).toMinutes();
            if (minutes < 0) minutes += 24 * 60;

            return minutes;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * FIX: kann beide Formate lesen:
     * 1) Array: [{"id":0,"result":"3.0"}, ...]
     * 2) Objekt: {"0":"3.0","1":"Nein", ...} (falls ihr das irgendwann nutzt)
     */
    private Map<Integer, Object> parseAnswers(String json) {
        Map<Integer, Object> out = new HashMap<>();
        if (json == null || json.isBlank()) return out;

        try {
            JsonNode root = mapper.readTree(json);

            // Format A: Array [{id,result}, ...]
            if (root.isArray()) {
                for (JsonNode item : root) {
                    JsonNode idNode = item.get("id");
                    if (idNode == null || idNode.isNull()) continue;

                    int id = idNode.asInt();
                    JsonNode resNode = item.get("result");

                    Object val = null;
                    if (resNode != null && !resNode.isNull()) {
                        if (resNode.isTextual()) val = resNode.asText();
                        else val = mapper.convertValue(resNode, Object.class);
                    }

                    out.put(id, val);
                }
                return out;
            }

            // Format B: Objekt {"0": "...", ...}
            if (root.isObject()) {
                Map<String, Object> raw = mapper.convertValue(root, new TypeReference<Map<String, Object>>() {});
                for (Map.Entry<String, Object> e : raw.entrySet()) {
                    try {
                        out.put(Integer.parseInt(e.getKey()), e.getValue());
                    } catch (NumberFormatException ignore) { }
                }
                return out;
            }

            return out;
        } catch (Exception ex) {
            // Wichtig: NIEMALS Report/PDF crashen lassen
            return out;
        }
    }


    /**
     * FIX: akzeptiert jetzt auch "3.0" usw.
     */
    private static Integer getInt(Map<Integer, Object> m, int k) {
        Object v = m.get(k);
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        if (v == null) return null;

        String s = v.toString().trim();
        if (s.isBlank()) return null;

        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
        }

        // z.B. "3.0"
        try {
            Double d = Double.parseDouble(s.replace(',', '.'));
            return d.intValue();
        } catch (Exception ignore) {
        }

        return null;
    }

    /**
     * FIX: versteht "Ja"/"Nein" (DE), "yes"/"no", "true"/"false", "1"/"0"
     */
    private static Boolean getBool(Map<Integer, Object> m, int k) {
        Object v = m.get(k);
        if (v instanceof Boolean b) return b;
        if (v == null) return null;

        String s = v.toString().trim().toLowerCase(Locale.ROOT);

        if (s.equals("ja") || s.equals("j") || s.equals("yes") || s.equals("y") || s.equals("true") || s.equals("1")) return true;
        if (s.equals("nein") || s.equals("n") || s.equals("no") || s.equals("false") || s.equals("0")) return false;

        // fallback: wie Java
        return Boolean.parseBoolean(s);
    }

    private static String getString(Map<Integer, Object> m, int k) {
        Object v = m.get(k);
        return v == null ? null : v.toString();
    }

    /**
     * NEU: "HH:mm" -> Minuten (z.B. "01:00" -> 60, "00:20" -> 20)
     */
    private static Integer getDurationMinutes(Map<Integer, Object> m, int k) {
        String s = getString(m, k);
        if (s == null) return null;

        s = s.trim();
        if (s.isBlank()) return null;

        // Falls jemand doch "60" sendet:
        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
        }

        // "HH:mm"
        try {
            String[] p = s.split(":");
            if (p.length != 2) return null;
            int h = Integer.parseInt(p[0]);
            int min = Integer.parseInt(p[1]);
            return h * 60 + min;
        } catch (Exception ex) {
            return null;
        }
    }
}
