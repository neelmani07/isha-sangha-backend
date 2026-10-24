-- Richer center and teacher profiles, plus the fields self-submitted listings need
-- (status for the approval queue, proof for verification, who submitted it).

ALTER TABLE center
    ADD COLUMN opening_hours VARCHAR(500),
    ADD COLUMN photo_url VARCHAR(500),
    ADD COLUMN languages VARCHAR(255),
    ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN listing_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN proof_url VARCHAR(500),
    ADD COLUMN submitted_by_user_id BIGINT REFERENCES app_user(id);

ALTER TABLE teacher
    ADD COLUMN teaching_mode VARCHAR(20) NOT NULL DEFAULT 'IN_PERSON',
    ADD COLUMN languages VARCHAR(255),
    ADD COLUMN years_teaching INTEGER,
    ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN listing_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN proof_url VARCHAR(500),
    ADD COLUMN submitted_by_user_id BIGINT REFERENCES app_user(id);

CREATE INDEX idx_center_listing_status ON center(listing_status);
CREATE INDEX idx_teacher_listing_status ON teacher(listing_status);

-- A member's public meditator profile. Location is deliberately approximate:
-- only a jittered point inside the member's city is ever stored.
CREATE TABLE meditator
(
    id BIGSERIAL PRIMARY KEY,
    app_user_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id),
    city_id BIGINT NOT NULL REFERENCES city(id),
    display_name VARCHAR(120) NOT NULL,
    approx_latitude DECIMAL(10,6) NOT NULL,
    approx_longitude DECIMAL(10,6) NOT NULL,
    about VARCHAR(500),
    practices VARCHAR(500),
    languages VARCHAR(255),
    open_to_connect BOOLEAN NOT NULL DEFAULT FALSE,
    open_to_volunteer BOOLEAN NOT NULL DEFAULT FALSE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meditator_city ON meditator(city_id);
CREATE INDEX idx_meditator_coords ON meditator(approx_latitude, approx_longitude);
