-- Members can add a city that isn't on the map yet (found through a worldwide map search).
-- Such cities are usable straight away in the member's own submission, but only appear publicly
-- once an admin approves them. Existing cities stay approved.
ALTER TABLE city
    ADD COLUMN listing_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN rejection_reason VARCHAR(500),
    ADD COLUMN reviewed_at TIMESTAMP,
    ADD COLUMN reviewed_by VARCHAR(255),
    ADD COLUMN submitted_by_user_id BIGINT REFERENCES app_user(id);

CREATE INDEX idx_city_listing_status ON city(listing_status);
