package com.ishaworld.country.repository;

import com.ishaworld.country.entity.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByIsoCodeIgnoreCase(String isoCode);

    Optional<Country> findByIsoCodeIgnoreCase(String isoCode);

    List<Country> findTop10ByNameContainingIgnoreCase(String name);
}

