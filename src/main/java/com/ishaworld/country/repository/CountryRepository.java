package com.ishaworld.country.repository;

import com.ishaworld.country.entity.Country;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByIsoCodeIgnoreCase(String isoCode);

    List<Country> findTop10ByNameContainingIgnoreCase(String name);
}

