package com.ishaworld.city.entity;

import com.ishaworld.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "community_stats")
public class CommunityStats extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false, unique = true)
    private City city;

    @Column(name = "meditator_count", nullable = false)
    private int meditatorCount;

    @Column(name = "volunteer_count", nullable = false)
    private int volunteerCount;

    @Column(name = "teacher_count", nullable = false)
    private int teacherCount;

    @Column(name = "yantra_count", nullable = false)
    private int yantraCount;

    @Column(name = "sannidhi_count", nullable = false)
    private int sannidhiCount;

    @Column(name = "center_count", nullable = false)
    private int centerCount;

    @Column(name = "temple_count", nullable = false)
    private int templeCount;

    @Column(name = "shop_count", nullable = false)
    private int shopCount;
}

