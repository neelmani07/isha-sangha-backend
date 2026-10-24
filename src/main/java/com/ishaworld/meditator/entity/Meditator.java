package com.ishaworld.meditator.entity;

import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * A member's public meditator profile. Only an approximate point inside the member's city is stored,
 * never their real location.
 */
@Getter
@Setter
@Entity
@Table(name = "meditator")
public class Meditator extends BaseEntity {

    @Column(name = "app_user_id", nullable = false, unique = true)
    private Long appUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "approx_latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal approxLatitude;

    @Column(name = "approx_longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal approxLongitude;

    @Column(length = 500)
    private String about;

    /** Comma-separated practice names. */
    @Column(length = 500)
    private String practices;

    @Column(length = 255)
    private String languages;

    @Column(name = "open_to_connect", nullable = false)
    private boolean openToConnect;

    @Column(name = "open_to_volunteer", nullable = false)
    private boolean openToVolunteer;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(nullable = false)
    private boolean active = true;
}
