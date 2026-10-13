package de.schlaftagebuch.auth;

import de.schlaftagebuch.arztManagement.dto.GenerateCodeRequest;
import de.schlaftagebuch.arztManagement.dto.GenerateCodeResponse;
import de.schlaftagebuch.auth.dto.ArztLoginRequest;
import de.schlaftagebuch.auth.dto.PatientFirstLoginRequest;
import de.schlaftagebuch.auth.dto.PatientLoginRequest;
import de.schlaftagebuch.auth.dto.TokenResponse;
import de.schlaftagebuch.fhir.store.ConsentStore;
import de.schlaftagebuch.fhir.store.PatientStore;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.security.JwtService;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.auth.repository.PatientRepository;

/**
 * Zentrale Geschäftslogik für Anmeldung und Registrierung.
 * Enthält:
 *  - Login des Arztes
 *  - Erzeugung von Einmal-Codes
 *  - erster Login des Patienten (mit Stammdaten und Consent)
 *  - spätere Logins des Patienten
 */

@Service
public class AuthService {

    private final UserAccountRepository userRepo;
    private final OneTimeCodeRepository codeRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final PatientStore patientStore;
    private final ConsentStore consentStore;

    private final SecureRandom random = new SecureRandom();
    // Ohne 0/O/1/I — bequemer
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LOGIN_ID_LENGTH = 8;
    private final PatientRepository patientRepo;

    public AuthService(
            UserAccountRepository userRepo,
            OneTimeCodeRepository codeRepo,
            PasswordEncoder encoder,
            JwtService jwtService,
            PatientStore patientStore,
            ConsentStore consentStore,
            PatientRepository patientRepo
    ) {
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.patientStore = patientStore;
        this.consentStore = consentStore;
        this.patientRepo = patientRepo;
    }

    /**
     * Anmeldung des Arztes mit Benutzername und Passwort.
     * Prüft das Passwort und erzeugt ein JWT mit Rolle ARZT.
     */

