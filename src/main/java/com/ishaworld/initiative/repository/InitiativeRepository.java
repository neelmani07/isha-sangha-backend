package com.ishaworld.initiative.repository;

import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.initiative.entity.Initiative;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InitiativeRepository extends JpaRepository<Initiative, Long> {

    @EntityGraph(attributePaths = "city")
    List<Initiative> findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = "city")
    Optional<Initiative> findBySlugAndActiveTrueAndListingStatus(String slug, ListingStatus listingStatus);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "city")
    List<Initiative> findTop50ByTitleContainingIgnoreCaseOrderByTitle(String title);

    @EntityGraph(attributePaths = "city")
    List<Initiative> findBySubmittedByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "city")
    List<Initiative> findByListingStatusOrderByCreatedAtAsc(ListingStatus listingStatus);

    /** Rows of [initiativeId, supporterCount]. */
    @Query(value = "SELECT initiative_id, COUNT(*) FROM initiative_supporter WHERE initiative_id IN (:ids) GROUP BY initiative_id", nativeQuery = true)
    List<Object[]> countSupporters(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT initiative_id FROM initiative_supporter WHERE app_user_id = :userId AND initiative_id IN (:ids)", nativeQuery = true)
    List<Long> supportedBy(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);

    @Modifying
    @Query(value = "INSERT INTO initiative_supporter (initiative_id, app_user_id) VALUES (:id, :userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addSupporter(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM initiative_supporter WHERE initiative_id = :id AND app_user_id = :userId", nativeQuery = true)
    void removeSupporter(@Param("id") Long id, @Param("userId") Long userId);
}
