package com.ishaworld.social.entity;

import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.EntityType;
import com.ishaworld.common.enums.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "social_link")
public class SocialLink extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(nullable = false, length = 500)
    private String url;
}

