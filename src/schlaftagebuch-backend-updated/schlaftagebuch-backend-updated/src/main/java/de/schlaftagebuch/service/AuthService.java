package de.schlaftagebuch.service;

import de.schlaftagebuch.dto.*;
import de.schlaftagebuch.fhir.store.ConsentStore;
import de.schlaftagebuch.fhir.store.PatientStore;
import de.schlaftagebuch.model.OneTimeCodeEntity;
import de.schlaftagebuch.model.Role;
import de.schlaftagebuch.model.UserAccountEntity;
import de.schlaftagebuch.repository_database.OneTimeCodeRepository;
import de.schlaftagebuch.repository_database.UserAccountRepository;
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

    public AuthService(
            UserAccountRepository userRepo,
            OneTimeCodeRepository codeRepo,
            PasswordEncoder encoder,
            JwtService jwtService,
            PatientStore patientStore,
            ConsentStore consentStore
    ) {
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.patientStore = patientStore;
        this.consentStore = consentStore;
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
    public GenerateCodeResponse generateCodeForPatient(String patientId) {
        String patientRef = "Patient/" + patientId;

        patientStore.findById(patientId).orElseThrow(() ->
                new IllegalArgumentException("Patient not found in FHIR store: " + patientRef)
        );

        String code = generateUniqueCode(6);
        Instant expiresAt = Instant.now().plusSeconds(24 * 60 * 60);

        OneTimeCodeEntity e = new OneTimeCodeEntity();
        e.setCode(code);
        e.setPatientRef(patientRef);
        e.setExpiresAt(expiresAt);
        e.setUsedAt(null);
        codeRepo.save(e);

        return new GenerateCodeResponse(code, expiresAt);
    }

    /**
     * Erzeugt einen leeren FHIR-Patienten und einen Einmal-Code dafür.
     * Wird vom Arzt verwendet, um neue Patienten einzuladen.
     */
    public GenerateCodeResponse generateCodeForNewPatient() {
        String patientId = UUID.randomUUID().toString();
        String patientRef = "Patient/" + patientId;

        Patient patient = new Patient();
        patient.setId(patientRef);
        patientStore.save(patientId, patient);

        String code = generateUniqueCode(6);
        Instant expiresAt = Instant.now().plusSeconds(24 * 60 * 60);

        OneTimeCodeEntity e = new OneTimeCodeEntity();
        e.setCode(code);
        e.setPatientRef(patientRef);
        e.setExpiresAt(expiresAt);
        e.setUsedAt(null);
        codeRepo.save(e);

        return new GenerateCodeResponse(code, expiresAt);
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
        if (code.isExpired(now)) throw new IllegalArgumentException("Code expired");

        String patientRef = code.getPatientRef();      // "Patient/{uuid}"
        String patientId = patientRef.substring("Patient/".length());

        Patient patient = patientStore.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientRef));

        // Name, Nachname
        patient.getName().clear();
        patient.addName()
                .addGiven(req.givenName)
                .setFamily(req.familyName);

        // Geburtsdatum: YYYY-MM-DD oder YYYY
        LocalDate birthDate;
        if (req.birthDate != null && !req.birthDate.isBlank()) {
            try {
                birthDate = LocalDate.parse(req.birthDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("birthDate must be ISO format YYYY-MM-DD");
            }
        } else if (req.birthYear != null) {
            birthDate = LocalDate.of(req.birthYear, 1, 1);
        } else {
            throw new IllegalArgumentException("Either birthDate or birthYear is required");
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
