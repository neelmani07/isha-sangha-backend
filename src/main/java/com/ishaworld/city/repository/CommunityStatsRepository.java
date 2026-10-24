package com.ishaworld.city.repository;

import com.ishaworld.city.entity.CommunityStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityStatsRepository extends JpaRepository<CommunityStats, Long> {

    Optional<CommunityStats> findByCityId(Long cityId);
}

