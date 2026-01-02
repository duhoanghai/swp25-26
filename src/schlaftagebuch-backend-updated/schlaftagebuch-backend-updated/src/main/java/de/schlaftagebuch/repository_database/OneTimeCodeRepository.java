package de.schlaftagebuch.repository_database;

import de.schlaftagebuch.model.OneTimeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Einmal-Codes.
 * Wird genutzt, um Codes beim ersten Patienten-Login zu prüfen.
 */

public interface OneTimeCodeRepository extends JpaRepository<OneTimeCodeEntity, UUID> {
    Optional<OneTimeCodeEntity> findByCode(String code);
}
