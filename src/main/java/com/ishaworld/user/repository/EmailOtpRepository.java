package com.ishaworld.user.repository;

import com.ishaworld.user.entity.EmailOtp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    long countByEmailAndCreatedAtAfter(String email, Instant since);

    Optional<EmailOtp> findFirstByEmailAndConsumedAtIsNullOrderByCreatedAtDesc(String email);
}
