package de.schlaftagebuch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Spring-Boot-Anwendung.
 * Startet den eingebetteten Webserver und initialisiert alle Konfigurationen.
 */

@SpringBootApplication
public class SchlaftagebuchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchlaftagebuchBackendApplication.class, args);
	}

}
