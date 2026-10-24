package com.ishaworld.resourcelink.service;

import com.ishaworld.city.service.CityService;
import com.ishaworld.common.UrlRules;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.ResourceCategory;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.resourcelink.dto.ResourceLinkResponse;
import com.ishaworld.resourcelink.dto.ResourceSubmissionRequest;
import com.ishaworld.resourcelink.entity.ResourceLink;
import com.ishaworld.resourcelink.repository.ResourceLinkRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The members' library of useful links. Every link is reviewed before it is shown. */
@Service
public class ResourceLinkService {

    private static final int MAX_OPEN_SUBMISSIONS = 10;

    /** Invite links to chat groups are always members-only: public invites get flooded with spam. */
    private static final Set<String> CHAT_INVITE_HOSTS = Set.of(
            "chat.whatsapp.com", "wa.me", "t.me", "telegram.me", "discord.gg", "signal.group", "groups.google.com");

    private final ResourceLinkRepository repository;
    private final CityService cityService;

    public ResourceLinkService(ResourceLinkRepository repository, CityService cityService) {
        this.repository = repository;
        this.cityService = cityService;
    }

    @Transactional(readOnly = true)
    public List<ResourceLinkResponse> findPublic(ResourceCategory category, String q, Long viewerId) {
        String needle = q == null ? null : q.trim().toLowerCase(Locale.ROOT);
        List<ResourceLink> found = repository.findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).stream()
                .filter(r -> category == null || r.getCategory() == category)
                .filter(r -> needle == null || needle.isEmpty()
                        || r.getTitle().toLowerCase(Locale.ROOT).contains(needle)
                        || (r.getDescription() != null && r.getDescription().toLowerCase(Locale.ROOT).contains(needle)))
                .toList();
        return toResponses(found, viewerId);
    }

    @Transactional
    public ResourceLinkResponse setHelpful(Long userId, Long id, boolean helpful) {
        ResourceLink link = repository.findById(id)
                .filter(r -> r.isActive() && r.getListingStatus() == ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Link not found"));
        if (helpful) {
            repository.addVote(id, userId);
        } else {
            repository.removeVote(id, userId);
        }
        return toResponses(List.of(link), userId).get(0);
    }

    @Transactional
    public ResourceLink submit(Long userId, ResourceSubmissionRequest request) {
        long open = repository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(r -> r.getListingStatus() == ListingStatus.PENDING).count();
        if (open >= MAX_OPEN_SUBMISSIONS) {
            throw new BusinessException("You already have " + MAX_OPEN_SUBMISSIONS + " links waiting for review.");
        }
        ResourceLink link = new ResourceLink();
        link.setSubmittedByUserId(userId);
        apply(link, request);
        if (repository.existsByUrlIgnoreCaseAndListingStatusNot(link.getUrl(), ListingStatus.REJECTED)) {
            throw new BusinessException("This link has already been shared.");
        }
        return repository.save(link);
    }

    @Transactional
    public ResourceLink update(Long userId, Long id, ResourceSubmissionRequest request) {
        ResourceLink link = own(userId, id);
        if (link.getListingStatus() == ListingStatus.APPROVED) {
            throw new BusinessException("This link is already approved. To change it, please contact us through Feedback.");
        }
        apply(link, request);
        return repository.save(link);
    }

    @Transactional(readOnly = true)
    public ResourceLink own(Long userId, Long id) {
        return repository.findById(id)
                .filter(r -> Objects.equals(r.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Link not found"));
    }

    private void apply(ResourceLink link, ResourceSubmissionRequest request) {
        String url = UrlRules.normalize(request.url());
        link.setTitle(request.title().trim());
        link.setUrl(url);
        link.setDescription(request.description() == null || request.description().isBlank() ? null : request.description().trim());
        link.setCategory(request.category());
        link.setCity(request.cityId() == null ? null : cityService.getCity(request.cityId()));
        link.setLanguage(request.language() == null || request.language().isBlank() ? null : request.language().trim());
        link.setMembersOnly(request.membersOnly() || isChatInvite(url) || request.category() == ResourceCategory.GROUPS);
        link.setActive(true);
        link.setListingStatus(ListingStatus.PENDING);
        link.setRejectionReason(null);
    }

    static boolean isChatInvite(String url) {
        String host = UrlRules.host(url);
        return host != null && CHAT_INVITE_HOSTS.contains(host);
    }

    public List<ResourceLinkResponse> toResponses(List<ResourceLink> links, Long viewerId) {
        List<Long> ids = links.stream().map(ResourceLink::getId).toList();
        Map<Long, Long> votes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : repository.countVotes(ids)) {
                votes.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }
        Set<Long> mine = viewerId == null || ids.isEmpty() ? Set.of() : new HashSet<>(repository.votedBy(viewerId, ids));
        boolean signedIn = viewerId != null;
        return links.stream().map(r -> new ResourceLinkResponse(
                r.getId(),
                r.getTitle(),
                r.isMembersOnly() && !signedIn ? null : r.getUrl(),
                r.isMembersOnly() && !signedIn ? null : UrlRules.host(r.getUrl()),
                r.getDescription(),
                r.getCategory(),
                r.isMembersOnly(),
                r.getCity() == null ? null : r.getCity().getId(),
                r.getCity() == null ? null : r.getCity().getName(),
                r.getLanguage(),
                votes.getOrDefault(r.getId(), 0L),
                mine.contains(r.getId()),
                r.getCreatedAt()
        )).toList();
    }
}
