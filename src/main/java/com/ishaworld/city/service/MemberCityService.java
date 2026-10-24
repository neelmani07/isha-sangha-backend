package com.ishaworld.city.service;

import com.ishaworld.city.dto.MemberCityRequest;
import com.ishaworld.city.dto.PickedCityResponse;
import com.ishaworld.city.entity.City;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.country.entity.Country;
import com.ishaworld.country.repository.CountryRepository;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.state.entity.State;
import com.ishaworld.state.repository.StateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lets members add a city that isn't on the map yet. The country and state are created if needed; the
 * city starts PENDING (usable by the member right away, public once an admin approves it). A city with
 * the same name within 40 km is reused instead of creating a duplicate.
 */
@Service
public class MemberCityService {

    private static final double SAME_CITY_KM = 40;
    private static final int MAX_NEW_CITIES_PER_MEMBER = 5;

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

    public MemberCityService(CityRepository cityRepository, StateRepository stateRepository, CountryRepository countryRepository) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional
    public PickedCityResponse addOrFind(Long userId, MemberCityRequest request) {
        String name = request.name().trim();
        Country country = countryRepository.findByIsoCodeIgnoreCase(request.countryCode())
                .orElseGet(() -> createCountry(request));

        for (City existing : cityRepository.findByCountryIdAndNameIgnoreCase(country.getId(), name)) {
            if (existing.getListingStatus() != ListingStatus.REJECTED && existing.getLatitude() != null
                    && distanceKm(existing.getLatitude().doubleValue(), existing.getLongitude().doubleValue(),
                    request.latitude(), request.longitude()) <= SAME_CITY_KM) {
                return toResponse(existing);
            }
        }

        long added = cityRepository.findByListingStatusOrderByCreatedAtAsc(ListingStatus.PENDING).stream()
                .filter(c -> userId.equals(c.getSubmittedByUserId())).count();
        if (added >= MAX_NEW_CITIES_PER_MEMBER) {
            throw new BusinessException("You have added several new cities that are still waiting for review. Please pick one of those.");
        }

        String stateName = request.stateName() == null || request.stateName().isBlank() ? name : request.stateName().trim();
        State state = stateRepository.findFirstByCountryIdAndNameIgnoreCase(country.getId(), stateName)
                .orElseGet(() -> createState(country, stateName, request));

        City city = new City();
        city.setCountry(country);
        city.setState(state);
        city.setName(name);
        city.setSlug(uniqueSlug(name, state.getName()));
        city.setLatitude(coord(request.latitude()));
        city.setLongitude(coord(request.longitude()));
        city.setActive(true);
        city.setListingStatus(ListingStatus.PENDING);
        city.setSubmittedByUserId(userId);
        return toResponse(cityRepository.save(city));
    }

    private Country createCountry(MemberCityRequest request) {
        Country country = new Country();
        country.setName(request.countryName().trim());
        country.setIsoCode(request.countryCode().toUpperCase(Locale.ROOT));
        country.setLatitude(coord(request.latitude()));
        country.setLongitude(coord(request.longitude()));
        return countryRepository.save(country);
    }

    private State createState(Country country, String stateName, MemberCityRequest request) {
        State state = new State();
        state.setCountry(country);
        state.setName(stateName);
        state.setLatitude(coord(request.latitude()));
        state.setLongitude(coord(request.longitude()));
        return stateRepository.save(state);
    }

    private String uniqueSlug(String name, String stateName) {
        String base = SlugGenerator.from(name);
        String slug = cityRepository.existsBySlug(base) ? SlugGenerator.from(name + " " + stateName) : base;
        String candidate = slug;
        for (int i = 2; cityRepository.existsBySlug(candidate); i++) {
            candidate = slug + "-" + i;
        }
        return candidate;
    }

    private static BigDecimal coord(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static PickedCityResponse toResponse(City city) {
        return new PickedCityResponse(city.getId(), city.getName(), city.getState().getName(), city.getCountry().getName(),
                city.getLatitude().doubleValue(), city.getLongitude().doubleValue(), city.getListingStatus());
    }
}
