package com.ishaworld.country.entity;

import com.ishaworld.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "country")
public class Country extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "iso_code", nullable = false, unique = true, length = 10)
    private String isoCode;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
}

