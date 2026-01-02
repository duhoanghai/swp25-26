package de.schlaftagebuch.repository_database;

import de.schlaftagebuch.model.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Benutzerkonten.
 * Bietet Standard-CRUD-Operationen und Suche nach Benutzername.
 */

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByUsername(String username);
    Optional<UserAccountEntity> findBySubjectRef(String subjectRef);
    boolean existsByUsername(String username);
}
