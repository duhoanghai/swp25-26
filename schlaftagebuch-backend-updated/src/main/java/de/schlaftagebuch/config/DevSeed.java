package de.schlaftagebuch.config;

import de.schlaftagebuch.model.Role;
import de.schlaftagebuch.model.UserAccountEntity;
import de.schlaftagebuch.repository_database.UserAccountRepository;
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
    CommandLineRunner seedArzt(UserAccountRepository repo, PasswordEncoder encoder) {
        return args -> {
            String username = "arzt1";
            if (repo.existsByUsername(username)) return;

            UserAccountEntity a = new UserAccountEntity();
            a.setUsername(username);
            a.setPasswordHash(encoder.encode("arztpass")); // Standard-Passwort für die Entwicklung, kann später geändert werden
            a.setRole(Role.ARZT);
            a.setSubjectRef("Practitioner/dev-arzt1");
            repo.save(a);

            System.out.println("Seeded ARZT account: username=arzt1 password=arztpass");
        };
    }
}
