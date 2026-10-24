package com.ishaworld.security;

import com.ishaworld.admin.entity.AdminUser;
import com.ishaworld.exception.UnauthorizedException;
import com.ishaworld.user.entity.AppUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Token payload is {@code subject|role|expiresAt}. Admin tokens use the admin email as subject and an
 * {@code AdminRole} name as role; public user tokens use the app_user id as subject and {@link #USER_ROLE}.
 */
@Service
public class JwtService {

    public static final String USER_ROLE = "USER";

    private final String secret;
    private final long expirySeconds;
    private final long userExpirySeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-hours}") long expiryHours,
            @Value("${app.jwt.user-expiry-days:30}") long userExpiryDays
    ) {
        this.secret = secret;
        this.expirySeconds = expiryHours * 3600;
        this.userExpirySeconds = userExpiryDays * 86400;
    }

    public String generate(AdminUser user) {
        return generate(user.getEmail(), user.getRole().name(), expirySeconds);
    }

    public String generate(AppUser user) {
        return generate(String.valueOf(user.getId()), USER_ROLE, userExpirySeconds);
    }

    public String extractSubject(String token) {
        return parts(token)[0];
    }

    public String extractRole(String token) {
        return parts(token)[1];
    }

    public boolean isValid(String token) {
        String[] tokenParts = token.split("\\.", 2);
        if (tokenParts.length != 2 || !MessageDigest.isEqual(
                sign(tokenParts[0]).getBytes(StandardCharsets.UTF_8),
                tokenParts[1].getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        try {
            long expiresAt = Long.parseLong(parts(token)[2]);
            return Instant.now().getEpochSecond() < expiresAt;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String generate(String subject, String role, long ttlSeconds) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = subject + "|" + role + "|" + expiresAt;
        String body = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + sign(body);
    }

    private String[] parts(String token) {
        try {
            String[] tokenParts = token.split("\\.", 2);
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[0]), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 3);
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid token");
            }
            return parts;
        } catch (RuntimeException ex) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign token", ex);
        }
    }
}
