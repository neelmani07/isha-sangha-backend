INSERT INTO state (country_id, name, latitude, longitude)
VALUES
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Maharashtra', 19.7515, 75.7139),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Delhi', 28.7041, 77.1025),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Gujarat', 22.2587, 71.1924),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Rajasthan', 27.0238, 74.2179),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Uttar Pradesh', 26.8467, 80.9462),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Bihar', 25.0961, 85.3131),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'West Bengal', 22.9868, 87.8550),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Odisha', 20.9517, 85.0985),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Telangana', 18.1124, 79.0193),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Andhra Pradesh', 15.9129, 79.7400),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Kerala', 10.8505, 76.2711),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Madhya Pradesh', 22.9734, 78.6569),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Punjab', 31.1471, 75.3412),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Haryana', 29.0588, 76.0856),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Assam', 26.2006, 92.9376),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Goa', 15.2993, 74.1240);

WITH city_seed AS (
    SELECT
        gs,
        ('isha-demo-city-' || gs) AS slug,
        (
            (ARRAY[
                'Aaranya', 'Adiyogi', 'Ananda', 'Arpana', 'Bhava', 'Dhyanalinga', 'Ganga', 'Isha',
                'Jeeva', 'Kaveri', 'Linga', 'Mahima', 'Nadi', 'Nirvana', 'Prana', 'Sadhana',
                'Sangha', 'Surya', 'Tejas', 'Velliangiri', 'Yogi', 'Yatra', 'Aum', 'Devi',
                'Kriya', 'Nila', 'Rudra', 'Shakti', 'Turiya', 'Vana'
            ])[((gs - 1) % 30) + 1] || ' Nagar ' || gs
        ) AS name,
        (8 + (random() * 25))::numeric(10,6) AS latitude,
        (68 + (random() * 28))::numeric(10,6) AS longitude
    FROM generate_series(1, 160) AS gs
)
INSERT INTO city (country_id, state_id, slug, name, latitude, longitude, description, hero_image_url, active)
SELECT
    (SELECT id FROM country WHERE iso_code = 'IN'),
    (
        SELECT id
        FROM state
        WHERE country_id = (SELECT id FROM country WHERE iso_code = 'IN')
        ORDER BY id
        OFFSET ((city_seed.gs - 1) % (
            SELECT count(*)
            FROM state
            WHERE country_id = (SELECT id FROM country WHERE iso_code = 'IN')
        ))
        LIMIT 1
    ),
    city_seed.slug,
    city_seed.name,
    city_seed.latitude,
    city_seed.longitude,
    'Demo Isha community marker for exploration, local statistics, centers, and teacher discovery.',
    NULL,
    TRUE
FROM city_seed;

INSERT INTO community_stats (
    city_id,
    meditator_count,
    volunteer_count,
    teacher_count,
    yantra_count,
    sannidhi_count,
    center_count,
    temple_count,
    shop_count
)
SELECT
    city.id,
    80 + ((city.id * 37) % 3200),
    10 + ((city.id * 13) % 420),
    1 + ((city.id * 5) % 12),
    3 + ((city.id * 7) % 90),
    1 + ((city.id * 11) % 35),
    CASE WHEN city.id % 3 = 0 THEN 2 ELSE 1 END,
    CASE WHEN city.id % 29 = 0 THEN 1 ELSE 0 END,
    CASE WHEN city.id % 9 = 0 THEN 1 ELSE 0 END
FROM city
WHERE city.slug LIKE 'isha-demo-city-%';

WITH center_seed AS (
    SELECT
        gs,
        city.id AS city_id,
        city.latitude,
        city.longitude,
        city.name AS city_name
    FROM generate_series(1, 110) AS gs
    JOIN LATERAL (
        SELECT id, latitude, longitude, name
        FROM city
        WHERE slug LIKE 'isha-demo-city-%'
        ORDER BY id
        OFFSET ((gs - 1) % 160)
        LIMIT 1
    ) city ON TRUE
)
INSERT INTO center (city_id, slug, name, type, description, address, latitude, longitude, phone, email, website, active)
SELECT
    city_id,
    'isha-demo-center-' || gs,
    'Isha ' || city_name || ' Center',
    CASE
        WHEN gs % 23 = 0 THEN 'DEVI_TEMPLE'
        WHEN gs % 11 = 0 THEN 'ISHA_SHOP'
        WHEN gs % 7 = 0 THEN 'MAJOR_CENTER'
        ELSE 'CITY_CENTER'
    END,
    'Demo center for local discovery and side panel exploration.',
    city_name || ', India',
    latitude + ((random() - 0.5) / 8),
    longitude + ((random() - 0.5) / 8),
    NULL,
    'center' || gs || '@example.com',
    'https://isha.sadhguru.org',
    TRUE
FROM center_seed;

WITH teacher_seed AS (
    SELECT
        gs,
        city.id AS city_id,
        city.name AS city_name
    FROM generate_series(1, 220) AS gs
    JOIN LATERAL (
        SELECT id, name
        FROM city
        WHERE slug LIKE 'isha-demo-city-%'
        ORDER BY id
        OFFSET ((gs - 1) % 160)
        LIMIT 1
    ) city ON TRUE
)
INSERT INTO teacher (city_id, slug, name, bio, photo_url, phone, email, website, active)
SELECT
    city_id,
    'isha-demo-teacher-' || gs,
    (
        (ARRAY[
            'Aadi', 'Aarav', 'Anika', 'Devika', 'Ishan', 'Kavya', 'Meera', 'Naren', 'Prisha', 'Rishi',
            'Sahana', 'Tanmay', 'Uma', 'Vihaan', 'Yamini', 'Anaya', 'Dhruv', 'Gauri', 'Kiran', 'Mira'
        ])[((gs - 1) % 20) + 1] || ' ' || gs
    ),
    'Demo Isha Hatha Yoga teacher serving the ' || city_name || ' community.',
    NULL,
    NULL,
    'teacher' || gs || '@example.com',
    NULL,
    TRUE
FROM teacher_seed;

INSERT INTO teacher_program (teacher_id, program_id)
SELECT teacher.id, program.id
FROM teacher
JOIN program ON program.name IN (
    CASE WHEN teacher.id % 2 = 0 THEN 'Surya Kriya' ELSE 'Yogasanas' END,
    CASE WHEN teacher.id % 3 = 0 THEN 'Angamardana' ELSE 'Bhuta Shuddhi' END
)
WHERE teacher.slug LIKE 'isha-demo-teacher-%';

INSERT INTO social_link (entity_type, entity_id, platform, url)
SELECT 'CITY', city.id, 'WEBSITE', 'https://isha.sadhguru.org'
FROM city
WHERE city.slug LIKE 'isha-demo-city-%'
  AND city.id % 8 = 0;

INSERT INTO media (entity_type, entity_id, url, title, is_primary)
SELECT
    'CITY',
    city.id,
    'https://images.unsplash.com/photo-1524492412937-b28074a5d7da',
    city.name || ' community image',
    TRUE
FROM city
WHERE city.slug LIKE 'isha-demo-city-%'
  AND city.id % 12 = 0;

