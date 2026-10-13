package de.schlaftagebuch.arztManagement.repository;

import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Einmal-Codes.
 * Wird genutzt, um Codes beim ersten Patienten-Login zu prüfen.
 */

public interface OneTimeCodeRepository extends JpaRepository<OneTimeCodeEntity, UUID> {
    Optional<OneTimeCodeEntity> findByCode(String code);

    List<OneTimeCodeEntity> findAllByPatientRefOrderByCreatedAtDesc(String patientRef);
    List<OneTimeCodeEntity> findAllByOrderByCreatedAtDesc();
}
