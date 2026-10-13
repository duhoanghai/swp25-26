package de.schlaftagebuch.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.schlaftagebuch.report.dto.SleepReportResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * Service zur Erzeugung von PDF-Dokumenten für Schlafberichte.
 *
 * Enthält zwei PDF-Typen:
 *
 * 1) Endbericht (z.B. 14 Tage)
 *    - enthält Durchschnittswerte + Tabelle + Charts
 *
 * 2) Tagesbericht (1 Tag)
 *    - NUR Werte als Tabelle (kein Feedback/Fließtext)
 *
 * WICHTIG:
 * - Dieser Service rendert NUR PDF aus Daten.
 */
@Service
public class PdfReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM");

    /**
     * Erstellt den PDF-Endbericht (Zusammenfassung über mehrere Tage).
     *
     * PDF enthält:
     * - Header (Patient, Zeitraum)
     * - Durchschnittswerte (Tabelle)
     * - Tabelle mit Tagesdaten
     * - Diagramme (Trends)
     */
    public byte[] createSummaryPdf(SleepReportResponse report) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);

        doc.open();

        addReportHeader(doc, report);
        addAveragesSection(doc, report);
        addDaysTable(doc, report);
        addCharts(doc, report);

        doc.close();
        return out.toByteArray();
    }

    /**
     * Erstellt ein Tages-PDF (genau ein Tag).
     *
     * Enthält nur Werte/Tabellen (kein Feedback/Fließtext).
     */
    public byte[] createDailyPdf(String subjectRef, SleepReportResponse.DayRow day) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);

        doc.open();

        Font h1 = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font text = new Font(Font.HELVETICA, 11);

        Paragraph title = new Paragraph("Schlaftagebuch – Tagesübersicht", h1);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Patient: " + safe(subjectRef), text));
        doc.add(new Paragraph("Datum: " + formatDate(day != null ? day.date : null), text));
        doc.add(Chunk.NEWLINE);

        // Nur Werte (Tabelle) – keine Fließtexte/Feedback.
        doc.add(new Paragraph("Werte", h2));
        doc.add(Chunk.NEWLINE);

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4f);
        t.setSpacingAfter(10f);

        addHeaderCell(t, "Feld");
        addHeaderCell(t, "Wert");

        addBodyCell(t, "Schlafdauer (h)", text);
        addBodyCell(t, fmt1(minToHours(day.computedSleepMinutes)), text);

        addBodyCell(t, "Einschlafzeit", text);
        addBodyCell(t, safe(day.sleepTime), text);

        addBodyCell(t, "Aufwachzeit", text);
        addBodyCell(t, safe(day.wakeTime), text);

        addBodyCell(t, "Schlafqualität (1–5)", text);
        addBodyCell(t, safe(day.sleepQuality), text);

        addBodyCell(t, "Morgengefühl (1–5)", text);
        addBodyCell(t, safe(day.morningFeeling), text);

        addBodyCell(t, "Geträumt", text);
        addBodyCell(t, yesNo(day.dreamed), text);

        addBodyCell(t, "Nickerchen (min)", text);
        addBodyCell(t, safe(day.napMinutes), text);

        addBodyCell(t, "Nachts aufgewacht (x)", text);
        addBodyCell(t, safe(day.nightWakeCount), text);

        addBodyCell(t, "Bildschirmzeit (min)", text);
        addBodyCell(t, safe(day.screenMinutes), text);

        doc.add(t);

        doc.close();
        return out.toByteArray();
    }

    // ---------------------------
    // SUMMARY PDF: Sections
    // ---------------------------

    private void addReportHeader(Document doc, SleepReportResponse report) throws Exception {
        Font h1 = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font text = new Font(Font.HELVETICA, 11);

        Paragraph title = new Paragraph("Schlaftagebuch – Endbericht", h1);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(Chunk.NEWLINE);

        String patientLabel =
                (report.patientName != null && !report.patientName.isBlank())
                        ? report.patientName
                        : safe(report.subjectRef);

        doc.add(new Paragraph("Patient: " + patientLabel, text));
        doc.add(new Paragraph("Zeitraum: " + safe(report.from) + " bis " + safe(report.to), text));
        doc.add(new Paragraph("Erwartete Tage: " + report.expectedDays + " | Gefundene Einträge: " + report.daysAvailable, text));
        doc.add(Chunk.NEWLINE);
    }

    private void addAveragesSection(Document doc, SleepReportResponse report) throws Exception {
        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 11);

        doc.add(new Paragraph("Durchschnittswerte", h2));
        doc.add(Chunk.NEWLINE);

        // Nur Werte/Tabellen – keine Fließtexte.
        SleepReportResponse.Summary s = report.summary != null ? report.summary : new SleepReportResponse.Summary();

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4f);
        t.setSpacingAfter(10f);

        addHeaderCell(t, "Kennzahl");
        addHeaderCell(t, "Wert");

        addBodyCell(t, "Schlafdauer (h)", cellFont);
        addBodyCell(t, fmt1(minToHours(s.avgSleepMinutes)), cellFont);

        // Uhrzeit-Durchschnitte als "HH:mm" ausgeben, damit es direkt lesbar ist.
        addBodyCell(t, "Einschlafzeit", cellFont);
        addBodyCell(t, formatClockMinutes(s.avgSleepTimeMinutes), cellFont);

        addBodyCell(t, "Aufwachzeit", cellFont);
        addBodyCell(t, formatClockMinutes(s.avgWakeTimeMinutes), cellFont);

        addBodyCell(t, "Schlafqualität (1–5)", cellFont);
        addBodyCell(t, fmt1(s.avgSleepQuality), cellFont);

        addBodyCell(t, "Morgengefühl (1–5).", cellFont);
        addBodyCell(t, fmt1(s.avgMorningFeeling), cellFont);

        addBodyCell(t, "nächtl. Aufwachen ", cellFont);
        addBodyCell(t, fmt1(s.avgNightWakeCount), cellFont);

        addBodyCell(t, "Wach-Minuten nachts", cellFont);
        addBodyCell(t, fmt1(s.avgNightWakeMinutes), cellFont);

        addBodyCell(t, "Bildschirmzeit (min)", cellFont);
        addBodyCell(t, fmt1(s.avgScreenMinutes), cellFont);

        addBodyCell(t, "Geträumt (Tage)", cellFont);
        addBodyCell(t, safe(s.dreamYesDays), cellFont);

        addBodyCell(t, "Medikamente (Tage)", cellFont);
        addBodyCell(t, safe(s.medicationYesDays), cellFont);

        addBodyCell(t, "Koffein (Tage)", cellFont);
        addBodyCell(t, safe(s.caffeineYesDays), cellFont);

        doc.add(t);
        doc.add(Chunk.NEWLINE);
    }

    private void addDaysTable(Document doc, SleepReportResponse report) throws Exception {

        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font text = new Font(Font.HELVETICA, 10);

        doc.add(new Paragraph("Tagesübersicht (Tabelle)", h2));
        doc.add(Chunk.NEWLINE);

        if (report.days == null || report.days.isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(10f);

        addHeaderCell(table, "Datum");
        addHeaderCell(table, "Schlaf (h)");
        addHeaderCell(table, "Qualität");
        addHeaderCell(table, "Morgen");
        addHeaderCell(table, "Aufwachen");
        addHeaderCell(table, "Screen (min)");

        for (SleepReportResponse.DayRow d : report.days) {
            addBodyCell(table, formatDate(d.date), text);
            addBodyCell(table, fmt1(minToHours(d.computedSleepMinutes)), text);
            addBodyCell(table, safe(d.sleepQuality), text);
            addBodyCell(table, safe(d.morningFeeling), text);
            addBodyCell(table, safe(d.nightWakeCount), text);
            addBodyCell(table, safe(d.screenMinutes), text);
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addCharts(Document doc, SleepReportResponse report) throws Exception {

        Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD);
        doc.add(new Paragraph("Diagramme", h2));
        doc.add(Chunk.NEWLINE);

        if (report.days == null || report.days.isEmpty()) {
            return;
        }

        DefaultCategoryDataset sleepMinutes = new DefaultCategoryDataset();
        DefaultCategoryDataset sleepTime = new DefaultCategoryDataset();
        DefaultCategoryDataset wakeTime = new DefaultCategoryDataset();
        DefaultCategoryDataset sleepQuality = new DefaultCategoryDataset();
        DefaultCategoryDataset morningFeeling = new DefaultCategoryDataset();

        // zählt gleiche Datumslabels, damit nichts überschrieben wird
        Map<String, Integer> labelCount = new HashMap<>();

        for (SleepReportResponse.DayRow d : report.days) {
            if (d.date == null) continue;

            String base = d.date.format(DATE_FMT);

            int n = labelCount.getOrDefault(base, 0) + 1;
            labelCount.put(base, n);

            // z.B. 2026-01-14, 2026-01-14 (2), 2026-01-14 (3) ...
            String label = (n == 1) ? base : (base + " (" + n + ")");

            if (d.computedSleepMinutes != null) {
                sleepMinutes.addValue(minToHours(d.computedSleepMinutes), "Schlafdauer", label);
            }
            Integer sleepClockMin = parseClockMinutes(d.sleepTime);
            if (sleepClockMin != null) {
                sleepTime.addValue(minutesToHoursForChart(sleepClockMin), "Einschlafzeit", label);
            }
            Integer wakeClockMin = parseClockMinutes(d.wakeTime);
            if (wakeClockMin != null) {
                wakeTime.addValue(minutesToHoursForChart(wakeClockMin), "Aufwachzeit", label);
            }
            if (d.sleepQuality != null) {
                sleepQuality.addValue(d.sleepQuality, "Schlafqualität", label);
            }
            if (d.morningFeeling != null) {
                morningFeeling.addValue(d.morningFeeling, "Morgengefühl", label);
            }
        }

        JFreeChart sleepChart = ChartFactory.createLineChart(
                "Schlafdauer (Stunden) pro Tag",
                "Datum",
                "Stunden",
                sleepMinutes
        );

        JFreeChart sleepTimeChart = ChartFactory.createLineChart(
                "Einschlafzeit (Uhrzeit) pro Tag",
                "Datum",
                "Uhrzeit (Stunden)",
                sleepTime
        );

        JFreeChart wakeTimeChart = ChartFactory.createLineChart(
                "Aufwachzeit (Uhrzeit) pro Tag",
                "Datum",
                "Uhrzeit (Stunden)",
                wakeTime
        );

        JFreeChart qualityChart = ChartFactory.createLineChart(
                "Schlafqualität (1–5) pro Tag",
                "Datum",
                "Wert",
                sleepQuality
        );

        JFreeChart morningChart = ChartFactory.createLineChart(
                "Gefühl am Morgen (1–5) pro Tag",
                "Datum",
                "Wert",
                morningFeeling
        );

        addChartImage(doc, sleepChart, 520, 260);
        addChartImage(doc, sleepTimeChart, 520, 260);
        addChartImage(doc, wakeTimeChart, 520, 260);
        addChartImage(doc, qualityChart, 520, 260);
        addChartImage(doc, morningChart, 520, 260);

        doc.add(Chunk.NEWLINE);
    }


    // ---------------------------
    // Helpers: PDF Building Blocks
    // ---------------------------

    private void addHeaderCell(PdfPTable table, String text) {
        Font font = new Font(Font.HELVETICA, 10, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addChartImage(Document doc, JFreeChart chart, int width, int height) throws Exception {
        BufferedImage img = chart.createBufferedImage(width, height);
        Image pdfImg = Image.getInstance(img, null);
        pdfImg.setAlignment(Image.ALIGN_CENTER);
        doc.add(pdfImg);
        doc.add(Chunk.NEWLINE);
    }

    // ---------------------------
    // Helpers: Formatting
    // ---------------------------

    private String safe(Object v) {
        return v == null ? "-" : v.toString();
    }

    private String fmt1(Double v) {
        if (v == null) return "-";
        return String.format("%.1f", v);
    }

    private String formatClockMinutes(Double minutes) {
        if (minutes == null) return "-";
        int total = (int) Math.round(minutes);
        total = ((total % (24 * 60)) + (24 * 60)) % (24 * 60);
        int h = total / 60;
        int m = total % 60;
        return String.format("%02d:%02d", h, m);
    }

    private String yesNo(Boolean v) {
        if (v == null) return "-";
        return v ? "Ja" : "Nein";
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

    private Double minutesToHoursForChart(Integer minutes) {
        if (minutes == null) return null;
        return minutes / 60.0;
    }

    private Double minToHours(Number minutes) {
        if (minutes == null) return null;
        return minutes.doubleValue() / 60.0;
    }

    private String formatDate(LocalDate d) {
        if (d == null) return "-";
        return d.format(DATE_FMT);
    }
}
