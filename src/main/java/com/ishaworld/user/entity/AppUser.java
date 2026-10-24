package com.ishaworld.user.entity;

import com.ishaworld.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "google_subject", unique = true, length = 255)
    private String googleSubject;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
