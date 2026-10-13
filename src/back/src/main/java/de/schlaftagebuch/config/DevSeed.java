package de.schlaftagebuch.config;

import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initialisiert Testdaten für die Entwicklung.
 * Wird einmal beim Start ausgeführt und legt einen Standard-Arzt-Account an.
 */

@Configuration
public class DevSeed {

    @Bean
    CommandLineRunner seedDevUsers(
            UserAccountRepository repo,
            PasswordEncoder encoder
    ) {
        return args -> {

            // ---------- Arzt ----------
            if (!repo.existsByUsername("arzt1")) {
                UserAccountEntity arzt = new UserAccountEntity();
                arzt.setUsername("arzt1");
                arzt.setPasswordHash(encoder.encode("arztpass"));
                arzt.setRole(Role.ARZT);
                arzt.setSubjectRef("Practitioner/dev-arzt1");
                repo.save(arzt);

                System.out.println("Seeded ARZT: arzt1 / arztpass");
            }
        };
    }

    @Bean
    CommandLineRunner seedPatient(UserAccountRepository repo, PasswordEncoder encoder) {
        return args -> {
            String username = "patient_test";
            if (repo.existsByUsername(username)) return;

            UserAccountEntity p = new UserAccountEntity();
            p.setUsername(username);
            p.setPasswordHash(encoder.encode("patientpass"));
            p.setRole(Role.PATIENT);
            p.setSubjectRef("Patient/dev-patient-test"); // Patient/...
            repo.save(p);

            System.out.println("Seeded PATIENT account: username=patient_test password=patientpass");
        };
    }
}
