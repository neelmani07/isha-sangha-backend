-- V2/V3 seeded demo data for building the map: 160 made-up cities ("Aaranya Nagar 1"...), generated centres
-- and teachers with example.com addresses, and invented community counts. Useful in development, wrong on the
-- live site. This removes it only where spring.flyway.placeholders.removeDemoData = true (the prod profile).
-- Kept everywhere: real countries, states and cities, programmes, and the original V2 centres.
DO $$
BEGIN
    IF '${removeDemoData}' <> 'true' THEN
        RETURN;
    END IF;

    -- Invented counts (e.g. "1,920 meditators") shown on city pages
    DELETE FROM community_stats;

    -- Generated and example teachers
    DELETE FROM teacher_program
    WHERE teacher_id IN (SELECT id FROM teacher WHERE slug LIKE 'isha-demo-teacher-%' OR email LIKE '%@example.com');
    DELETE FROM social_link
    WHERE entity_type = 'TEACHER' AND entity_id IN (SELECT id FROM teacher WHERE slug LIKE 'isha-demo-teacher-%' OR email LIKE '%@example.com');
    DELETE FROM teacher
    WHERE (slug LIKE 'isha-demo-teacher-%' OR email LIKE '%@example.com') AND submitted_by_user_id IS NULL;

    -- Generated centres
    DELETE FROM media WHERE entity_type = 'CENTER' AND entity_id IN (SELECT id FROM center WHERE slug LIKE 'isha-demo-center-%');
    DELETE FROM social_link WHERE entity_type = 'CENTER' AND entity_id IN (SELECT id FROM center WHERE slug LIKE 'isha-demo-center-%');
    DELETE FROM center c
    WHERE c.slug LIKE 'isha-demo-center-%' AND c.submitted_by_user_id IS NULL
      AND NOT EXISTS (SELECT 1 FROM story s WHERE s.center_id = c.id);

    -- Made-up cities, unless something real has been attached to them since
    DELETE FROM media WHERE entity_type = 'CITY' AND entity_id IN (SELECT id FROM city WHERE slug LIKE 'isha-demo-city-%');
    DELETE FROM social_link WHERE entity_type = 'CITY' AND entity_id IN (SELECT id FROM city WHERE slug LIKE 'isha-demo-city-%');
    DELETE FROM city ci
    WHERE ci.slug LIKE 'isha-demo-city-%'
      AND NOT EXISTS (SELECT 1 FROM center x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM teacher x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM meditator x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM initiative x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM resource_link x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM story x WHERE x.city_id = ci.id)
      AND NOT EXISTS (SELECT 1 FROM shop_listing x WHERE x.city_id = ci.id);
END $$;
