package com.ishaworld.config;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outside dev (app.env != dev), refuses to start with development shortcuts still in place:
 * the sample JWT secret, the sample admin password, sample data, a localhost frontend, or no email.
 */
@Component
public class ProductionSafetyCheck {

    private static final Logger log = LoggerFactory.getLogger(ProductionSafetyCheck.class);

    private final String env;
    private final String jwtSecret;
    private final String adminPassword;
    private final boolean seedSampleData;
    private final String frontendOrigin;
    private final String mailHost;
    private final String storageType;

    public ProductionSafetyCheck(
            @Value("${app.env:dev}") String env,
            @Value("${app.jwt.secret:}") String jwtSecret,
            @Value("${app.admin.default-password:}") String adminPassword,
            @Value("${app.dev.seed-sample-data:false}") boolean seedSampleData,
            @Value("${app.frontend-origin:}") String frontendOrigin,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.storage.type:local}") String storageType
    ) {
        this.env = env;
        this.jwtSecret = jwtSecret;
        this.adminPassword = adminPassword;
        this.seedSampleData = seedSampleData;
        this.frontendOrigin = frontendOrigin;
        this.mailHost = mailHost;
        this.storageType = storageType;
    }

    @EventListener(ApplicationStartedEvent.class)
    void check() {
        if ("dev".equalsIgnoreCase(env)) {
            return;
        }
        List<String> problems = new ArrayList<>();
        if (jwtSecret.length() < 32 || jwtSecret.startsWith("change-me")) {
            problems.add("JWT_SECRET must be a random value of at least 32 characters");
        }
        if ("admin123".equals(adminPassword) || (!adminPassword.isBlank() && adminPassword.length() < 12)) {
            problems.add("ADMIN_DEFAULT_PASSWORD must be blank or at least 12 characters (and not the sample one)");
        }
        if (seedSampleData) {
            problems.add("SEED_SAMPLE_DATA must be off");
        }
        if (frontendOrigin.isBlank() || frontendOrigin.contains("localhost")) {
            problems.add("FRONTEND_ORIGIN must be the public site address, e.g. https://ishasangha.org");
        }
        if (mailHost.isBlank()) {
            problems.add("MAIL_HOST (SMTP) is needed so members receive sign-in codes");
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Not safe to run in '" + env + "':\n - " + String.join("\n - ", problems));
        }
        if ("local".equalsIgnoreCase(storageType)) {
            log.warn("Uploads are stored on local disk. Make sure it is a persistent disk, or set STORAGE_TYPE=s3.");
        }
    }
}
