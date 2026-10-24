package com.ishaworld.feedback.entity;

import com.ishaworld.common.entity.BaseEntity;
import com.ishaworld.common.enums.FeedbackType;
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
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private FeedbackType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
}

