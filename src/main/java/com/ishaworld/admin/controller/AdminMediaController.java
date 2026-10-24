package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.media.dto.MediaResponse;
import com.ishaworld.media.dto.MediaUpsertRequest;
import com.ishaworld.media.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;

    public AdminMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    ApiResponse<MediaResponse> create(@Valid @RequestBody MediaUpsertRequest request) {
        return ApiResponse.success(mediaService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<MediaResponse> update(@PathVariable Long id, @Valid @RequestBody MediaUpsertRequest request) {
        return ApiResponse.success(mediaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ApiResponse.success(null);
    }
}

