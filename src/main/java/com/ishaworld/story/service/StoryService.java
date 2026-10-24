package com.ishaworld.story.service;

import com.ishaworld.center.entity.Center;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.StoryCategory;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.storage.ImageStorage;
import com.ishaworld.story.dto.StoryResponse;
import com.ishaworld.story.dto.StorySubmissionRequest;
import com.ishaworld.story.entity.Story;
import com.ishaworld.story.repository.StoryRepository;
import com.ishaworld.user.repository.AppUserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Members' stories. Submissions are reviewed before they appear; likes are one per member. */
@Service
public class StoryService {

    private static final int MAX_OPEN_SUBMISSIONS = 5;
    private static final int EXCERPT_LENGTH = 240;

    private final StoryRepository storyRepository;
    private final CityService cityService;
    private final CenterService centerService;
    private final ImageStorage imageStorage;
    private final AppUserRepository appUserRepository;

    public StoryService(
            StoryRepository storyRepository,
            CityService cityService,
            CenterService centerService,
            ImageStorage imageStorage,
            AppUserRepository appUserRepository
    ) {
        this.storyRepository = storyRepository;
        this.cityService = cityService;
        this.centerService = centerService;
        this.imageStorage = imageStorage;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> findPublic(StoryCategory category, Long viewerId) {
        List<Story> stories = storyRepository.findByActiveTrueAndListingStatusOrderByFeaturedDescPublishedAtDesc(ListingStatus.APPROVED)
                .stream()
                .filter(s -> category == null || s.getCategory() == category)
                .toList();
        return toResponses(stories, viewerId);
    }

    @Transactional(readOnly = true)
    public StoryResponse findPublicBySlug(String slug, Long viewerId) {
        Story story = storyRepository.findBySlugAndActiveTrueAndListingStatus(slug, ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        return toResponses(List.of(story), viewerId).get(0);
    }

    @Transactional
    public StoryResponse setLiked(Long userId, Long id, boolean liked) {
        Story story = storyRepository.findById(id)
                .filter(s -> s.isActive() && s.getListingStatus() == ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        if (liked) {
            storyRepository.addLike(id, userId);
        } else {
            storyRepository.removeLike(id, userId);
        }
        return toResponses(List.of(story), userId).get(0);
    }

    @Transactional
    public Story submit(Long userId, StorySubmissionRequest request) {
        long open = storyRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(s -> s.getListingStatus() == ListingStatus.PENDING).count();
        if (open >= MAX_OPEN_SUBMISSIONS) {
            throw new BusinessException("You already have " + MAX_OPEN_SUBMISSIONS + " stories waiting for review.");
        }
        Story story = new Story();
        story.setSubmittedByUserId(userId);
        apply(story, userId, request);
        story.setSlug(uniqueSlug(request.title()));
        return storyRepository.save(story);
    }

    @Transactional
    public Story update(Long userId, Long id, StorySubmissionRequest request) {
        Story story = own(userId, id);
        if (story.getListingStatus() == ListingStatus.APPROVED) {
            throw new BusinessException("This story is already published. To change it, please contact us through Feedback.");
        }
        apply(story, userId, request);
        return storyRepository.save(story);
    }

    @Transactional(readOnly = true)
    public Story own(Long userId, Long id) {
        return storyRepository.findById(id)
                .filter(s -> Objects.equals(s.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private void apply(Story story, Long userId, StorySubmissionRequest request) {
        story.setTitle(request.title().trim());
        story.setCategory(request.category());
        String body = request.body().trim().replace("\r\n", "\n");
        story.setBody(body);
        story.setExcerpt(excerptOf(body));
        String author = request.authorName() == null || request.authorName().isBlank()
                ? appUserRepository.findById(userId).map(u -> u.getName() == null ? null : shortName(u.getName())).orElse(null)
                : request.authorName().trim();
        story.setAuthorName(author == null ? "A meditator" : author);

        Center center = request.centerId() == null ? null : centerService.getCenter(request.centerId());
        story.setCenter(center);
        story.setCity(center != null ? center.getCity() : request.cityId() == null ? null : cityService.getCity(request.cityId()));

        if (request.photoKey() != null && !request.photoKey().isBlank()) {
            if (!request.photoKey().equals(story.getPhotoKey()) && !imageStorage.isOwnedBy(request.photoKey(), userId)) {
                throw new BusinessException("Please upload the photo again.");
            }
            story.setPhotoKey(request.photoKey());
        }
        story.setActive(true);
        story.setListingStatus(ListingStatus.PENDING);
        story.setRejectionReason(null);
    }

    private String uniqueSlug(String title) {
        String base = SlugGenerator.from(title);
        String slug = base;
        for (int i = 2; storyRepository.existsBySlug(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    static String excerptOf(String body) {
        String flat = body.replaceAll("\\s+", " ").trim();
        if (flat.length() <= EXCERPT_LENGTH) {
            return flat;
        }
        int cut = flat.lastIndexOf(' ', EXCERPT_LENGTH);
        return flat.substring(0, cut > 0 ? cut : EXCERPT_LENGTH) + "…";
    }

    /** "priya narayanan" -> "Priya N." */
    static String shortName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].isEmpty() ? parts[0] : Character.toUpperCase(parts[0].charAt(0)) + parts[0].substring(1);
        return parts.length > 1 ? first + " " + Character.toUpperCase(parts[parts.length - 1].charAt(0)) + "." : first;
    }

    public List<StoryResponse> toResponses(List<Story> stories, Long viewerId) {
        List<Long> ids = stories.stream().map(Story::getId).toList();
        Map<Long, Long> likes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : storyRepository.countLikes(ids)) {
                likes.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }
        Set<Long> mine = viewerId == null || ids.isEmpty() ? Set.of() : new HashSet<>(storyRepository.likedBy(viewerId, ids));
        return stories.stream().map(s -> toResponse(s, likes.getOrDefault(s.getId(), 0L), mine.contains(s.getId()))).toList();
    }

    private static StoryResponse toResponse(Story s, long likes, boolean likedByMe) {
        String kind = s.getCenter() != null ? "center" : s.getCity() != null ? "city" : null;
        String placeSlug = s.getCenter() != null ? s.getCenter().getSlug() : s.getCity() != null ? s.getCity().getSlug() : null;
        String placeName = s.getCenter() != null ? s.getCenter().getName() : s.getCity() != null ? s.getCity().getName() : null;
        int words = s.getBody().split("\\s+").length;
        return new StoryResponse(
                s.getId(), s.getSlug(), s.getTitle(), s.getCategory(), s.getExcerpt(), s.getBody(), s.getAuthorName(),
                kind, placeSlug, placeName, s.getCity() == null ? null : s.getCity().getId(),
                s.getPhotoKey() != null ? ImageStorage.publicUrl(s.getPhotoKey()) : s.getPhotoUrl(),
                s.isFeatured(), s.getPublishedAt() != null ? s.getPublishedAt() : s.getCreatedAt(),
                Math.max(1, Math.round(words / 200f)), likes, likedByMe);
    }
}
