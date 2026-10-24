package com.ishaworld.globe.service;

import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.country.repository.CountryRepository;
import com.ishaworld.globe.dto.GlobeMarkerResponse;
import com.ishaworld.meditator.service.MeditatorService;
import com.ishaworld.state.repository.StateRepository;
import com.ishaworld.teacher.repository.TeacherRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GlobeService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final CenterRepository centerRepository;
    private final TeacherRepository teacherRepository;
    private final MeditatorService meditatorService;

    public GlobeService(
            CountryRepository countryRepository,
            StateRepository stateRepository,
            CityRepository cityRepository,
            CenterRepository centerRepository,
            TeacherRepository teacherRepository,
            MeditatorService meditatorService
    ) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.centerRepository = centerRepository;
        this.teacherRepository = teacherRepository;
        this.meditatorService = meditatorService;
    }

    @Transactional(readOnly = true)
    public List<GlobeMarkerResponse> findMarkers(String level, String type) {
        return switch (level == null ? "country" : level.toLowerCase()) {
            case "state" -> stateRepository.findAll(PageRequest.of(0, 300))
                    .map(state -> new GlobeMarkerResponse(
                            state.getId(),
                            null,
                            state.getName(),
                            "STATE",
                            toDouble(state.getLatitude()),
                            toDouble(state.getLongitude()),
                            null,
                            null
                    ))
                    .getContent();
            case "city" -> {
                // One grouped query per kind instead of one count query per city.
                Map<Long, Long> teachers = toCounts(teacherRepository.countPublicByCity(ListingStatus.APPROVED));
                Map<Long, Long> meditators = meditatorService.countVisibleByCity();
                yield cityRepository.findByListingStatusAndActiveTrue(ListingStatus.APPROVED, PageRequest.of(0, 1000))
                        .map(city -> new GlobeMarkerResponse(
                                city.getId(),
                                city.getSlug(),
                                city.getName(),
                                "CITY",
                                toDouble(city.getLatitude()),
                                toDouble(city.getLongitude()),
                                Math.toIntExact(teachers.getOrDefault(city.getId(), 0L)),
                                Math.toIntExact(meditators.getOrDefault(city.getId(), 0L))
                        ))
                        .getContent();
            }
            case "center" -> centerRepository.findByActiveTrueAndListingStatus(ListingStatus.APPROVED, PageRequest.of(0, 1000))
                    .map(center -> new GlobeMarkerResponse(
                            center.getId(),
                            center.getSlug(),
                            center.getName(),
                            center.getType().name(),
                            toDouble(center.getLatitude()),
                            toDouble(center.getLongitude()),
                            null,
                            null
                    ))
                    .getContent();
            case "teacher" -> teacherRepository.findByActiveTrueAndListingStatus(ListingStatus.APPROVED, PageRequest.of(0, 1000))
                    .map(teacher -> new GlobeMarkerResponse(
                            teacher.getId(),
                            teacher.getSlug(),
                            teacher.getName(),
                            "TEACHER",
                            resolve(teacher.getLatitude(), teacher.getCity().getLatitude()),
                            resolve(teacher.getLongitude(), teacher.getCity().getLongitude()),
                            null,
                            null
                    ))
                    .getContent();
            // Approximate, city-level points only (see MeditatorService).
            case "meditator" -> meditatorService.findVisibleForGlobe().stream()
                    .map(meditator -> new GlobeMarkerResponse(
                            meditator.id(),
                            meditator.citySlug(),
                            meditator.displayName(),
                            "MEDITATOR",
                            meditator.lat(),
                            meditator.lng(),
                            null,
                            null
                    ))
                    .toList();
            default -> {
                Map<Long, Long> teachers = toCounts(teacherRepository.countPublicByCountry(ListingStatus.APPROVED));
                // A country appears once it has at least one approved city (members can add new countries).
                Set<Long> withCities = new HashSet<>(cityRepository.findCountryIdsWithStatus(ListingStatus.APPROVED));
                yield countryRepository.findAll(PageRequest.of(0, 250))
                        .filter(country -> withCities.contains(country.getId()))
                        .map(country -> new GlobeMarkerResponse(
                                country.getId(),
                                null,
                                country.getName(),
                                "COUNTRY",
                                toDouble(country.getLatitude()),
                                toDouble(country.getLongitude()),
                                Math.toIntExact(teachers.getOrDefault(country.getId(), 0L)),
                                null
                        ))
                        .toList();
            }
        };
    }

    private static Map<Long, Long> toCounts(List<Object[]> rows) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private static double toDouble(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    private static double resolve(BigDecimal value, BigDecimal fallback) {
        return value != null ? value.doubleValue() : toDouble(fallback);
    }
}