    public TokenResponse loginArzt(ArztLoginRequest req) {
        UserAccountEntity acc = userRepo.findByUsername(req.username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/password"));

        if (acc.getRole() != Role.ARZT) {
            throw new IllegalArgumentException("Not an ARZT account");
        }

        if (!encoder.matches(req.password, acc.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/password");
        }

        String token = jwtService.generateToken(acc.getUsername(), acc.getRole(), acc.getSubjectRef());
        return new TokenResponse(token, acc.getRole().name(), acc.getSubjectRef(), acc.getUsername());
    }

    /**
     * Erzeugt einen Einmal-Code für schon existierenden Patienten. (Vlh brauchen nicht)
     */
    public GenerateCodeResponse generateCodeForPatient(String patientId, GenerateCodeRequest req) {
        String patientRef = "Patient/" + patientId;

        patientStore.findById(patientId).orElseThrow(() ->
                new IllegalArgumentException("Patient not found in FHIR store: " + patientRef)
        );

        String code = generateUniqueCode(6);
        Instant now = Instant.now();
        Instant expiresAt = computeExpiresAt(now, req);

        OneTimeCodeEntity e = new OneTimeCodeEntity();
        e.setCode(code);
        e.setPatientRef(patientRef);
        e.setExpiresAt(expiresAt);
        e.setUsedAt(null);
        e.setCreatedAt(now);
        e.setRevokedAt(null);
        if (req != null && req.appointmentDate != null && !req.appointmentDate.isBlank()) {
            e.setAppointmentDate(LocalDate.parse(req.appointmentDate));
        }
        codeRepo.save(e);

        return new GenerateCodeResponse(e.getId(), code, now, expiresAt, patientId, patientRef);

    }

    /**
     * Erzeugt einen leeren FHIR-Patienten und einen Einmal-Code dafür.
     * Wird vom Arzt verwendet, um neue Patienten einzuladen.
     */
    public GenerateCodeResponse generateCodeForNewPatient(GenerateCodeRequest req) {
        String patientId = UUID.randomUUID().toString();
        String patientRef = "Patient/" + patientId;

        Patient patient = new Patient();
        patient.setId(patientId);    // nur UUID
        patientStore.save(patientId, patient);

        String code = generateUniqueCode(6);
        Instant now = Instant.now();
        Instant expiresAt = computeExpiresAt(now, req);

        OneTimeCodeEntity e = new OneTimeCodeEntity();
        e.setCode(code);
        e.setPatientRef(patientRef);
        e.setExpiresAt(expiresAt);
        e.setUsedAt(null);
        e.setCreatedAt(now);
        e.setRevokedAt(null);
        if (req != null && req.appointmentDate != null && !req.appointmentDate.isBlank()) {
            e.setAppointmentDate(LocalDate.parse(req.appointmentDate));
        }
        codeRepo.save(e);

        return new GenerateCodeResponse(e.getId(), code, now, expiresAt, patientId, patientRef);

    }

    /**
     * Erster Login des Patienten mit Einmal-Code.
     * - Prüft Gültigkeit des Codes
     * - schreibt Vorname/Nachname/Geburtsdatum in den FHIR-Patient
     * - legt einen FHIR-Consent an
     * - erzeugt ein UserAccount mit Rolle PATIENT
     * - markiert den Einmal-Code als benutzt
     */

    public TokenResponse patientFirstLogin(PatientFirstLoginRequest req) {
        if (req.consentAccepted == null || !req.consentAccepted) {
            throw new IllegalArgumentException("Consent is required");
        }

        OneTimeCodeEntity code = codeRepo.findByCode(req.code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

        Instant now = Instant.now();
        if (code.isUsed()) throw new IllegalArgumentException("Code already used");
        if (code.isRevoked()) throw new IllegalArgumentException("Code revoked");
        if (code.isExpired(now)) throw new IllegalArgumentException("Code expired");

        String patientRef = code.getPatientRef();      // "Patient/{uuid}"
        String patientId = patientRef.substring("Patient/".length());

        Patient patient = patientStore.findById(patientId).orElseGet(() -> {
            Patient p = new Patient();
            p.setId(patientId);          // WICHTIG: UUID
            return p;
        });

        // Name, Nachname
        patient.getName().clear();
        patient.addName()
                .addGiven(req.givenName)
                .setFamily(req.familyName);

        // Geburtsdatum: ISO-Format YYYY-MM-DD (Pflichtfeld)
        if (req.birthDate == null || req.birthDate.isBlank()) {
            throw new IllegalArgumentException("birthDate is required (YYYY-MM-DD)");
        }

        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(req.birthDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("birthDate must be ISO format YYYY-MM-DD");
        }

        patient.setBirthDate(java.sql.Date.valueOf(birthDate));
        patientStore.save(patientId, patient);

        // FHIR Consent: speichern in ConsentStore
        Consent consent = new Consent();
        consent.setStatus(Consent.ConsentState.ACTIVE);
        consent.setPatient(new Reference(patientRef));

        String consentId = UUID.randomUUID().toString();
        consent.setId("Consent/" + consentId);
        consentStore.save(consentId, consent);

        // Kurzer Username
        String username = generateUniqueLoginId();

        UserAccountEntity acc = new UserAccountEntity();
        acc.setUsername(username);
        acc.setPasswordHash(encoder.encode(req.password));
        acc.setRole(Role.PATIENT);
        acc.setSubjectRef(patientRef);
        userRepo.save(acc);

        // --- Persistente Patient-Stammdaten in SQL speichern (damit Name dauerhaft bleibt) ---
        PatientEntity pe = patientRepo.findByPatientId(patientId).orElseGet(PatientEntity::new);

        pe.setPatientId(patientId);
        pe.setUserAccountId(acc.getId());
        pe.setGivenName(req.givenName);
        pe.setFamilyName(req.familyName);
        pe.setBirthDate(birthDate);

        patientRepo.save(pe);

        // Code wurde genutzt
        code.setUsedAt(now);
        codeRepo.save(code);

        String token = jwtService.generateToken(acc.getUsername(), acc.getRole(), acc.getSubjectRef());
        return new TokenResponse(token, acc.getRole().name(), acc.getSubjectRef(), acc.getUsername());
    }

    /**
     * Späterer Login des Patienten mit eigener Login-ID und Passwort.
     * Prüft die Zugangsdaten und erzeugt ein neues JWT.
     */

    public TokenResponse loginPatient(PatientLoginRequest req) {
        UserAccountEntity acc = userRepo.findByUsername(req.username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/password"));

        if (acc.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("Not a PATIENT account");
        }

        if (!encoder.matches(req.password, acc.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/password");
        }

        String token = jwtService.generateToken(acc.getUsername(), acc.getRole(), acc.getSubjectRef());
        return new TokenResponse(token, acc.getRole().name(), acc.getSubjectRef(), acc.getUsername());
    }

    // Hilfsfunktionen:

    /**
     * Berechnet den Ablaufzeitpunkt.
     * - wenn req.validUntilDate gesetzt ist (YYYY-MM-DD), gilt der Code bis Ende dieses Tages in Europe/Berlin
     * - sonst Default: 24h ab jetzt
     */
    private Instant computeExpiresAt(Instant now, GenerateCodeRequest req) {
        if (req == null || req.validUntilDate == null || req.validUntilDate.isBlank()) {
            return now.plusSeconds(24 * 60 * 60);
        }

        try {
            LocalDate d = LocalDate.parse(req.validUntilDate.trim());
            ZoneId zone = ZoneId.of("Europe/Berlin");
            // Ende des Tages: wir nehmen "letztes Nano" des Tages
            ZonedDateTime zdt = d.plusDays(1).atStartOfDay(zone).minusNanos(1);
            Instant expires = zdt.toInstant();

            // Falls Datum in Vergangenheit: mindestens 1 Minute gültig
            if (expires.isBefore(now.plusSeconds(60))) {
                return now.plusSeconds(60);
            }
            return expires;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("validUntilDate must be ISO format YYYY-MM-DD");
        }
    }

    // Erzeugt einen eindeutigen Einmal-Code aus Großbuchstaben und Ziffern
    private String generateUniqueCode(int length) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String c = randomString(length);
            if (codeRepo.findByCode(c).isEmpty()) return c;
        }
        throw new IllegalStateException("Failed to generate unique code");
    }

    // Erzeugt eine eindeutige Login-ID für Patienten
    private String generateUniqueLoginId() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = randomString(LOGIN_ID_LENGTH);
            if (!userRepo.existsByUsername(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate unique login id");
    }

    // Hilfsmethode für zufällige Strings
    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
