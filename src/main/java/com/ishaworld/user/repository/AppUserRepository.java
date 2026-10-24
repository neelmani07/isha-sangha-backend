package com.ishaworld.user.repository;

import com.ishaworld.user.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByGoogleSubject(String googleSubject);
}
