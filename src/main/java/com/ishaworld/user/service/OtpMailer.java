package com.ishaworld.user.service;

import com.ishaworld.exception.BusinessException;
import com.ishaworld.site.SiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends sign-in codes by email. Without SMTP configured (no spring.mail.host), dev mode logs the code
 * to the console instead so login can be tested locally; any other environment refuses.
 */
@Component
public class OtpMailer {

    private static final Logger log = LoggerFactory.getLogger(OtpMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final SiteProperties siteProperties;
    private final String fromAddress;
    private final boolean devMode;
    private final boolean mailConfigured;

    public OtpMailer(
            ObjectProvider<JavaMailSender> mailSender,
            SiteProperties siteProperties,
            @Value("${app.auth.otp.from:${app.site.contact-email:}}") String fromAddress,
            @Value("${app.env:dev}") String env,
            @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSender = mailSender;
        this.mailConfigured = !mailHost.isBlank();
        this.siteProperties = siteProperties;
        this.fromAddress = fromAddress;
        this.devMode = "dev".equalsIgnoreCase(env);
    }

    public void send(String email, String code, long ttlMinutes) {
        JavaMailSender sender = mailConfigured ? mailSender.getIfAvailable() : null;

        if (sender == null) {
            if (devMode) {
                log.warn("[DEV] Sign-in code for {} is {} (email sending is not configured)", email, code);
                return;
            }
            throw new BusinessException("Email sign-in is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("Your " + siteProperties.name() + " sign-in code: " + code);
        message.setText("""
                Namaskaram,

                Your sign-in code is %s

                It expires in %d minutes. If you did not request it, you can ignore this email.

                — %s
                """.formatted(code, ttlMinutes, siteProperties.name()));

        try {
            sender.send(message);
        } catch (MailException ex) {
            log.error("Could not send sign-in code to {}", email, ex);
            throw new BusinessException("Could not send the sign-in email. Please try again.");
        }
    }
}
