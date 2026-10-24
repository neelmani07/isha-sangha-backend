package com.ishaworld.story.repository;

import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.story.entity.Story;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryRepository extends JpaRepository<Story, Long> {

    @EntityGraph(attributePaths = {"city", "center"})
    List<Story> findByActiveTrueAndListingStatusOrderByFeaturedDescPublishedAtDesc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = {"city", "center"})
    Optional<Story> findBySlugAndActiveTrueAndListingStatus(String slug, ListingStatus listingStatus);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"city", "center"})
    List<Story> findBySubmittedByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"city", "center"})
    List<Story> findByListingStatusOrderByCreatedAtAsc(ListingStatus listingStatus);

    @EntityGraph(attributePaths = {"city", "center"})
    List<Story> findTop50ByTitleContainingIgnoreCaseOrderByTitle(String title);

    /** Rows of [storyId, likeCount]. */
    @Query(value = "SELECT story_id, COUNT(*) FROM story_like WHERE story_id IN (:ids) GROUP BY story_id", nativeQuery = true)
    List<Object[]> countLikes(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT story_id FROM story_like WHERE app_user_id = :userId AND story_id IN (:ids)", nativeQuery = true)
    List<Long> likedBy(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);

    @Modifying
    @Query(value = "INSERT INTO story_like (story_id, app_user_id) VALUES (:id, :userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addLike(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM story_like WHERE story_id = :id AND app_user_id = :userId", nativeQuery = true)
    void removeLike(@Param("id") Long id, @Param("userId") Long userId);
}
