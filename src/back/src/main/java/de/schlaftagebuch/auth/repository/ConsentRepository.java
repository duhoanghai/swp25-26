package de.schlaftagebuch.auth.repository;

import de.schlaftagebuch.auth.model.ConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRepository extends JpaRepository<ConsentEntity, UUID> {
    Optional<ConsentEntity> findByConsentId(String consentId);
    Optional<ConsentEntity> findByPatientId(String patientId);
}
