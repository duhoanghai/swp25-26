package de.schlaftagebuch.protocol.repository;

/**
 * Repository-Schnittstelle zur Verwaltung und
 * Abfrage von Protokollübermittlungen.
 */

import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProtocolSubmissionRepository extends JpaRepository<ProtocolSubmissionEntity, UUID> {

    /**
     * Ermittelt alle Protokollübermittlungen eines Subjekts in einem
     * Zeitintervall, sortiert nach filledAt.
     * Der Methodenname folgt der Namenskonvention von Spring Data JPA
     * und beschreibt den Query deklarativ.
     */
    List<ProtocolSubmissionEntity> findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
            String subjectRef, Instant startInclusive, Instant endExclusive
    );
}
