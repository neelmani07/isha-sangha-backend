package com.ishaworld.teacher.entity;

import com.ishaworld.city.entity.City;
import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.TeachingMode;
import com.ishaworld.program.entity.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher")
public class Teacher extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false, length = 250, unique = true)
    private String slug;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "teaching_mode", nullable = false, length = 20)
    private TeachingMode teachingMode = TeachingMode.IN_PERSON;

    @Column(name = "years_teaching")
    private Integer yearsTeaching;

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

    @ManyToMany
    @JoinTable(
            name = "teacher_program",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private Set<Program> programs = new LinkedHashSet<>();
}

