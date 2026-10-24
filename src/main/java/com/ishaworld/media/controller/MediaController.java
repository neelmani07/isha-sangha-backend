package com.ishaworld.media.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.EntityType;
import com.ishaworld.media.dto.MediaResponse;
import com.ishaworld.media.service.MediaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    ApiResponse<List<MediaResponse>> findForEntity(@RequestParam EntityType entityType, @RequestParam Long entityId) {
        return ApiResponse.success(mediaService.findForEntity(entityType, entityId));
    }
}

