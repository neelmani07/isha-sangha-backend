package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.feedback.dto.FeedbackResponse;
import com.ishaworld.feedback.service.FeedbackService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    ApiResponse<PageResponse<FeedbackResponse>> findAll(Pageable pageable) {
        return ApiResponse.success(feedbackService.findAll(pageable));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ApiResponse.success(null);
    }
}

