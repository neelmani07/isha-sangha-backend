package com.ishaworld.social.service;

import com.ishaworld.common.api.PageResponse;
import com.ishaworld.common.enums.EntityType;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.social.dto.SocialLinkResponse;
import com.ishaworld.social.dto.SocialLinkUpsertRequest;
import com.ishaworld.social.entity.SocialLink;
import com.ishaworld.social.mapper.SocialLinkMapper;
import com.ishaworld.social.repository.SocialLinkRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;

    public SocialLinkService(SocialLinkRepository socialLinkRepository, SocialLinkMapper socialLinkMapper) {
        this.socialLinkRepository = socialLinkRepository;
        this.socialLinkMapper = socialLinkMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<SocialLinkResponse> findAll(Pageable pageable) {
        return PageResponse.from(socialLinkRepository.findAll(pageable).map(socialLinkMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<SocialLinkResponse> findForEntity(EntityType type, Long id) {
        return socialLinkRepository.findByEntityTypeAndEntityId(type, id)
                .stream()
                .map(socialLinkMapper::toResponse)
                .toList();
    }

    @Transactional
    public SocialLinkResponse create(SocialLinkUpsertRequest request) {
        SocialLink link = new SocialLink();
        apply(link, request);
        return socialLinkMapper.toResponse(socialLinkRepository.save(link));
    }

    @Transactional
    public SocialLinkResponse update(Long id, SocialLinkUpsertRequest request) {
        SocialLink link = socialLinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Social link not found"));
        apply(link, request);
        return socialLinkMapper.toResponse(socialLinkRepository.save(link));
    }

    @Transactional
    public void delete(Long id) {
        socialLinkRepository.deleteById(id);
    }

    private void apply(SocialLink link, SocialLinkUpsertRequest request) {
        link.setEntityType(request.entityType());
        link.setEntityId(request.entityId());
        link.setPlatform(request.platform());
        link.setUrl(request.url());
    }
}

