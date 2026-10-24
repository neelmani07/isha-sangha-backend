package com.ishaworld.listing.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.listing.dto.AdminListingResponse;
import com.ishaworld.listing.dto.RejectListingRequest;
import com.ishaworld.listing.service.ListingReviewService;
import com.ishaworld.storage.ProofStorage;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminListingController {

    private final ListingReviewService reviewService;
    private final ProofStorage proofStorage;

    public AdminListingController(ListingReviewService reviewService, ProofStorage proofStorage) {
        this.reviewService = reviewService;
        this.proofStorage = proofStorage;
    }

    @GetMapping("/listings")
    ApiResponse<List<AdminListingResponse>> queue(@RequestParam(defaultValue = "PENDING") ListingStatus status) {
        return ApiResponse.success(reviewService.findByStatus(status));
    }

    @PostMapping("/listings/{kind}/{id}/approve")
    ApiResponse<Void> approve(@PathVariable String kind, @PathVariable Long id, @AuthenticationPrincipal UserDetails admin) {
        reviewService.approve(kind, id, admin.getUsername());
        return ApiResponse.success(null);
    }

    @PostMapping("/listings/{kind}/{id}/reject")
    ApiResponse<Void> reject(
            @PathVariable String kind,
            @PathVariable Long id,
            @Valid @RequestBody RejectListingRequest request,
            @AuthenticationPrincipal UserDetails admin
    ) {
        reviewService.reject(kind, id, request.reason(), admin.getUsername());
        return ApiResponse.success(null);
    }

    /** Streams a proof document; admins only (the whole /admin path is admin-protected). */
    @GetMapping("/proofs/{key}")
    ResponseEntity<Resource> proof(@PathVariable String key) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ProofStorage.contentTypeOf(key)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + key + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(proofStorage.load(key));
    }
}
