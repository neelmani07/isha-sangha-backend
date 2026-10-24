package com.ishaworld.country.service;

import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.country.dto.CountryResponse;
import com.ishaworld.country.dto.CountryUpsertRequest;
import com.ishaworld.country.entity.Country;
import com.ishaworld.country.repository.CountryRepository;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.teacher.repository.TeacherRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final TeacherRepository teacherRepository;
    private final CenterRepository centerRepository;

    public CountryService(
            CountryRepository countryRepository,
            CityRepository cityRepository,
            TeacherRepository teacherRepository,
            CenterRepository centerRepository
    ) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.teacherRepository = teacherRepository;
        this.centerRepository = centerRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CountryResponse> findAll(Pageable pageable) {
        return PageResponse.from(countryRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CountryResponse findById(Long id) {
        return toResponse(getCountry(id));
    }

    @Transactional
    public CountryResponse create(CountryUpsertRequest request) {
        Country country = new Country();
        apply(country, request);
        return toResponse(countryRepository.save(country));
    }

    @Transactional
    public CountryResponse update(Long id, CountryUpsertRequest request) {
        Country country = getCountry(id);
        apply(country, request);
        return toResponse(countryRepository.save(country));
    }

    @Transactional
    public void delete(Long id) {
        countryRepository.delete(getCountry(id));
    }

    public Country getCountry(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Country not found"));
    }

    private void apply(Country country, CountryUpsertRequest request) {
        country.setName(request.name());
        country.setIsoCode(request.isoCode());
        country.setLatitude(request.latitude());
        country.setLongitude(request.longitude());
    }

    private CountryResponse toResponse(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getName(),
                country.getIsoCode(),
                country.getLatitude(),
                country.getLongitude(),
                cityRepository.countByCountryId(country.getId()),
                teacherRepository.countByCountryId(country.getId()),
                centerRepository.countByCountryId(country.getId())
        );
    }
}

