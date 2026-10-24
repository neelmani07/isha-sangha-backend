package com.ishaworld.feedback.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.feedback.dto.FeedbackRequest;
import com.ishaworld.feedback.dto.FeedbackResponse;
import com.ishaworld.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    ApiResponse<FeedbackResponse> submit(@Valid @RequestBody FeedbackRequest request) {
        return ApiResponse.success(feedbackService.submit(request));
    }
}

