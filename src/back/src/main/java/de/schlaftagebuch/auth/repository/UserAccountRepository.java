package de.schlaftagebuch.auth.repository;

import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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
    long countByRole(Role role);

    List<UserAccountEntity> findAllByRole(Role role);
}



