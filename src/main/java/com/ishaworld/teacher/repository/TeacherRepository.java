package com.ishaworld.teacher.repository;

import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.teacher.entity.Teacher;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, Long>, TeacherRepositoryCustom {

    Page<Teacher> findByCityId(Long cityId, Pageable pageable);

    Optional<Teacher> findBySlug(String slug);

    long countByCityId(Long cityId);

    @Query("select count(t) from Teacher t where t.city.country.id = :countryId")
    long countByCountryId(@Param("countryId") Long countryId);

    @Query("select t from Teacher t where lower(t.city.name) = lower(:city)")
    Page<Teacher> findByCityName(@Param("city") String city, Pageable pageable);

    @Query("select t from Teacher t where lower(t.city.country.name) = lower(:country)")
    Page<Teacher> findByCountryName(@Param("country") String country, Pageable pageable);

    @Query("select distinct t from Teacher t join t.programs p where lower(p.name) = lower(:program)")
    Page<Teacher> findByProgramName(@Param("program") String program, Pageable pageable);

    List<Teacher> findTop10ByNameContainingIgnoreCase(String name);

    /** Active, approved teachers: what the public map may show. */
    @EntityGraph(attributePaths = "city")
    Page<Teacher> findByActiveTrueAndListingStatus(ListingStatus listingStatus, Pageable pageable);

    /** Rows of [cityId, count] of public teachers. */
    @Query("select t.city.id, count(t) from Teacher t where t.active = true and t.listingStatus = :status group by t.city.id")
    List<Object[]> countPublicByCity(@Param("status") ListingStatus status);

    /** Rows of [countryId, count] of public teachers. */
    @Query("select t.city.country.id, count(t) from Teacher t where t.active = true and t.listingStatus = :status group by t.city.country.id")
    List<Object[]> countPublicByCountry(@Param("status") ListingStatus status);
}

