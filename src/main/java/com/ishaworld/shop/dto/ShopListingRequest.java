package com.ishaworld.shop.dto;

import com.ishaworld.common.enums.ShopCategory;
import com.ishaworld.common.enums.ShopDelivery;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ShopListingRequest(
        @NotBlank @Size(max = 150) String title,
        @NotNull ShopCategory category,
        @Size(max = 4000) String description,
        @DecimalMin("0") @DecimalMax("10000000") BigDecimal priceAmount,
        @Size(max = 60) String priceNote,
        ShopDelivery delivery,
        @NotNull Long cityId,
        @Size(max = 120) String sellerName,
        @Pattern(regexp = "^$|^\\+?[0-9 ]{8,20}$", message = "must be a phone number with country code") String contactWhatsapp,
        @Pattern(regexp = "^$|^\\+?[0-9 ()-]{6,20}$", message = "must be a phone number") String contactPhone,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 500) String contactWebsite,
        String photoKey,
        @AssertTrue(message = "must be confirmed: not Isha-branded, not presented as consecrated, and yours to sell") boolean rulesConfirmed
) {
}
