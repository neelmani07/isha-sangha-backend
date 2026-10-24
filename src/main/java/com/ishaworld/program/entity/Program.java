package com.ishaworld.program.entity;

import com.ishaworld.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "program")
public class Program extends BaseEntity {

    @Column(nullable = false, unique = true, length = 250)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}

