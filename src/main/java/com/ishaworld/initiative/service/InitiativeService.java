package com.ishaworld.initiative.service;

import com.ishaworld.city.entity.City;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.UrlRules;
import com.ishaworld.common.enums.InitiativeKind;
import com.ishaworld.common.enums.InitiativeNeed;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.initiative.dto.InitiativeResponse;
import com.ishaworld.initiative.dto.InitiativeStatsResponse;
import com.ishaworld.initiative.dto.InitiativeSubmissionRequest;
import com.ishaworld.initiative.entity.Initiative;
import com.ishaworld.initiative.repository.InitiativeRepository;
import com.ishaworld.storage.ImageStorage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Seva and ventures members list. Submissions are reviewed before they appear. */
@Service
public class InitiativeService {

    private static final int MAX_OPEN_SUBMISSIONS = 5;

    private final InitiativeRepository initiativeRepository;
    private final CityService cityService;
    private final ImageStorage imageStorage;

    public InitiativeService(InitiativeRepository initiativeRepository, CityService cityService, ImageStorage imageStorage) {
        this.initiativeRepository = initiativeRepository;
        this.cityService = cityService;
        this.imageStorage = imageStorage;
    }

    // ---- Public ----

    /** Approved initiatives, optionally filtered by kind and by any of the needs asked for. */
    @Transactional(readOnly = true)
    public List<InitiativeResponse> findPublic(InitiativeKind kind, Set<InitiativeNeed> needs, Long cityId, Long viewerId) {
        List<Initiative> found = initiativeRepository.findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus.APPROVED)
                .stream()
                .filter(i -> kind == null || i.getKind() == kind)
                .filter(i -> cityId == null || Objects.equals(i.getCity().getId(), cityId))
                .filter(i -> needs == null || needs.isEmpty() || parseNeeds(i.getNeeds()).stream().anyMatch(needs::contains))
                .toList();
        return toResponses(found, viewerId);
    }

    @Transactional(readOnly = true)
    public InitiativeResponse findPublicBySlug(String slug, Long viewerId) {
        Initiative initiative = initiativeRepository.findBySlugAndActiveTrueAndListingStatus(slug, ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Initiative not found"));
        return toResponses(List.of(initiative), viewerId).get(0);
    }

    @Transactional(readOnly = true)
    public InitiativeStatsResponse stats() {
        List<Initiative> all = initiativeRepository.findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        Map<Long, Long> supporters = supporterCounts(all.stream().map(Initiative::getId).toList());
        long places = all.stream().map(i -> i.getCity().getId()).distinct().count();
        return new InitiativeStatsResponse(all.size(), supporters.values().stream().mapToLong(Long::longValue).sum(), places);
    }

    @Transactional
    public InitiativeResponse setSupport(Long userId, Long id, boolean support) {
        Initiative initiative = initiativeRepository.findById(id)
                .filter(i -> i.isActive() && i.getListingStatus() == ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Initiative not found"));
        if (support) {
            initiativeRepository.addSupporter(id, userId);
        } else {
            initiativeRepository.removeSupporter(id, userId);
        }
        return toResponses(List.of(initiative), userId).get(0);
    }

    // ---- Member submissions ----

    @Transactional
    public Initiative submit(Long userId, InitiativeSubmissionRequest request) {
        long open = initiativeRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(i -> i.getListingStatus() == ListingStatus.PENDING).count();
        if (open >= MAX_OPEN_SUBMISSIONS) {
            throw new BusinessException("You already have " + MAX_OPEN_SUBMISSIONS + " initiatives waiting for review.");
        }
        Initiative initiative = new Initiative();
        initiative.setSubmittedByUserId(userId);
        apply(initiative, userId, request);
        initiative.setSlug(uniqueSlug(request.title(), initiative.getCity()));
        return initiativeRepository.save(initiative);
    }

    @Transactional
    public Initiative update(Long userId, Long id, InitiativeSubmissionRequest request) {
        Initiative initiative = own(userId, id);
        if (initiative.getListingStatus() == ListingStatus.APPROVED) {
            throw new BusinessException("This initiative is already approved. To change it, please contact us through Feedback.");
        }
        apply(initiative, userId, request);
        return initiativeRepository.save(initiative);
    }

    @Transactional(readOnly = true)
    public Initiative own(Long userId, Long id) {
        return initiativeRepository.findById(id)
                .filter(i -> Objects.equals(i.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Initiative not found"));
    }

    private void apply(Initiative initiative, Long userId, InitiativeSubmissionRequest request) {
        City city = cityService.getCity(request.cityId());
        initiative.setCity(city);
        initiative.setTitle(request.title().trim());
        initiative.setKind(request.kind());
        initiative.setSummary(request.summary().trim());
        initiative.setDescription(blankToNull(request.description()));
        initiative.setStartedYear(request.startedYear());
        initiative.setStartedByName(blankToNull(request.startedByName()));
        initiative.setNeeds(request.needs() == null || request.needs().isEmpty()
                ? null
                : request.needs().stream().map(Enum::name).sorted().collect(Collectors.joining(",")));
        initiative.setNeedsNote(blankToNull(request.needsNote()));
        initiative.setContactWhatsapp(request.contactWhatsapp() == null ? null : blankToNull(request.contactWhatsapp().replaceAll("[^0-9]", "")));
        initiative.setContactEmail(blankToNull(request.contactEmail()));
        initiative.setContactWebsite(UrlRules.normalize(request.contactWebsite()));
        if (initiative.getContactWhatsapp() == null && initiative.getContactEmail() == null && initiative.getContactWebsite() == null) {
            throw new BusinessException("Please add at least one way for supporters to reach you.");
        }
        if (request.photoKey() != null && !request.photoKey().isBlank()) {
            if (!request.photoKey().equals(initiative.getPhotoKey()) && !imageStorage.isOwnedBy(request.photoKey(), userId)) {
                throw new BusinessException("Please upload the photo again.");
            }
            initiative.setPhotoKey(request.photoKey());
        }
        initiative.setLatitude(city.getLatitude());
        initiative.setLongitude(city.getLongitude());
        initiative.setActive(true);
        initiative.setListingStatus(ListingStatus.PENDING);
        initiative.setRejectionReason(null);
    }

    private String uniqueSlug(String title, City city) {
        String base = SlugGenerator.from(title + " " + city.getName());
        String slug = base;
        for (int i = 2; initiativeRepository.existsBySlug(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    // ---- Mapping ----

    public List<InitiativeResponse> toResponses(List<Initiative> initiatives, Long viewerId) {
        List<Long> ids = initiatives.stream().map(Initiative::getId).toList();
        Map<Long, Long> counts = supporterCounts(ids);
        Set<Long> mine = viewerId == null || ids.isEmpty() ? Set.of() : new HashSet<>(initiativeRepository.supportedBy(viewerId, ids));
        return initiatives.stream().map(i -> toResponse(i, counts.getOrDefault(i.getId(), 0L), mine.contains(i.getId()))).toList();
    }

    private Map<Long, Long> supporterCounts(Collection<Long> ids) {
        Map<Long, Long> counts = new HashMap<>();
        if (ids.isEmpty()) {
            return counts;
        }
        for (Object[] row : initiativeRepository.countSupporters(ids)) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private static InitiativeResponse toResponse(Initiative i, long supporters, boolean supportedByMe) {
        City city = i.getCity();
        return new InitiativeResponse(
                i.getId(), i.getSlug(), i.getTitle(), i.getKind(), i.getSummary(), i.getDescription(),
                city.getId(), city.getName(), city.getSlug(),
                i.getLatitude() == null ? null : i.getLatitude().doubleValue(),
                i.getLongitude() == null ? null : i.getLongitude().doubleValue(),
                i.getStartedYear(), i.getStartedByName(),
                parseNeeds(i.getNeeds()).stream().map(Enum::name).toList(),
                i.getNeedsNote(), i.getContactWhatsapp(), i.getContactEmail(), i.getContactWebsite(),
                i.getPhotoKey() != null ? ImageStorage.publicUrl(i.getPhotoKey()) : i.getPhotoUrl(),
                supporters, supportedByMe);
    }

    private static List<InitiativeNeed> parseNeeds(String needs) {
        if (needs == null || needs.isBlank()) {
            return List.of();
        }
        return Arrays.stream(needs.split(",")).map(String::trim).map(InitiativeNeed::valueOf).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
