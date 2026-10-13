package de.schlaftagebuch.report.repository;

import de.schlaftagebuch.report.model.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Schlaf-Berichte in PostgreSQL.
 */
@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

    /**
     * Findet alle Berichte für einen bestimmten Patienten.
     */
    List<ReportEntity> findAllBySubjectRefOrderByCreatedAtDesc(String subjectRef);

    /**
     * Findet Berichte für einen Patienten in einem bestimmten Zeitraum.
     */
    List<ReportEntity> findAllBySubjectRefAndFromDateGreaterThanEqualAndToDateLessThanEqualOrderByCreatedAtDesc(
            String subjectRef, LocalDate fromDate, LocalDate toDate
    );

    /**
     * Findet einen spezifischen Bericht nach Typ und Datum.
     */
    Optional<ReportEntity> findFirstBySubjectRefAndReportTypeAndFromDateAndToDateOrderByCreatedAtDesc(
            String subjectRef, String reportType, LocalDate fromDate, LocalDate toDate
    );

    /**
     * Zählt Berichte für einen Patienten.
     */
    long countBySubjectRef(String subjectRef);

    /**
     * zählt Anzahl der zur Verfügung stehenden Berichte
     */
    long countByCreatedAtAfter(Instant createdAt);

    List<ReportEntity> findTop5ByOrderByCreatedAtDesc();

    Optional<ReportEntity> findFirstBySubjectRefOrderByCreatedAtDesc(String subjectRef);

}
