package com.ishaworld.media.service;

import com.ishaworld.common.api.PageResponse;
import com.ishaworld.common.enums.EntityType;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.media.dto.MediaResponse;
import com.ishaworld.media.dto.MediaUpsertRequest;
import com.ishaworld.media.entity.Media;
import com.ishaworld.media.mapper.MediaMapper;
import com.ishaworld.media.repository.MediaRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    public MediaService(MediaRepository mediaRepository, MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.mediaMapper = mediaMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<MediaResponse> findAll(Pageable pageable) {
        return PageResponse.from(mediaRepository.findAll(pageable).map(mediaMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> findForEntity(EntityType type, Long id) {
        return mediaRepository.findByEntityTypeAndEntityId(type, id)
                .stream()
                .map(mediaMapper::toResponse)
                .toList();
    }

    @Transactional
    public MediaResponse create(MediaUpsertRequest request) {
        Media media = new Media();
        apply(media, request);
        return mediaMapper.toResponse(mediaRepository.save(media));
    }

    @Transactional
    public MediaResponse update(Long id, MediaUpsertRequest request) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media not found"));
        apply(media, request);
        return mediaMapper.toResponse(mediaRepository.save(media));
    }

    @Transactional
    public void delete(Long id) {
        mediaRepository.deleteById(id);
    }

    private void apply(Media media, MediaUpsertRequest request) {
        media.setEntityType(request.entityType());
        media.setEntityId(request.entityId());
        media.setUrl(request.url());
        media.setTitle(request.title());
        media.setPrimary(request.primary());
    }
}

