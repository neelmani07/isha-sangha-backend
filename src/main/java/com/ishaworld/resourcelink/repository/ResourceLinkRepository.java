package com.ishaworld.resourcelink.repository;

import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.resourcelink.entity.ResourceLink;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceLinkRepository extends JpaRepository<ResourceLink, Long> {

    @EntityGraph(attributePaths = "city")
    List<ResourceLink> findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = "city")
    List<ResourceLink> findBySubmittedByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "city")
    List<ResourceLink> findByListingStatusOrderByCreatedAtAsc(ListingStatus listingStatus);

    boolean existsByUrlIgnoreCaseAndListingStatusNot(String url, ListingStatus listingStatus);

    @EntityGraph(attributePaths = "city")
    List<ResourceLink> findTop50ByTitleContainingIgnoreCaseOrderByTitle(String title);

    /** Rows of [resourceId, helpfulCount]. */
    @Query(value = "SELECT resource_id, COUNT(*) FROM resource_vote WHERE resource_id IN (:ids) GROUP BY resource_id", nativeQuery = true)
    List<Object[]> countVotes(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT resource_id FROM resource_vote WHERE app_user_id = :userId AND resource_id IN (:ids)", nativeQuery = true)
    List<Long> votedBy(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);

    @Modifying
    @Query(value = "INSERT INTO resource_vote (resource_id, app_user_id) VALUES (:id, :userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addVote(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM resource_vote WHERE resource_id = :id AND app_user_id = :userId", nativeQuery = true)
    void removeVote(@Param("id") Long id, @Param("userId") Long userId);
}
