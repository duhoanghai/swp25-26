package de.schlaftagebuch.auth.repository;

import de.schlaftagebuch.auth.dto.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    Optional<PatientEntity> findByPatientId(String patientId);

    Optional<PatientEntity> findByUserAccountId(UUID userAccountId);

    List<PatientEntity> findAllByFamilyNameContainingIgnoreCase(String familyName);

    List<PatientEntity> findAllByGivenNameContainingIgnoreCase(String givenName);

    @Query("SELECT p FROM PatientEntity p WHERE LOWER(CONCAT(p.givenName, ' ', p.familyName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PatientEntity> searchByName(String name);

    boolean existsByPatientId(String patientId);

    long count();
}