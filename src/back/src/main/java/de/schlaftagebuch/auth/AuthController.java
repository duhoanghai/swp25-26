package de.schlaftagebuch.auth;

import de.schlaftagebuch.arztManagement.dto.GenerateCodeRequest;
import de.schlaftagebuch.arztManagement.dto.GenerateCodeResponse;
import de.schlaftagebuch.auth.dto.ArztLoginRequest;
import de.schlaftagebuch.auth.dto.PatientFirstLoginRequest;
import de.schlaftagebuch.auth.dto.PatientLoginRequest;
import de.schlaftagebuch.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Endpunkte für Anmeldung und Registrierung von Arzt und Patient.
 * Delegiert die eigentliche Logik an den AuthService.
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    // Anmeldung des Arztes mit Benutzername und Passwort
    @PostMapping("/arzt/login")
    public TokenResponse loginArzt(@RequestBody @Valid ArztLoginRequest req) {
        return auth.loginArzt(req);
    }

    @PostMapping("/patients/{patientId}/generate-code")
    public GenerateCodeResponse generateCodeForExisting(@PathVariable String patientId, @RequestBody(required = false) GenerateCodeRequest req) {
        return auth.generateCodeForPatient(patientId, req);
    }

    // Arzt erzeugt einen Einmal-Code für einen neuen FHIR-Patienten (leerer Entwurf)
    @PostMapping("/patients/generate-code")
    public GenerateCodeResponse generateCodeForNew(@RequestBody(required = false) GenerateCodeRequest req) {
        return auth.generateCodeForNewPatient(req);
    }

    // Erster Login des Patienten mit Einmal-Code und Stammdaten
    @PostMapping("/patient/first-login")
    public TokenResponse firstLogin(@RequestBody @Valid PatientFirstLoginRequest req) {
        return auth.patientFirstLogin(req);
    }

    // Spätere Logins des Patienten mit eigener Login-ID und Passwort
    @PostMapping("/patient/login")
    public TokenResponse loginPatient(@RequestBody @Valid PatientLoginRequest req) {
        return auth.loginPatient(req);
    }
}
