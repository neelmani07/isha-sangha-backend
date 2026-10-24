package com.ishaworld.story.entity;

import com.ishaworld.center.entity.Center;
import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.StoryCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "story")
public class Story extends BaseEntity {

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StoryCategory category;

    @Column(nullable = false, length = 400)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_name", nullable = false, length = 120)
    private String authorName;

    /** Where it happened: a city, or a centre (which implies its city). Both optional. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    private Center center;

    @Column(name = "photo_key", length = 120)
    private String photoKey;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "published_at")
    private Instant publishedAt;

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
