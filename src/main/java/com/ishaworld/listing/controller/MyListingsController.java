package com.ishaworld.listing.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.initiative.dto.InitiativeSubmissionRequest;
import com.ishaworld.listing.dto.CenterSubmissionRequest;
import com.ishaworld.listing.dto.MyListingResponse;
import com.ishaworld.listing.dto.OwnListingResponse;
import com.ishaworld.listing.dto.ProofUploadResponse;
import com.ishaworld.listing.dto.TeacherSubmissionRequest;
import com.ishaworld.listing.service.ListingSubmissionService;
import com.ishaworld.resourcelink.dto.ResourceSubmissionRequest;
import com.ishaworld.security.AuthenticatedUser;
import com.ishaworld.shop.dto.ShopListingRequest;
import com.ishaworld.story.dto.StorySubmissionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** A signed-in member's own teacher and center submissions. */
@RestController
@RequestMapping("/api/v1/me")
public class MyListingsController {

    private final ListingSubmissionService submissionService;

    public MyListingsController(ListingSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(path = "/proofs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ProofUploadResponse> uploadProof(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success(new ProofUploadResponse(submissionService.uploadProof(user.id(), file)));
    }

    @GetMapping("/listings")
    ApiResponse<List<MyListingResponse>> myListings(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(submissionService.myListings(user.id()));
    }

    @GetMapping("/listings/teachers/{id}")
    ApiResponse<OwnListingResponse> ownTeacher(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownTeacher(user.id(), id));
    }

    @GetMapping("/listings/centers/{id}")
    ApiResponse<OwnListingResponse> ownCenter(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownCenter(user.id(), id));
    }

    @GetMapping("/listings/products/{id}")
    ApiResponse<OwnListingResponse> ownProduct(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownProduct(user.id(), id));
    }

    @PostMapping("/listings/products")
    ApiResponse<MyListingResponse> submitProduct(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ShopListingRequest request) {
        return ApiResponse.success(submissionService.submitProduct(user.id(), request));
    }

    @PutMapping("/listings/products/{id}")
    ApiResponse<MyListingResponse> updateProduct(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ShopListingRequest request
    ) {
        return ApiResponse.success(submissionService.updateProduct(user.id(), id, request));
    }

    @GetMapping("/listings/stories/{id}")
    ApiResponse<OwnListingResponse> ownStory(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownStory(user.id(), id));
    }

    @PostMapping("/listings/stories")
    ApiResponse<MyListingResponse> submitStory(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody StorySubmissionRequest request) {
        return ApiResponse.success(submissionService.submitStory(user.id(), request));
    }

    @PutMapping("/listings/stories/{id}")
    ApiResponse<MyListingResponse> updateStory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody StorySubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.updateStory(user.id(), id, request));
    }

    @GetMapping("/listings/initiatives/{id}")
    ApiResponse<OwnListingResponse> ownInitiative(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownInitiative(user.id(), id));
    }

    @PostMapping("/listings/initiatives")
    ApiResponse<MyListingResponse> submitInitiative(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody InitiativeSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.submitInitiative(user.id(), request));
    }

    @PutMapping("/listings/initiatives/{id}")
    ApiResponse<MyListingResponse> updateInitiative(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody InitiativeSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.updateInitiative(user.id(), id, request));
    }

    @GetMapping("/listings/resources/{id}")
    ApiResponse<OwnListingResponse> ownResource(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.success(submissionService.ownResource(user.id(), id));
    }

    @PostMapping("/listings/resources")
    ApiResponse<MyListingResponse> submitResource(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ResourceSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.submitResource(user.id(), request));
    }

    @PutMapping("/listings/resources/{id}")
    ApiResponse<MyListingResponse> updateResource(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ResourceSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.updateResource(user.id(), id, request));
    }

    @PostMapping("/listings/teachers")
    ApiResponse<MyListingResponse> submitTeacher(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody TeacherSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.submitTeacher(user.id(), request));
    }

    @PutMapping("/listings/teachers/{id}")
    ApiResponse<MyListingResponse> updateTeacher(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody TeacherSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.updateTeacher(user.id(), id, request));
    }

    @PostMapping("/listings/centers")
    ApiResponse<MyListingResponse> submitCenter(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CenterSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.submitCenter(user.id(), request));
    }

    @PutMapping("/listings/centers/{id}")
    ApiResponse<MyListingResponse> updateCenter(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody CenterSubmissionRequest request
    ) {
        return ApiResponse.success(submissionService.updateCenter(user.id(), id, request));
    }
}
