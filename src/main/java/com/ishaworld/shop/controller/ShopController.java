package com.ishaworld.shop.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.ShopCategory;
import com.ishaworld.shop.dto.ShopListingResponse;
import com.ishaworld.shop.service.ShopService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/api/v1/shop")
    ApiResponse<List<ShopListingResponse>> list(@RequestParam(required = false) ShopCategory category) {
        return ApiResponse.success(shopService.findPublic(category));
    }

    @GetMapping("/api/v1/shop/slug/{slug}")
    ApiResponse<ShopListingResponse> bySlug(@PathVariable String slug) {
        return ApiResponse.success(shopService.findPublicBySlug(slug));
    }
}
