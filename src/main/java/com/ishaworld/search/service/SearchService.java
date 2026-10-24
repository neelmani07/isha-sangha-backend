package com.ishaworld.search.service;

import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.country.repository.CountryRepository;
import com.ishaworld.search.dto.SearchResultResponse;
import com.ishaworld.state.repository.StateRepository;
import com.ishaworld.teacher.repository.TeacherRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final CenterRepository centerRepository;
    private final TeacherRepository teacherRepository;

    public SearchService(
            CountryRepository countryRepository,
            StateRepository stateRepository,
            CityRepository cityRepository,
            CenterRepository centerRepository,
            TeacherRepository teacherRepository
    ) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.centerRepository = centerRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Names use Indian spelling ("Centre"), so also accept American spelling in searches.
        query = query.trim().replaceAll("(?i)center", "centre").replaceAll("(?i)program(?!me)", "programme");

        List<SearchResultResponse> results = new ArrayList<>();

        countryRepository.findTop10ByNameContainingIgnoreCase(query)
                .forEach(country -> results.add(new SearchResultResponse(
                        "COUNTRY",
                        country.getId(),
                        null,
                        country.getName(),
                        toDouble(country.getLatitude()),
                        toDouble(country.getLongitude())
                )));

        stateRepository.findTop10ByNameContainingIgnoreCase(query)
                .forEach(state -> results.add(new SearchResultResponse(
                        "STATE",
                        state.getId(),
                        null,
                        state.getName(),
                        toDouble(state.getLatitude()),
                        toDouble(state.getLongitude())
                )));

        cityRepository.findTop10ByNameContainingIgnoreCase(query)
                .forEach(city -> results.add(new SearchResultResponse(
                        "CITY",
                        city.getId(),
                        city.getSlug(),
                        city.getName(),
                        toDouble(city.getLatitude()),
                        toDouble(city.getLongitude())
                )));

        centerRepository.findTop10ByNameContainingIgnoreCaseAndActiveTrueAndListingStatus(query, ListingStatus.APPROVED)
                .forEach(center -> results.add(new SearchResultResponse(
                        "CENTER",
                        center.getId(),
                        center.getSlug(),
                        center.getName(),
                        toDouble(center.getLatitude()),
                        toDouble(center.getLongitude())
                )));

        teacherRepository.findTop10ByNameContainingIgnoreCaseAndActiveTrueAndListingStatus(query, ListingStatus.APPROVED)
                .forEach(teacher -> results.add(new SearchResultResponse(
                        "TEACHER",
                        teacher.getId(),
                        teacher.getSlug(),
                        teacher.getName(),
                        toDouble(teacher.getCity().getLatitude()),
                        toDouble(teacher.getCity().getLongitude())
                )));

        return results.stream().limit(25).toList();
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}

