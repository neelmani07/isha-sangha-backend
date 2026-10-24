package com.ishaworld.meditator.repository;

import com.ishaworld.meditator.entity.Meditator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeditatorRepository extends JpaRepository<Meditator, Long> {

    Optional<Meditator> findByAppUserId(Long appUserId);

    @EntityGraph(attributePaths = "city")
    Page<Meditator> findByVisibleTrueAndActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "city")
    Page<Meditator> findByCityIdAndVisibleTrueAndActiveTrue(Long cityId, Pageable pageable);

    /** Rows of [cityId, count] for publicly visible meditators. */
    @Query("select m.city.id, count(m) from Meditator m where m.visible = true and m.active = true group by m.city.id")
    List<Object[]> countVisibleByCity();
}
