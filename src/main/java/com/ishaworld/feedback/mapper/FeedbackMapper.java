package com.ishaworld.feedback.mapper;

import com.ishaworld.feedback.dto.FeedbackResponse;
import com.ishaworld.feedback.entity.Feedback;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    FeedbackResponse toResponse(Feedback feedback);
}

