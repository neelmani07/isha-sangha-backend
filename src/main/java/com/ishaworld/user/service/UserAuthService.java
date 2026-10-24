package com.ishaworld.user.service;

import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.exception.UnauthorizedException;
import com.ishaworld.security.JwtService;
import com.ishaworld.user.dto.AuthResponse;
import com.ishaworld.user.dto.OtpSentResponse;
import com.ishaworld.user.dto.UserResponse;
import com.ishaworld.user.entity.AppUser;
import com.ishaworld.user.entity.EmailOtp;
import com.ishaworld.user.repository.AppUserRepository;
import com.ishaworld.user.repository.EmailOtpRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {

    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration OTP_RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final int MAX_OTPS_PER_HOUR = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final AppUserRepository appUserRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final GoogleIdentityService googleIdentityService;
    private final OtpMailer otpMailer;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    public UserAuthService(
            AppUserRepository appUserRepository,
            EmailOtpRepository emailOtpRepository,
            GoogleIdentityService googleIdentityService,
            OtpMailer otpMailer,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.googleIdentityService = googleIdentityService;
        this.otpMailer = otpMailer;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse loginWithGoogle(String credential) {
        GoogleIdentityService.GoogleProfile profile = googleIdentityService.verify(credential);
        String email = normalize(profile.email());

        AppUser user = appUserRepository.findByGoogleSubject(profile.subject())
                .or(() -> appUserRepository.findByEmailIgnoreCase(email))
                .orElseGet(() -> newUser(email));

        user.setGoogleSubject(profile.subject());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(profile.name());
        }
        if (profile.pictureUrl() != null) {
            user.setAvatarUrl(profile.pictureUrl());
        }

        return signIn(user);
    }

    @Transactional
    public OtpSentResponse requestOtp(String rawEmail) {
        String email = normalize(rawEmail);
        Instant now = Instant.now();

        if (emailOtpRepository.countByEmailAndCreatedAtAfter(email, now.minus(Duration.ofHours(1))) >= MAX_OTPS_PER_HOUR) {
            throw new BusinessException("Too many codes requested. Please try again in an hour.");
        }

        emailOtpRepository.findFirstByEmailAndConsumedAtIsNullOrderByCreatedAtDesc(email)
                .filter(previous -> previous.getCreatedAt() != null
                        && previous.getCreatedAt().isAfter(now.minus(OTP_RESEND_COOLDOWN)))
                .ifPresent(previous -> {
                    throw new BusinessException("Please wait a few seconds before requesting another code.");
                });

        String code = "%06d".formatted(random.nextInt(1_000_000));

        EmailOtp otp = new EmailOtp();
        otp.setEmail(email);
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(now.plus(OTP_TTL));
        emailOtpRepository.save(otp);

        otpMailer.send(email, code, OTP_TTL.toMinutes());
        return new OtpSentResponse(email, OTP_TTL.toSeconds());
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResponse verifyOtp(String rawEmail, String code) {
        String email = normalize(rawEmail);
        EmailOtp otp = emailOtpRepository.findFirstByEmailAndConsumedAtIsNullOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new UnauthorizedException("Code is invalid or has expired. Request a new one."));

        Instant now = Instant.now();
        if (otp.getExpiresAt().isBefore(now) || otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new UnauthorizedException("Code is invalid or has expired. Request a new one.");
        }

        otp.setAttempts(otp.getAttempts() + 1);
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            throw new UnauthorizedException("Incorrect code. Please check and try again.");
        }

        otp.setConsumedAt(now);
        AppUser user = appUserRepository.findByEmailIgnoreCase(email).orElseGet(() -> newUser(email));
        return signIn(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Long userId) {
        return appUserRepository.findById(userId)
                .map(UserAuthService::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private AuthResponse signIn(AppUser user) {
        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been disabled");
        }
        user.setLastLoginAt(Instant.now());
        AppUser saved = appUserRepository.save(user);
        return new AuthResponse(jwtService.generate(saved), toResponse(saved));
    }

    private static AppUser newUser(String email) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setActive(true);
        return user;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl());
    }
}
