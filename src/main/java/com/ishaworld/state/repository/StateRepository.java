package com.ishaworld.state.repository;

import com.ishaworld.state.entity.State;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {

    Page<State> findByCountryId(Long countryId, Pageable pageable);

    List<State> findTop10ByNameContainingIgnoreCase(String name);
}

