package com.ishaworld.media.entity;

import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.EntityType;
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
@Table(name = "media")
public class Media extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String title;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;
}

