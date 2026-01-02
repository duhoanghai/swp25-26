package de.schlaftagebuch.repository_database;

import de.schlaftagebuch.model.ProtocolSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProtocolSubmissionRepository extends JpaRepository<ProtocolSubmissionEntity, UUID> {

    List<ProtocolSubmissionEntity> findAllBySubjectRefOrderByFilledAtAsc(String subjectRef);

    Optional<ProtocolSubmissionEntity> findFirstBySubjectRefOrderByFilledAtAsc(String subjectRef);

    long countBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThan(
            String subjectRef, Instant startInclusive, Instant endExclusive
    );

    List<ProtocolSubmissionEntity> findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
            String subjectRef, Instant startInclusive, Instant endExclusive
    );
}
