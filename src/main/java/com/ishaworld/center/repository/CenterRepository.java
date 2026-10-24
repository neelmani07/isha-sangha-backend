package com.ishaworld.center.repository;

import com.ishaworld.center.entity.Center;
import com.ishaworld.common.enums.CenterType;
import com.ishaworld.common.enums.ListingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CenterRepository extends JpaRepository<Center, Long> {

    Page<Center> findByType(CenterType type, Pageable pageable);

    Page<Center> findByCityId(Long cityId, Pageable pageable);

    Optional<Center> findBySlug(String slug);

    long countByCityId(Long cityId);

    @Query("select count(c) from Center c where c.city.country.id = :countryId")
    long countByCountryId(@Param("countryId") Long countryId);

    @Query("""
            select c from Center c
            where c.city.id = :cityId
            and (:type is null or c.type = :type)
            and (:q is null or :q = '' or lower(c.name) like lower(concat('%', :q, '%')))
            """)
    Page<Center> findByCityFilters(
            @Param("cityId") Long cityId,
            @Param("type") CenterType type,
            @Param("q") String q,
            Pageable pageable
    );

    List<Center> findTop10ByNameContainingIgnoreCase(String name);

    /** Active, approved centers: what the public map may show. */
    @EntityGraph(attributePaths = "city")
    Page<Center> findByActiveTrueAndListingStatus(ListingStatus listingStatus, Pageable pageable);
}

