package com.ishaworld.center.entity;

import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.CenterType;
import com.ishaworld.common.enums.ListingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "center")
public class Center extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false, length = 250, unique = true)
    private String slug;

    @Column(nullable = false, length = 250)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CenterType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "opening_hours", length = 500)
    private String openingHours;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 255)
    private String languages;

    @Column(nullable = false)
    private boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status", nullable = false, length = 20)
    private ListingStatus listingStatus = ListingStatus.APPROVED;

    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by", length = 255)
    private String reviewedBy;
}

