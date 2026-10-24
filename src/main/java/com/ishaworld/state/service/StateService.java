package com.ishaworld.state.service;

import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.country.service.CountryService;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.state.dto.StateResponse;
import com.ishaworld.state.dto.StateUpsertRequest;
import com.ishaworld.state.entity.State;
import com.ishaworld.state.repository.StateRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StateService {

    private final StateRepository stateRepository;
    private final CountryService countryService;
    private final CityRepository cityRepository;

    public StateService(StateRepository stateRepository, CountryService countryService, CityRepository cityRepository) {
        this.stateRepository = stateRepository;
        this.countryService = countryService;
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<StateResponse> findAll(Long countryId, Pageable pageable) {
        var page = countryId == null
                ? stateRepository.findAll(pageable)
                : stateRepository.findByCountryId(countryId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public StateResponse findById(Long id) {
        return toResponse(getState(id));
    }

    @Transactional
    public StateResponse create(StateUpsertRequest request) {
        State state = new State();
        apply(state, request);
        return toResponse(stateRepository.save(state));
    }

    @Transactional
    public StateResponse update(Long id, StateUpsertRequest request) {
        State state = getState(id);
        apply(state, request);
        return toResponse(stateRepository.save(state));
    }

    @Transactional
    public void delete(Long id) {
        stateRepository.delete(getState(id));
    }

    public State getState(Long id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("State not found"));
    }

    private void apply(State state, StateUpsertRequest request) {
        state.setCountry(countryService.getCountry(request.countryId()));
        state.setName(request.name());
        state.setLatitude(request.latitude());
        state.setLongitude(request.longitude());
    }

    private StateResponse toResponse(State state) {
        return new StateResponse(
                state.getId(),
                state.getCountry().getId(),
                state.getCountry().getName(),
                state.getName(),
                state.getLatitude(),
                state.getLongitude(),
                cityRepository.countByStateId(state.getId())
        );
    }
}

