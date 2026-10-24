ALTER TABLE teacher
    ADD COLUMN latitude DECIMAL(10, 6),
    ADD COLUMN longitude DECIMAL(10, 6);

UPDATE teacher t
SET latitude = c.latitude + ((random() - 0.5) / 6),
    longitude = c.longitude + ((random() - 0.5) / 6)
FROM city c
WHERE t.city_id = c.id
  AND c.latitude IS NOT NULL
  AND c.longitude IS NOT NULL;

CREATE INDEX idx_teacher_coords ON teacher(latitude, longitude);
