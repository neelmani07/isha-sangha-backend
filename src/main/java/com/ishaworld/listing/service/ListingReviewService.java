package com.ishaworld.listing.service;

import com.ishaworld.center.entity.Center;
import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.city.dto.PickedCityResponse;
import com.ishaworld.city.entity.City;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.initiative.dto.InitiativeResponse;
import com.ishaworld.initiative.entity.Initiative;
import com.ishaworld.initiative.repository.InitiativeRepository;
import com.ishaworld.initiative.service.InitiativeService;
import com.ishaworld.listing.dto.AdminListingResponse;
import com.ishaworld.resourcelink.dto.ResourceLinkResponse;
import com.ishaworld.resourcelink.entity.ResourceLink;
import com.ishaworld.resourcelink.repository.ResourceLinkRepository;
import com.ishaworld.resourcelink.service.ResourceLinkService;
import com.ishaworld.shop.entity.ShopListing;
import com.ishaworld.shop.repository.ShopListingRepository;
import com.ishaworld.shop.service.ShopService;
import com.ishaworld.story.dto.StoryResponse;
import com.ishaworld.story.entity.Story;
import com.ishaworld.story.repository.StoryRepository;
import com.ishaworld.story.service.StoryService;
import com.ishaworld.teacher.entity.Teacher;
import com.ishaworld.teacher.repository.TeacherRepository;
import com.ishaworld.teacher.service.TeacherService;
import com.ishaworld.user.entity.AppUser;
import com.ishaworld.user.repository.AppUserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The admin approval queue for everything members submit: teachers, centres, initiatives and links. */
@Service
public class ListingReviewService {

    private final TeacherRepository teacherRepository;
    private final CenterRepository centerRepository;
    private final TeacherService teacherService;
    private final CenterService centerService;
    private final AppUserRepository appUserRepository;
    private final InitiativeRepository initiativeRepository;
    private final InitiativeService initiativeService;
    private final ResourceLinkRepository resourceLinkRepository;
    private final ResourceLinkService resourceLinkService;
    private final CityRepository cityRepository;
    private final StoryRepository storyRepository;
    private final StoryService storyService;
    private final ShopListingRepository shopListingRepository;

    /** Any non-null viewer id unlocks members-only links; reviewers never vote, so 0 is safe. */
    private static final Long REVIEWER_VIEWER = 0L;

    public ListingReviewService(
            TeacherRepository teacherRepository,
            CenterRepository centerRepository,
            TeacherService teacherService,
            CenterService centerService,
            AppUserRepository appUserRepository,
            InitiativeRepository initiativeRepository,
            InitiativeService initiativeService,
            ResourceLinkRepository resourceLinkRepository,
            ResourceLinkService resourceLinkService,
            CityRepository cityRepository,
            StoryRepository storyRepository,
            StoryService storyService,
            ShopListingRepository shopListingRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.centerRepository = centerRepository;
        this.teacherService = teacherService;
        this.centerService = centerService;
        this.appUserRepository = appUserRepository;
        this.initiativeRepository = initiativeRepository;
        this.initiativeService = initiativeService;
        this.resourceLinkRepository = resourceLinkRepository;
        this.resourceLinkService = resourceLinkService;
        this.cityRepository = cityRepository;
        this.storyRepository = storyRepository;
        this.storyService = storyService;
        this.shopListingRepository = shopListingRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminListingResponse> findByStatus(ListingStatus status) {
        List<Teacher> teachers = teacherRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(t -> t.getSubmittedByUserId() != null).toList();
        List<Center> centers = centerRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(c -> c.getSubmittedByUserId() != null).toList();

        List<Initiative> initiatives = initiativeRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(i -> i.getSubmittedByUserId() != null).toList();
        List<ResourceLink> resources = resourceLinkRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(r -> r.getSubmittedByUserId() != null).toList();

        Set<Long> userIds = new HashSet<>();
        teachers.forEach(t -> userIds.add(t.getSubmittedByUserId()));
        centers.forEach(c -> userIds.add(c.getSubmittedByUserId()));
        initiatives.forEach(i -> userIds.add(i.getSubmittedByUserId()));
        resources.forEach(r -> userIds.add(r.getSubmittedByUserId()));
        Map<Long, String> emails = appUserRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getEmail, (a, b) -> a));
        Function<Long, String> email = id -> emails.getOrDefault(id, "unknown");

