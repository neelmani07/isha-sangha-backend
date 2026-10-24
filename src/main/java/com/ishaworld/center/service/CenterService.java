package com.ishaworld.center.service;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.center.dto.CenterUpsertRequest;
import com.ishaworld.center.entity.Center;
import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.common.enums.CenterType;
import com.ishaworld.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CenterService {

    private final CenterRepository centerRepository;
    private final CityService cityService;

    public CenterService(CenterRepository centerRepository, CityService cityService) {
        this.centerRepository = centerRepository;
        this.cityService = cityService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CenterResponse> findAll(CenterType type, Long cityId, String q, Pageable pageable) {
        if (cityId != null) {
            return PageResponse.from(
                    centerRepository.findByCityFilters(cityId, type, q, pageable).map(this::toResponse)
            );
        }

        var page = centerRepository.findAll(pageable);
        if (type != null) {
            page = centerRepository.findByType(type, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CenterResponse findById(Long id) {
        return toResponse(getCenter(id));
    }

    @Transactional(readOnly = true)
    public CenterResponse findBySlug(String slug) {
        return toResponse(centerRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Center not found")));
    }

    @Transactional
    public CenterResponse create(CenterUpsertRequest request) {
        Center center = new Center();
        apply(center, request);
        return toResponse(centerRepository.save(center));
    }

    @Transactional
    public CenterResponse update(Long id, CenterUpsertRequest request) {
        Center center = getCenter(id);
        apply(center, request);
        return toResponse(centerRepository.save(center));
    }

    @Transactional
    public void delete(Long id) {
        centerRepository.delete(getCenter(id));
    }

    public Center getCenter(Long id) {
        return centerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Center not found"));
    }

    private void apply(Center center, CenterUpsertRequest request) {
        center.setCity(cityService.getCity(request.cityId()));
        center.setSlug(request.slug() == null || request.slug().isBlank() ? SlugGenerator.from(request.name()) : request.slug());
        center.setName(request.name());
        center.setType(request.type());
        center.setDescription(request.description());
        center.setAddress(request.address());
        center.setLatitude(request.latitude());
        center.setLongitude(request.longitude());
        center.setPhone(request.phone());
        center.setEmail(request.email());
        center.setWebsite(request.website());
        // Omitting "active" used to silently hide a new listing; default to visible.
        center.setActive(request.active() == null || request.active());
        center.setOpeningHours(request.openingHours());
        center.setPhotoUrl(request.photoUrl());
        center.setLanguages(request.languages());
        if (request.verified() != null) {
            center.setVerified(request.verified());
        }
    }

    private CenterResponse toResponse(Center center) {
        return new CenterResponse(
                center.getId(),
                center.getCity().getId(),
                center.getCity().getName(),
                center.getSlug(),
                center.getName(),
                center.getType(),
                center.getDescription(),
                center.getAddress(),
                center.getLatitude(),
                center.getLongitude(),
                center.getPhone(),
                center.getEmail(),
                center.getWebsite(),
                center.isActive(),
                center.getOpeningHours(),
                center.getPhotoUrl(),
                center.getLanguages(),
                center.isVerified()
        );
    }
}

