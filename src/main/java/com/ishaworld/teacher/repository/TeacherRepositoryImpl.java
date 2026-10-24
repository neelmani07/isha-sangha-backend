package com.ishaworld.teacher.repository;

import com.ishaworld.teacher.entity.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherRepositoryImpl implements TeacherRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Teacher> findByCityFilters(
            Long cityId,
            String q,
            Long programId,
            Double lat,
            Double lng,
            boolean sortByDistance,
            Pageable pageable
    ) {
        StringBuilder base = new StringBuilder("""
                FROM teacher t
                WHERE t.city_id = :cityId
                  AND t.active = TRUE
                  AND t.listing_status = 'APPROVED'
                """);

        if (q != null && !q.isBlank()) {
            base.append(" AND LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))");
        }

        if (programId != null) {
            base.append(" AND EXISTS (SELECT 1 FROM teacher_program tp WHERE tp.teacher_id = t.id AND tp.program_id = :programId)");
        }

        boolean byDistance = sortByDistance && lat != null && lng != null;
        String orderBy = byDistance
                ? """
                ORDER BY CASE
                    WHEN t.latitude IS NOT NULL AND t.longitude IS NOT NULL THEN
                        (6371 * acos(LEAST(1.0, GREATEST(-1.0,
                            cos(radians(:lat)) * cos(radians(t.latitude)) * cos(radians(t.longitude) - radians(:lng))
                            + sin(radians(:lat)) * sin(radians(t.latitude))
                        ))))
                    ELSE 999999
                END,
                t.name ASC
                """
                : " ORDER BY t.name ASC";

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + base);
        bindCityFilters(countQuery, cityId, q, programId, null, null);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query dataQuery = entityManager.createNativeQuery(
                "SELECT t.* " + base + orderBy,
                Teacher.class
        );
        bindCityFilters(dataQuery, cityId, q, programId, byDistance ? lat : null, byDistance ? lng : null);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Teacher> content = dataQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private void bindCityFilters(Query query, Long cityId, String q, Long programId, Double lat, Double lng) {
        query.setParameter("cityId", cityId);
        if (q != null && !q.isBlank()) {
            query.setParameter("q", q);
        }
        if (programId != null) {
            query.setParameter("programId", programId);
        }
        if (lat != null && lng != null) {
            query.setParameter("lat", lat);
            query.setParameter("lng", lng);
        }
    }
}