        List<AdminListingResponse> queue = new ArrayList<>();
        teachers.forEach(t -> queue.add(new AdminListingResponse("TEACHER", t.getId(), t.getListingStatus(),
                email.apply(t.getSubmittedByUserId()), t.getProofUrl(), t.getCreatedAt(), t.getRejectionReason(),
                teacherService.toResponse(t), null, null, null, null, null, null)));
        centers.forEach(c -> queue.add(new AdminListingResponse("CENTER", c.getId(), c.getListingStatus(),
                email.apply(c.getSubmittedByUserId()), c.getProofUrl(), c.getCreatedAt(), c.getRejectionReason(),
                null, centerService.toResponse(c), null, null, null, null, null)));
        List<InitiativeResponse> initiativeViews = initiativeService.toResponses(initiatives, null);
        for (int k = 0; k < initiatives.size(); k++) {
            Initiative i = initiatives.get(k);
            queue.add(new AdminListingResponse("INITIATIVE", i.getId(), i.getListingStatus(), email.apply(i.getSubmittedByUserId()),
                    null, i.getCreatedAt(), i.getRejectionReason(), null, null, initiativeViews.get(k), null, null, null, null));
        }
        // Reviewers must see members-only URLs too, so map as a signed-in viewer.
        List<ResourceLinkResponse> resourceViews = resourceLinkService.toResponses(resources, REVIEWER_VIEWER);
        for (int k = 0; k < resources.size(); k++) {
            ResourceLink r = resources.get(k);
            queue.add(new AdminListingResponse("RESOURCE", r.getId(), r.getListingStatus(), email.apply(r.getSubmittedByUserId()),
                    null, r.getCreatedAt(), r.getRejectionReason(), null, null, null, resourceViews.get(k), null, null, null));
        }
        for (City c : cityRepository.findByListingStatusOrderByCreatedAtAsc(status)) {
            if (c.getSubmittedByUserId() == null) {
                continue;
            }
            PickedCityResponse view = new PickedCityResponse(c.getId(), c.getName(), c.getState().getName(), c.getCountry().getName(),
                    c.getLatitude().doubleValue(), c.getLongitude().doubleValue(), c.getListingStatus());
            queue.add(new AdminListingResponse("CITY", c.getId(), c.getListingStatus(), emailOf(c.getSubmittedByUserId()),
                    null, c.getCreatedAt(), c.getRejectionReason(), null, null, null, null, view, null, null));
        }
        List<Story> stories = storyRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(st -> st.getSubmittedByUserId() != null).toList();
        List<StoryResponse> storyViews = storyService.toResponses(stories, null);
        for (int k = 0; k < stories.size(); k++) {
            Story st = stories.get(k);
            queue.add(new AdminListingResponse("STORY", st.getId(), st.getListingStatus(), emailOf(st.getSubmittedByUserId()),
                    null, st.getCreatedAt(), st.getRejectionReason(), null, null, null, null, null, storyViews.get(k), null));
        }
        for (ShopListing p : shopListingRepository.findByListingStatusOrderByCreatedAtAsc(status)) {
            if (p.getSubmittedByUserId() == null) {
                continue;
            }
            queue.add(new AdminListingResponse("PRODUCT", p.getId(), p.getListingStatus(), emailOf(p.getSubmittedByUserId()),
                    null, p.getCreatedAt(), p.getRejectionReason(), null, null, null, null, null, null, ShopService.toResponse(p)));
        }
        queue.sort(Comparator.comparing(AdminListingResponse::submittedAt));
        return queue;
    }

    private String emailOf(Long userId) {
        return appUserRepository.findById(userId).map(AppUser::getEmail).orElse("unknown");
    }

    /** Approved submissions come with checked proof, so they also get the verified badge. */
    @Transactional
    public void approve(String kind, Long id, String adminEmail) {
        review(kind, id, ListingStatus.APPROVED, null, adminEmail);
    }

    @Transactional
    public void reject(String kind, Long id, String reason, String adminEmail) {
        review(kind, id, ListingStatus.REJECTED, reason, adminEmail);
    }

    private void review(String kind, Long id, ListingStatus status, String reason, String adminEmail) {
        Instant now = Instant.now();
        switch (kind.toUpperCase()) {
            case "TEACHER" -> {
                Teacher t = teacherRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                t.setListingStatus(status);
                t.setVerified(status == ListingStatus.APPROVED);
                t.setRejectionReason(reason);
                t.setReviewedAt(now);
                t.setReviewedBy(adminEmail);
            }
            case "CENTER" -> {
                Center c = centerRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                c.setListingStatus(status);
                c.setVerified(status == ListingStatus.APPROVED);
                c.setRejectionReason(reason);
                c.setReviewedAt(now);
                c.setReviewedBy(adminEmail);
            }
            case "INITIATIVE" -> {
                Initiative i = initiativeRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                i.setListingStatus(status);
                i.setRejectionReason(reason);
                i.setReviewedAt(now);
                i.setReviewedBy(adminEmail);
            }
            case "RESOURCE" -> {
                ResourceLink r = resourceLinkRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                r.setListingStatus(status);
                r.setRejectionReason(reason);
                r.setReviewedAt(now);
                r.setReviewedBy(adminEmail);
            }
            case "CITY" -> {
                City c = cityRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                c.setListingStatus(status);
                c.setRejectionReason(reason);
                c.setReviewedAt(now);
                c.setReviewedBy(adminEmail);
            }
            case "STORY" -> {
                Story st = storyRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                st.setListingStatus(status);
                st.setRejectionReason(reason);
                st.setReviewedAt(now);
                st.setReviewedBy(adminEmail);
                if (status == ListingStatus.APPROVED && st.getPublishedAt() == null) {
                    st.setPublishedAt(now);
                }
            }
            case "PRODUCT" -> {
                ShopListing p = shopListingRepository.findById(id).orElseThrow(() -> new NotFoundException("Listing not found"));
                p.setListingStatus(status);
                p.setRejectionReason(reason);
                p.setReviewedAt(now);
                p.setReviewedBy(adminEmail);
            }
            default -> throw new NotFoundException("Unknown listing kind");
        }
    }
}
