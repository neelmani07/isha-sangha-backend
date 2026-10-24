package com.ishaworld.nearby.service;

import com.ishaworld.nearby.dto.NearbyResultResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NearbyService {

    private static final String DISTANCE_EXPR = """
            (6371 * acos(LEAST(1.0, GREATEST(-1.0,
                cos(radians(:lat)) * cos(radians(%s)) * cos(radians(%s) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(%s))
            ))))
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<NearbyResultResponse> findNearby(
            double lat,
            double lng,
            double radiusKm,
            Set<String> types,
            int limit
    ) {
        List<NearbyResultResponse> results = new ArrayList<>();

        if (includeType(types, "TEACHER")) {
            results.addAll(findTeachers(lat, lng, radiusKm));
        }

        if (includeType(types, "CENTER") || includeType(types, "MAJOR_CENTER")
                || includeType(types, "CITY_CENTER") || includeType(types, "DEVI_TEMPLE")
                || includeType(types, "ISHA_SHOP")) {
            results.addAll(findCenters(lat, lng, radiusKm, types));
        }

        if (includeType(types, "CITY")) {
            results.addAll(findCities(lat, lng, radiusKm));
        }

        // Meditators only when asked for explicitly, so people never crowd out places in the default list.
        if (types != null && types.contains("MEDITATOR")) {
            results.addAll(findMeditators(lat, lng, radiusKm));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(NearbyResultResponse::distanceKm))
                .limit(limit)
                .toList();
    }

    private List<NearbyResultResponse> findTeachers(double lat, double lng, double radiusKm) {
        String distance = DISTANCE_EXPR.formatted("t.latitude", "t.longitude", "t.latitude");
        String sql = """
                SELECT * FROM (
                    SELECT
                        'TEACHER' AS type,
                        t.id,
                        t.slug,
                        t.name,
                        t.latitude AS lat,
                        t.longitude AS lng,
                        c.name AS subtitle,
                        c.slug AS city_slug,
                        %s AS distance_km
                    FROM teacher t
                    JOIN city c ON c.id = t.city_id
                    WHERE t.active = TRUE
                      AND t.listing_status = 'APPROVED'
                      AND t.latitude IS NOT NULL
                      AND t.longitude IS NOT NULL
                ) nearby_teachers
                WHERE distance_km <= :radiusKm
                """.formatted(distance);

        return mapResults(createQuery(sql, lat, lng, radiusKm).getResultList());
    }

    private List<NearbyResultResponse> findCenters(double lat, double lng, double radiusKm, Set<String> types) {
        String distance = DISTANCE_EXPR.formatted("ctr.latitude", "ctr.longitude", "ctr.latitude");
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM (
                    SELECT
                        ctr.type AS type,
                        ctr.id,
                        ctr.slug,
                        ctr.name,
                        ctr.latitude AS lat,
                        ctr.longitude AS lng,
                        city.name AS subtitle,
                        city.slug AS city_slug,
                        %s AS distance_km
                    FROM center ctr
                    JOIN city ON city.id = ctr.city_id
                    WHERE ctr.active = TRUE
                      AND ctr.listing_status = 'APPROVED'
                      AND ctr.latitude IS NOT NULL
                      AND ctr.longitude IS NOT NULL
                """.formatted(distance));

        if (types != null && !types.isEmpty() && !types.contains("CENTER")) {
            sql.append(" AND ctr.type IN (:centerTypes)");
        }

        sql.append("""
                ) nearby_centers
                WHERE distance_km <= :radiusKm
                """);

        Query query = createQuery(sql.toString(), lat, lng, radiusKm);
        if (types != null && !types.isEmpty() && !types.contains("CENTER")) {
            query.setParameter("centerTypes", resolveCenterTypes(types));
        }

        return mapResults(query.getResultList());
    }

    private List<NearbyResultResponse> findCities(double lat, double lng, double radiusKm) {
        String distance = DISTANCE_EXPR.formatted("c.latitude", "c.longitude", "c.latitude");
        String sql = """
                SELECT * FROM (
                    SELECT
                        'CITY' AS type,
                        c.id,
                        c.slug,
                        c.name,
                        c.latitude AS lat,
                        c.longitude AS lng,
                        s.name AS subtitle,
                        c.slug AS city_slug,
                        %s AS distance_km
                    FROM city c
                    JOIN state s ON s.id = c.state_id
                    WHERE c.active = TRUE
                      AND c.latitude IS NOT NULL
                      AND c.longitude IS NOT NULL
                ) nearby_cities
                WHERE distance_km <= :radiusKm
                """.formatted(distance);

        return mapResults(createQuery(sql, lat, lng, radiusKm).getResultList());
    }

    private List<NearbyResultResponse> findMeditators(double lat, double lng, double radiusKm) {
        String distance = DISTANCE_EXPR.formatted("m.approx_latitude", "m.approx_longitude", "m.approx_latitude");
        String sql = """
                SELECT * FROM (
                    SELECT
                        'MEDITATOR' AS type,
                        m.id,
                        NULL AS slug,
                        m.display_name AS name,
                        m.approx_latitude AS lat,
                        m.approx_longitude AS lng,
                        c.name AS subtitle,
                        c.slug AS city_slug,
                        %s AS distance_km
                    FROM meditator m
                    JOIN city c ON c.id = m.city_id
                    WHERE m.visible = TRUE
                      AND m.active = TRUE
                ) nearby_meditators
                WHERE distance_km <= :radiusKm
                """.formatted(distance);

        return mapResults(createQuery(sql, lat, lng, radiusKm).getResultList());
    }

    private Query createQuery(String sql, double lat, double lng, double radiusKm) {
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("lat", lat);
        query.setParameter("lng", lng);
        query.setParameter("radiusKm", radiusKm);
        return query;
    }

    private List<NearbyResultResponse> mapResults(List<?> rows) {
        List<NearbyResultResponse> results = new ArrayList<>();

        for (Object row : rows) {
            Object[] values = (Object[]) row;
            results.add(new NearbyResultResponse(
                    String.valueOf(values[0]),
                    ((Number) values[1]).longValue(),
                    values[2] == null ? null : String.valueOf(values[2]),
                    String.valueOf(values[3]),
                    ((Number) values[4]).doubleValue(),
                    ((Number) values[5]).doubleValue(),
                    // SELECT order is ..., subtitle (6), city_slug (7), distance_km (8)
                    ((Number) values[8]).doubleValue(),
                    values[6] == null ? null : String.valueOf(values[6]),
                    values[7] == null ? null : String.valueOf(values[7])
            ));
        }

        return results;
    }

    private boolean includeType(Set<String> types, String type) {
        return types == null || types.isEmpty() || types.contains(type.toUpperCase(Locale.ROOT));
    }

    private List<String> resolveCenterTypes(Set<String> types) {
        List<String> centerTypes = new ArrayList<>();
        for (String type : types) {
            String normalized = type.toUpperCase(Locale.ROOT);
            if (normalized.equals("CENTER")
                    || normalized.equals("MAJOR_CENTER")
                    || normalized.equals("CITY_CENTER")
                    || normalized.equals("DEVI_TEMPLE")
                    || normalized.equals("ISHA_SHOP")) {
                centerTypes.add(normalized.equals("CENTER") ? "CITY_CENTER" : normalized);
            }
        }

        if (centerTypes.isEmpty()) {
            centerTypes.add("CITY_CENTER");
        }

        return centerTypes.stream().distinct().toList();
    }
}
