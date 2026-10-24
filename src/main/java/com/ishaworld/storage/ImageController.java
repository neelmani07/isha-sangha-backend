package com.ishaworld.storage;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.security.AuthenticatedUser;
import java.time.Duration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    public record ImageUploadResponse(String imageKey, String url) {
    }

    private final ImageStorage imageStorage;

    public ImageController(ImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    @PostMapping(path = "/api/v1/me/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ImageUploadResponse> upload(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam("file") MultipartFile file) {
        String key = imageStorage.store(user.id(), file);
        return ApiResponse.success(new ImageUploadResponse(key, ImageStorage.publicUrl(key)));
    }

    @GetMapping("/api/v1/images/{key}")
    ResponseEntity<Resource> image(@PathVariable String key) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ImageStorage.contentTypeOf(key)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(imageStorage.load(key));
    }
}
