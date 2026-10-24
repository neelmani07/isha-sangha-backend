package com.ishaworld.initiative.entity;

import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.InitiativeKind;
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
@Table(name = "initiative")
public class Initiative extends BaseEntity {

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InitiativeKind kind;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "started_year")
    private Integer startedYear;

    @Column(name = "started_by_name", length = 120)
    private String startedByName;

    /** Comma-separated InitiativeNeed names. */
    @Column(length = 100)
    private String needs;

    @Column(name = "needs_note", length = 1000)
    private String needsNote;

    @Column(name = "contact_whatsapp", length = 30)
    private String contactWhatsapp;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_website", length = 500)
    private String contactWebsite;

    @Column(name = "photo_key", length = 120)
    private String photoKey;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status", nullable = false, length = 20)
    private ListingStatus listingStatus = ListingStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by", length = 255)
    private String reviewedBy;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;
}
