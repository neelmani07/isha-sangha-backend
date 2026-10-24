package com.ishaworld.city.repository;

import com.ishaworld.city.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    Page<City> findByCountryId(Long countryId, Pageable pageable);

    Page<City> findByStateId(Long stateId, Pageable pageable);

    Optional<City> findBySlug(String slug);

    long countByCountryId(Long countryId);

    long countByStateId(Long stateId);

    List<City> findTop10ByNameContainingIgnoreCase(String name);
}

