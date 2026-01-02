package de.schlaftagebuch.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import de.schlaftagebuch.dto.report.SleepReportResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Erzeugt ein PDF mit:
 *  - Titel & Metadaten
 *  - 14-Tage Zusammenfassung
 *  - Diagrammen (Schlafdauer, Schlafqualität)
 */
@Service
public class PdfReportService {

    public byte[] createSummaryPdf(SleepReportResponse report) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);

        doc.open();

        addTitle(doc, report);
        addSummary(doc, report);
        addSleepDurationChart(doc, report);
        addSleepQualityChart(doc, report);

        doc.close();
        return out.toByteArray();
    }

    // ---------- TITLE ----------

    private void addTitle(Document doc, SleepReportResponse report) throws Exception {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("14-Tage Schlafprotokoll – Zusammenfassung", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(new Paragraph("Patient: " + report.subjectRef));
        doc.add(new Paragraph("Zeitraum: " + report.from + " bis " + report.to));
        doc.add(Chunk.NEWLINE);
    }

    // ---------- SUMMARY TEXT ----------

    private void addSummary(Document doc, SleepReportResponse r) throws Exception {
        doc.add(new Paragraph("Zusammenfassung", new Font(Font.HELVETICA, 14, Font.BOLD)));
        doc.add(Chunk.NEWLINE);

        Font f = new Font(Font.HELVETICA, 11);

        doc.add(new Paragraph("• Durchschnittliche Schlafdauer: " + round(r.summary.avgSleepMinutes) + " Minuten", f));
        doc.add(new Paragraph("• Durchschnittliche Schlafqualität: " + round(r.summary.avgSleepQuality) + " / 5", f));
        doc.add(new Paragraph("• Gefühl am Morgen: " + round(r.summary.avgMorningFeeling) + " / 5", f));
        doc.add(new Paragraph("• Nächtliches Aufwachen (Ø): " + round(r.summary.avgNightWakeCount) + " mal", f));
        doc.add(new Paragraph("• Bildschirmzeit vor dem Schlafen (Ø): " + round(r.summary.avgScreenMinutes) + " Minuten", f));
        doc.add(new Paragraph("• Träume erinnert an: " + r.summary.dreamYesDays + " Tagen", f));
        doc.add(new Paragraph("• Medikamente eingenommen an: " + r.summary.medicationYesDays + " Tagen", f));
        doc.add(new Paragraph("• Koffein/Energy konsumiert an: " + r.summary.caffeineYesDays + " Tagen", f));

        doc.add(Chunk.NEWLINE);
    }

    // ---------- CHART 1: Schlafdauer ----------

    private void addSleepDurationChart(Document doc, SleepReportResponse report) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM");

        report.days.forEach(d -> {
            if (d.computedSleepMinutes != null) {
                dataset.addValue(d.computedSleepMinutes, "Schlafdauer (Min)", d.date.format(df));
            }
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "Schlafdauer pro Tag",
                "Datum",
                "Minuten",
                dataset
        );

        addChartToPdf(doc, chart);
    }

    // ---------- CHART 2: Schlafqualität ----------

    private void addSleepQualityChart(Document doc, SleepReportResponse report) throws Exception {
        XYSeries series = new XYSeries("Schlafqualität");

        int day = 1;
        for (var d : report.days) {
            if (d.sleepQuality != null) {
                series.add(day, d.sleepQuality);
            }
            day++;
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Schlafqualität (1–5)",
                "Tag",
                "Qualität",
                dataset
        );

        addChartToPdf(doc, chart);
    }

    // ---------- HELPER ----------

    private void addChartToPdf(Document doc, JFreeChart chart) throws Exception {
        BufferedImage img = chart.createBufferedImage(500, 300);
        Image image = Image.getInstance(img, null);
        image.setAlignment(Image.ALIGN_CENTER);
        doc.add(image);
        doc.add(Chunk.NEWLINE);
    }

    private String round(Double v) {
        if (v == null) return "-";
        return String.format("%.1f", v);
    }
}
