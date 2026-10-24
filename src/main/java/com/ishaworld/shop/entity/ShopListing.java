package com.ishaworld.shop.entity;

import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.ShopCategory;
import com.ishaworld.common.enums.ShopDelivery;
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

/** A product a volunteer offers to the community. Buyers contact the seller; no payment goes through the site. */
@Getter
@Setter
@Entity
@Table(name = "shop_listing")
public class ShopListing extends BaseEntity {

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_amount", precision = 10, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_note", length = 60)
    private String priceNote;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopDelivery delivery = ShopDelivery.PICKUP;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "seller_name", nullable = false, length = 120)
    private String sellerName;

    @Column(name = "contact_whatsapp", length = 30)
    private String contactWhatsapp;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_website", length = 500)
    private String contactWebsite;

    @Column(name = "photo_key", length = 120)
    private String photoKey;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "rules_confirmed", nullable = false)
    private boolean rulesConfirmed;

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
