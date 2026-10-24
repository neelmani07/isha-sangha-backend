INSERT INTO country (name, iso_code, latitude, longitude)
VALUES
    ('India', 'IN', 20.5937, 78.9629),
    ('United States', 'US', 37.0902, -95.7129),
    ('Germany', 'DE', 51.1657, 10.4515),
    ('Australia', 'AU', -25.2744, 133.7751)
ON CONFLICT (iso_code) DO NOTHING;

INSERT INTO state (country_id, name, latitude, longitude)
VALUES
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Tamil Nadu', 11.1271, 78.6569),
    ((SELECT id FROM country WHERE iso_code = 'IN'), 'Karnataka', 15.3173, 75.7139),
    ((SELECT id FROM country WHERE iso_code = 'US'), 'Tennessee', 35.5175, -86.5804),
    ((SELECT id FROM country WHERE iso_code = 'DE'), 'Berlin', 52.5200, 13.4050),
    ((SELECT id FROM country WHERE iso_code = 'AU'), 'Victoria', -37.8136, 144.9631);

INSERT INTO city (country_id, state_id, slug, name, latitude, longitude, description, hero_image_url, active)
VALUES
    (
        (SELECT id FROM country WHERE iso_code = 'IN'),
        (SELECT id FROM state WHERE name = 'Tamil Nadu'),
        'coimbatore',
        'Coimbatore',
        11.0168,
        76.9558,
        'Home region of the Isha Yoga Center and a major anchor for the global Isha ecosystem.',
        NULL,
        TRUE
    ),
    (
        (SELECT id FROM country WHERE iso_code = 'IN'),
        (SELECT id FROM state WHERE name = 'Karnataka'),
        'bengaluru',
        'Bengaluru',
        12.9716,
        77.5946,
        'A large urban Isha community with centers, teachers, and frequent local activity.',
        NULL,
        TRUE
    ),
    (
        (SELECT id FROM country WHERE iso_code = 'US'),
        (SELECT id FROM state WHERE name = 'Tennessee'),
        'mcminnville',
        'McMinnville',
        35.6834,
        -85.7699,
        'Regional home of Isha Institute of Inner-sciences.',
        NULL,
        TRUE
    ),
    (
        (SELECT id FROM country WHERE iso_code = 'DE'),
        (SELECT id FROM state WHERE name = 'Berlin'),
        'berlin',
        'Berlin',
        52.5200,
        13.4050,
        'A growing European city community with local practice and teacher discovery.',
        NULL,
        TRUE
    ),
    (
        (SELECT id FROM country WHERE iso_code = 'AU'),
        (SELECT id FROM state WHERE name = 'Victoria'),
        'melbourne',
        'Melbourne',
        -37.8136,
        144.9631,
        'Australian city community for local discovery and connection.',
        NULL,
        TRUE
    );

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
VALUES
    ((SELECT id FROM city WHERE slug = 'coimbatore'), 25000, 1800, 80, 1200, 250, 3, 2, 2),
    ((SELECT id FROM city WHERE slug = 'bengaluru'), 8500, 520, 32, 350, 88, 2, 1, 1),
    ((SELECT id FROM city WHERE slug = 'mcminnville'), 4200, 300, 18, 125, 32, 1, 0, 1),
    ((SELECT id FROM city WHERE slug = 'berlin'), 1500, 90, 8, 42, 11, 1, 0, 0),
    ((SELECT id FROM city WHERE slug = 'melbourne'), 1800, 110, 9, 50, 15, 1, 0, 0);

INSERT INTO center (city_id, slug, name, type, description, address, latitude, longitude, phone, email, website, active)
VALUES
    ((SELECT id FROM city WHERE slug = 'coimbatore'), 'isha-yoga-center-coimbatore', 'Isha Yoga Center Coimbatore', 'MAJOR_CENTER', 'Major Isha center near the Velliangiri foothills.', 'Coimbatore, Tamil Nadu', 10.9764, 76.7357, NULL, NULL, 'https://isha.sadhguru.org', TRUE),
    ((SELECT id FROM city WHERE slug = 'bengaluru'), 'isha-bengaluru', 'Isha Bengaluru', 'CITY_CENTER', 'City center for local Isha activities.', 'Bengaluru, Karnataka', 12.9716, 77.5946, NULL, NULL, 'https://isha.sadhguru.org', TRUE),
    ((SELECT id FROM city WHERE slug = 'mcminnville'), 'isha-institute-of-inner-sciences', 'Isha Institute of Inner-sciences', 'MAJOR_CENTER', 'Major North America Isha center.', 'McMinnville, Tennessee', 35.6834, -85.7699, NULL, NULL, 'https://isha.sadhguru.org/us/en/center/isha-institute-inner-sciences-usa', TRUE),
    ((SELECT id FROM city WHERE slug = 'berlin'), 'isha-berlin', 'Isha Berlin', 'CITY_CENTER', 'Local Isha community presence in Berlin.', 'Berlin, Germany', 52.5200, 13.4050, NULL, NULL, 'https://isha.sadhguru.org', TRUE);

INSERT INTO program (name, description)
VALUES
    ('Surya Kriya', 'A powerful yogic practice of tremendous antiquity.'),
    ('Yogasanas', 'A way of aligning the inner system and naturally achieving a chemistry of healthfulness.'),
    ('Angamardana', 'A fitness system rooted in yoga that offers everyone the opportunity to invigorate the body.'),
    ('Bhuta Shuddhi', 'A process of purifying the five elements within the human system.'),
    ('Children Yoga', 'Yoga programs designed for children.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO teacher (city_id, slug, name, bio, photo_url, phone, email, website, active)
VALUES
    ((SELECT id FROM city WHERE slug = 'coimbatore'), 'ananya-coimbatore', 'Ananya', 'Isha Hatha Yoga teacher offering classical practices in Coimbatore.', NULL, NULL, 'ananya@example.com', NULL, TRUE),
    ((SELECT id FROM city WHERE slug = 'bengaluru'), 'arjun-bengaluru', 'Arjun', 'Isha Hatha Yoga teacher serving the Bengaluru community.', NULL, NULL, 'arjun@example.com', NULL, TRUE),
    ((SELECT id FROM city WHERE slug = 'berlin'), 'maya-berlin', 'Maya', 'Isha Hatha Yoga teacher offering programs in Berlin.', NULL, NULL, 'maya@example.com', NULL, TRUE),
    ((SELECT id FROM city WHERE slug = 'melbourne'), 'dev-melbourne', 'Dev', 'Isha Hatha Yoga teacher offering programs in Melbourne.', NULL, NULL, 'dev@example.com', NULL, TRUE);

INSERT INTO teacher_program (teacher_id, program_id)
SELECT t.id, p.id
FROM teacher t
CROSS JOIN program p
WHERE t.slug IN ('ananya-coimbatore', 'arjun-bengaluru', 'maya-berlin', 'dev-melbourne')
  AND p.name IN ('Surya Kriya', 'Yogasanas');

INSERT INTO social_link (entity_type, entity_id, platform, url)
VALUES
    ('CITY', (SELECT id FROM city WHERE slug = 'coimbatore'), 'WEBSITE', 'https://isha.sadhguru.org'),
    ('CENTER', (SELECT id FROM center WHERE slug = 'isha-yoga-center-coimbatore'), 'WEBSITE', 'https://isha.sadhguru.org'),
    ('CITY', (SELECT id FROM city WHERE slug = 'berlin'), 'WEBSITE', 'https://isha.sadhguru.org');

