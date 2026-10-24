package com.ishaworld.shop.repository;

import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.shop.entity.ShopListing;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopListingRepository extends JpaRepository<ShopListing, Long> {

    @EntityGraph(attributePaths = "city")
    List<ShopListing> findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = "city")
    Optional<ShopListing> findBySlugAndActiveTrueAndListingStatus(String slug, ListingStatus listingStatus);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "city")
    List<ShopListing> findBySubmittedByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "city")
    List<ShopListing> findByListingStatusOrderByCreatedAtAsc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = "city")
    List<ShopListing> findTop50ByTitleContainingIgnoreCaseOrderByTitle(String title);
}
