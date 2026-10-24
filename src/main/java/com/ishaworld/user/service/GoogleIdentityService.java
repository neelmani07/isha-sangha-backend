package com.ishaworld.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.UnauthorizedException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Verifies Google Identity Services ID tokens (signature, expiry, issuer and audience).
 */
@Service
public class GoogleIdentityService {

    public record GoogleProfile(String subject, String email, String name, String pictureUrl) {
    }

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityService(@Value("${app.auth.google.client-id:}") String clientId) {
        this.verifier = clientId.isBlank()
                ? null
                : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(List.of(clientId))
                        .build();
    }

    public GoogleProfile verify(String credential) {
        if (verifier == null) {
            throw new BusinessException("Google sign-in is not configured");
        }

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(credential);
        } catch (Exception ex) {
            throw new UnauthorizedException("Could not verify Google sign-in");
        }

        if (idToken == null) {
            throw new UnauthorizedException("Invalid Google sign-in");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new UnauthorizedException("Google account email is not verified");
        }

        return new GoogleProfile(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"),
                (String) payload.get("picture")
        );
    }
}
