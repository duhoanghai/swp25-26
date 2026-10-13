package de.schlaftagebuch.arztManagement;

import de.schlaftagebuch.arztManagement.dto.CodeInfoResponse;
import de.schlaftagebuch.arztManagement.dto.PatientSummaryResponse;
import de.schlaftagebuch.fhir.store.PatientStore;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArztManagementService {

    private static final Logger log = LoggerFactory.getLogger(ArztManagementService.class);

    private final UserAccountRepository userRepo;
    private final OneTimeCodeRepository codeRepo;
    private final PatientRepository patientRepo;

    public ArztManagementService(
            UserAccountRepository userRepo,
            OneTimeCodeRepository codeRepo,
            PatientStore patientStore, // Kann bleiben, wird aber nicht verwendet
            PatientRepository patientRepo
    ) {
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.patientRepo = patientRepo;
        log.info("ArztManagementService initialisiert");
    }

    public List<PatientSummaryResponse> listPatients() {
        log.info("=== listPatients() aufgerufen ===");

        // 1. Alle PATIENT UserAccounts holen
        List<UserAccountEntity> patientAccounts = userRepo.findAllByRole(Role.PATIENT);
        log.info("Gefundene PATIENT UserAccounts: {}", patientAccounts.size());

        if (patientAccounts.isEmpty()) {
            log.warn("KEINE UserAccounts mit Rolle PATIENT gefunden!");
            // Zeige alle UserAccounts zum Debuggen
            userRepo.findAll().forEach(acc ->
                    log.debug("UserAccount: {} - Rolle: {}", acc.getUsername(), acc.getRole()));
        }

        // 2. Für jeden Account Patientendaten holen
        return patientAccounts.stream()
                .map(account -> {
                    log.debug("Verarbeite Account: {} -> {}",
                            account.getUsername(), account.getSubjectRef());

                    PatientSummaryResponse response = new PatientSummaryResponse();

                    // UserAccount Daten
                    response.setUsername(account.getUsername());
                    response.setSubjectRef(account.getSubjectRef());

                    // Patient ID extrahieren
                    String patientId = extractPatientId(account.getSubjectRef());
                    response.setPatientId(patientId);

                    log.debug("Extrahiert Patient ID: {}", patientId);

                    // Patientendaten aus patient Tabelle
                    if (patientId != null) {
                        patientRepo.findByPatientId(patientId).ifPresentOrElse(
                                patient -> {
                                    response.setGivenName(patient.getGivenName());
                                    response.setFamilyName(patient.getFamilyName());
                                    if (patient.getBirthDate() != null) {
                                        response.setBirthDate(patient.getBirthDate().toString());
                                    }
                                    log.debug("Patient gefunden: {} {}",
                                            patient.getGivenName(), patient.getFamilyName());
                                },
                                () -> log.warn("Patient {} nicht in patient Tabelle gefunden!", patientId)
                        );
                    } else {
                        log.warn("Keine Patient ID aus SubjectRef extrahierbar: {}",
                                account.getSubjectRef());
                    }

                    // Display Name
                    String displayName = buildDisplayName(response);
                    response.setDisplayName(displayName);
                    log.debug("Display Name: {}", displayName);

                    return response;
                })
                .collect(Collectors.toList());
    }


    private String extractPatientId(String subjectRef) {
        if (subjectRef == null) {
            log.warn("SubjectRef ist null");
            return null;
        }
        if (!subjectRef.startsWith("Patient/")) {
            log.warn("SubjectRef hat falsches Format: {}", subjectRef);
            return null;
        }
        return subjectRef.substring("Patient/".length());
    }

    private String buildDisplayName(PatientSummaryResponse response) {
        if (response.getFamilyName() != null && response.getGivenName() != null) {
            return response.getFamilyName() + ", " + response.getGivenName();
        }
        if (response.getGivenName() != null) {
            return response.getGivenName();
        }
        if (response.getFamilyName() != null) {
            return response.getFamilyName();
        }
        return "-";
    }

    public List<CodeInfoResponse> listCodes() {
        Instant now = Instant.now();
        return codeRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(e -> toCodeInfo(e, now))
                .collect(Collectors.toList());
    }

    public CodeInfoResponse deactivateCode(UUID codeId) {
        OneTimeCodeEntity e = codeRepo.findById(codeId)
                .orElseThrow(() -> new IllegalArgumentException("Code not found: " + codeId));

        if (e.getRevokedAt() == null) {
            e.setRevokedAt(Instant.now());
            codeRepo.save(e);
        }

        return toCodeInfo(e, Instant.now());
    }

    private CodeInfoResponse toCodeInfo(OneTimeCodeEntity e, Instant now) {
        CodeInfoResponse r = new CodeInfoResponse();
        r.id = e.getId();
        r.code = e.getCode();
        r.patientRef = e.getPatientRef();
        r.createdAt = e.getCreatedAt();
        r.expiresAt = e.getExpiresAt();
        r.usedAt = e.getUsedAt();
        r.revokedAt = e.getRevokedAt();
        if (e.getAppointmentDate() != null) {
            r.appointmentDate = e.getAppointmentDate().toString();
        }


        // Status bestimmen
        if (e.isRevoked()) {
            r.status = "REVOKED";
        } else if (e.isUsed()) {
            r.status = "USED";
        } else if (e.isExpired(now)) {
            r.status = "EXPIRED";
        } else {
            r.status = "ACTIVE";
        }

        return r;
    }
}