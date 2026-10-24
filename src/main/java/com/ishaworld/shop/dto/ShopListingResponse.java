package com.ishaworld.shop.dto;

import com.ishaworld.common.enums.ShopCategory;
import com.ishaworld.common.enums.ShopDelivery;
import java.math.BigDecimal;
import java.time.Instant;

public record ShopListingResponse(
        Long id,
        String slug,
        String title,
        ShopCategory category,
        String description,
        BigDecimal priceAmount,
        String priceNote,
        String currency,
        ShopDelivery delivery,
        Long cityId,
        String cityName,
        String citySlug,
        String sellerName,
        String contactWhatsapp,
        String contactPhone,
        String contactEmail,
        String contactWebsite,
        String photoUrl,
        Instant listedAt
) {
}
