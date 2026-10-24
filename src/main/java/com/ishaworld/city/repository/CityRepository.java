package com.ishaworld.city.repository;

import com.ishaworld.common.enums.ListingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import com.ishaworld.city.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface CityRepository extends JpaRepository<City, Long> {

    Page<City> findByCountryId(Long countryId, Pageable pageable);

    Page<City> findByStateId(Long stateId, Pageable pageable);

    Optional<City> findBySlug(String slug);

    long countByCountryId(Long countryId);

    long countByStateId(Long stateId);

    List<City> findTop10ByNameContainingIgnoreCaseAndListingStatusAndActiveTrue(String name, ListingStatus listingStatus);

    Page<City> findByListingStatusAndActiveTrue(ListingStatus listingStatus, Pageable pageable);

    List<City> findByCountryIdAndNameIgnoreCase(Long countryId, String name);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"state", "country"})
    List<City> findTop50ByNameContainingIgnoreCaseOrderByName(String name);

    @Query("select distinct c.country.id from City c where c.listingStatus = :status")
    List<Long> findCountryIdsWithStatus(@Param("status") ListingStatus status);

    @EntityGraph(attributePaths = {"state", "country"})
    List<City> findByListingStatusOrderByCreatedAtAsc(ListingStatus listingStatus);
}

