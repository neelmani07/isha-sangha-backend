package com.ishaworld.listing.service;

import com.ishaworld.center.entity.Center;
import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.listing.dto.AdminListingResponse;
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

/** The admin approval queue for member-submitted teachers and centers. */
@Service
public class ListingReviewService {

    private final TeacherRepository teacherRepository;
    private final CenterRepository centerRepository;
    private final TeacherService teacherService;
    private final CenterService centerService;
    private final AppUserRepository appUserRepository;

    public ListingReviewService(
            TeacherRepository teacherRepository,
            CenterRepository centerRepository,
            TeacherService teacherService,
            CenterService centerService,
            AppUserRepository appUserRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.centerRepository = centerRepository;
        this.teacherService = teacherService;
        this.centerService = centerService;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminListingResponse> findByStatus(ListingStatus status) {
        List<Teacher> teachers = teacherRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(t -> t.getSubmittedByUserId() != null).toList();
        List<Center> centers = centerRepository.findByListingStatusOrderByCreatedAtAsc(status).stream()
                .filter(c -> c.getSubmittedByUserId() != null).toList();

        Set<Long> userIds = new HashSet<>();
        teachers.forEach(t -> userIds.add(t.getSubmittedByUserId()));
        centers.forEach(c -> userIds.add(c.getSubmittedByUserId()));
        Map<Long, String> emails = appUserRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getEmail, (a, b) -> a));
        Function<Long, String> email = id -> emails.getOrDefault(id, "unknown");

        List<AdminListingResponse> queue = new ArrayList<>();
        teachers.forEach(t -> queue.add(new AdminListingResponse("TEACHER", t.getId(), t.getListingStatus(),
                email.apply(t.getSubmittedByUserId()), t.getProofUrl(), t.getCreatedAt(), t.getRejectionReason(),
                teacherService.toResponse(t), null)));
        centers.forEach(c -> queue.add(new AdminListingResponse("CENTER", c.getId(), c.getListingStatus(),
                email.apply(c.getSubmittedByUserId()), c.getProofUrl(), c.getCreatedAt(), c.getRejectionReason(),
                null, centerService.toResponse(c))));
        queue.sort(Comparator.comparing(AdminListingResponse::submittedAt));
        return queue;
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
            default -> throw new NotFoundException("Unknown listing kind");
        }
    }
}
