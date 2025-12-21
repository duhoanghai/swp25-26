package de.schlaftagebuch.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import de.schlaftagebuch.fhir.provider.ConsentProvider;
import de.schlaftagebuch.fhir.provider.PatientProvider;
import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguration für den eingebetteten HAPI-FHIR-Server.
 * Registriert FHIR-Ressourcen (Patient, Consent) und stellt sie unter /fhir/* bereit.
 */

@Configuration
public class FhirConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public ServletRegistrationBean<Servlet> fhirServlet(
            FhirContext fhirContext,
            PatientProvider patientProvider,
            ConsentProvider consentProvider
    ) {
        RestfulServer server = new RestfulServer(fhirContext);
        server.setResourceProviders(patientProvider, consentProvider);

        // FHIR endpoint: /fhir/*
        ServletRegistrationBean<Servlet> reg = new ServletRegistrationBean<>(server, "/fhir/*");
        reg.setName("fhirServlet");
        reg.setLoadOnStartup(1);
        return reg;
    }
}
