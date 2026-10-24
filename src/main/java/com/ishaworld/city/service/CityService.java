package com.ishaworld.city.service;

import com.ishaworld.city.dto.CityResponse;
import com.ishaworld.city.dto.CityUpsertRequest;
import com.ishaworld.city.dto.CommunityStatsResponse;
import com.ishaworld.city.dto.CommunityStatsUpsertRequest;
import com.ishaworld.city.entity.City;
import com.ishaworld.city.entity.CommunityStats;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.city.repository.CommunityStatsRepository;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.country.service.CountryService;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.state.service.StateService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CommunityStatsRepository communityStatsRepository;
    private final CountryService countryService;
    private final StateService stateService;

    public CityService(
            CityRepository cityRepository,
            CommunityStatsRepository communityStatsRepository,
            CountryService countryService,
            StateService stateService
    ) {
        this.cityRepository = cityRepository;
        this.communityStatsRepository = communityStatsRepository;
        this.countryService = countryService;
        this.stateService = stateService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CityResponse> findAll(Long countryId, Long stateId, Pageable pageable) {
        var page = cityRepository.findAll(pageable);
        if (stateId != null) {
            page = cityRepository.findByStateId(stateId, pageable);
        } else if (countryId != null) {
            page = cityRepository.findByCountryId(countryId, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CityResponse findById(Long id) {
        return toResponse(getCity(id));
    }

    @Transactional(readOnly = true)
    public CityResponse findBySlug(String slug) {
        // Public page: only approved, visible cities (member-added ones wait for review; admins can hide any).
        return toResponse(cityRepository.findBySlug(slug)
                .filter(city -> city.isActive() && city.getListingStatus() == ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("City not found")));
    }

    @Transactional(readOnly = true)
    public CommunityStatsResponse findStats(Long id) {
        return statsResponse(communityStatsRepository.findByCityId(id).orElse(null));
    }

    @Transactional
    public CityResponse create(CityUpsertRequest request) {
        City city = new City();
        apply(city, request);
        City saved = cityRepository.save(city);
        upsertStats(saved, request.stats());
        return toResponse(saved);
    }

    @Transactional
    public CityResponse update(Long id, CityUpsertRequest request) {
        City city = getCity(id);
        apply(city, request);
        City saved = cityRepository.save(city);
        upsertStats(saved, request.stats());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        cityRepository.delete(getCity(id));
    }

    public City getCity(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("City not found"));
    }

    private void apply(City city, CityUpsertRequest request) {
        city.setCountry(countryService.getCountry(request.countryId()));
        city.setState(stateService.getState(request.stateId()));
        city.setSlug(request.slug() == null || request.slug().isBlank() ? SlugGenerator.from(request.name()) : request.slug());
        city.setName(request.name());
        city.setLatitude(request.latitude());
        city.setLongitude(request.longitude());
        city.setDescription(request.description());
        city.setHeroImageUrl(request.heroImageUrl());
        city.setActive(request.active());
    }

    private void upsertStats(City city, CommunityStatsUpsertRequest request) {
        if (request == null) {
            return;
        }

        CommunityStats stats = communityStatsRepository.findByCityId(city.getId()).orElseGet(CommunityStats::new);
        stats.setCity(city);
        stats.setMeditatorCount(request.meditatorCount());
        stats.setVolunteerCount(request.volunteerCount());
        stats.setTeacherCount(request.teacherCount());
        stats.setYantraCount(request.yantraCount());
        stats.setSannidhiCount(request.sannidhiCount());
        stats.setCenterCount(request.centerCount());
        stats.setTempleCount(request.templeCount());
        stats.setShopCount(request.shopCount());
        communityStatsRepository.save(stats);
    }

    private CityResponse toResponse(City city) {
        CommunityStats stats = communityStatsRepository.findByCityId(city.getId()).orElse(null);
        return new CityResponse(
                city.getId(),
                city.getCountry().getId(),
                city.getCountry().getName(),
                city.getState().getId(),
                city.getState().getName(),
                city.getSlug(),
                city.getName(),
                city.getLatitude(),
                city.getLongitude(),
                city.getDescription(),
                city.getHeroImageUrl(),
                city.isActive(),
                statsResponse(stats)
        );
    }

    private CommunityStatsResponse statsResponse(CommunityStats stats) {
        if (stats == null) {
            return new CommunityStatsResponse(0, 0, 0, 0, 0, 0, 0, 0);
        }
        return new CommunityStatsResponse(
                stats.getMeditatorCount(),
                stats.getVolunteerCount(),
                stats.getTeacherCount(),
                stats.getYantraCount(),
                stats.getSannidhiCount(),
                stats.getCenterCount(),
                stats.getTempleCount(),
                stats.getShopCount()
        );
    }
}

